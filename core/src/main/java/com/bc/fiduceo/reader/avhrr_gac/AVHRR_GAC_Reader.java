/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.avhrr_gac;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AVHRR_GAC_Reader implements Reader {

    private static final int NUM_SPLITS = 2;
    private static final String START_TIME_ATTRIBUTE_NAME = "start_time";
    private static final String STOP_TIME_ATTRIBUTE_NAME = "stop_time";

    private final GeometryFactory geometryFactory;

    private NetcdfFile netcdfFile;

    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;
    private long startTimeMilliSecondsSince1970;

    AVHRR_GAC_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);

        final String startTimeString = NetCDFUtils.getGlobalAttributeString(START_TIME_ATTRIBUTE_NAME, netcdfFile);
        startTimeMilliSecondsSince1970 = parseDate(startTimeString).getTime();

        timeLocator = null;
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        pixelLocator = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final Date startDate = new Date(startTimeMilliSecondsSince1970);
        acquisitionInfo.setSensingStart(startDate);

        final String stopDateString = NetCDFUtils.getGlobalAttributeString(STOP_TIME_ATTRIBUTE_NAME, netcdfFile);
        final Date stopDate = parseDate(stopDateString);
        acquisitionInfo.setSensingStop(stopDate);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final ArrayFloat lonStorage = (ArrayFloat) arrayCache.get("lon");
            final ArrayFloat latStorage = (ArrayFloat) arrayCache.get("lat");
            final int[] shape = lonStorage.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(lonStorage, latStorage, width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        final Array longitudes = arrayCache.get("lon");
        final int[] shape = longitudes.getShape();
        final int height = shape[0];
        final int width = shape[1];
        final int subsetHeight = getBoundingPolygonCreator().getSubsetHeight(height, NUM_SPLITS);
        final PixelLocator pixelLocator = getPixelLocator();

        return PixelLocatorFactory.getSubScenePixelLocator(sceneGeometry, width, height, subsetHeight, pixelLocator);
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final Array dTime = arrayCache.get("dtime");
            final String startTimeString = NetCDFUtils.getGlobalAttributeString(START_TIME_ATTRIBUTE_NAME, netcdfFile);
            final Date startDate = parseDate(startTimeString);

            timeLocator = new AVHRR_GAC_TimeLocator(dTime, startDate);
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws InvalidRangeException, IOException {
        final Array rawArray = arrayCache.get(variableName);
        final Number fillValue = getFillValue(variableName);

        final int defaultWidth = getProductWidth(netcdfFile);
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawArray, defaultWidth);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array array = readRaw(centerX, centerY, interval, variableName);

        final double scaleFactor = getScaleFactor(variableName);
        final double offset = getOffset(variableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array raw = readRaw(x, y, interval, "dtime");
        final Number fillValue = getFillValue("dtime");
        return convertToAcquisitionTime(raw, startTimeMilliSecondsSince1970, fillValue.floatValue());
    }

    @Override
    public List<Variable> getVariables() {
        final List<Variable> variables = netcdfFile.getVariables();

        final Variable timeVariable = netcdfFile.findVariable("time");
        variables.remove(timeVariable);

        return variables;
    }

    @Override
    public com.bc.fiduceo.core.Dimension getProductSize() {
        final Variable ch1 = netcdfFile.findVariable("ch1");
        final int[] shape = ch1.getShape();
        return new com.bc.fiduceo.core.Dimension("ch1", shape[2], shape[1]);
    }

    private Geometries calculateGeometries() throws IOException {
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");
        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, false);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2016-03-02
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(40, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private Number getFillValue(String variableName) throws IOException {
        final Number fillValue = arrayCache.getNumberAttributeValue("_FillValue", variableName);
        if (fillValue != null) {
            return fillValue;
        }
        final Array array = arrayCache.get(variableName);
        return NetCDFUtils.getDefaultFillValue(array);
    }

    private double getOffset(String variableName) throws IOException {
        final Number offsetValue = arrayCache.getNumberAttributeValue("add_offset", variableName);
        if (offsetValue != null) {
            return offsetValue.doubleValue();
        }
        return 0.0;
    }

    private double getScaleFactor(String variableName) throws IOException {
        final Number scaleFactorValue = arrayCache.getNumberAttributeValue("scale_factor", variableName);
        if (scaleFactorValue != null) {
            return scaleFactorValue.doubleValue();
        }
        return 1.0;
    }

    // package access for testing only tb 2016-03-02
    static Date parseDate(String timeString) throws IOException {
        if (StringUtils.isNullOrEmpty(timeString)) {
            throw new IOException("required global attribute '" + START_TIME_ATTRIBUTE_NAME + "' contains no data");
        }
        return TimeUtils.parse(timeString, "yyyyMMdd'T'HHmmss'Z'");
    }

    static ArrayInt.D2 convertToAcquisitionTime(Array rawData, long startTimeMilliSecondsSince1970, float fillValue) {
        final int rank = rawData.getRank();

        if (rank == 0) {
            final ArrayInt.D2 times = new ArrayInt.D2(1, 1);
            final float seconds = rawData.getFloat(0);
            final int secondsSince1970 = getSecondsSince1970(startTimeMilliSecondsSince1970, seconds);
            times.set(0, 0, secondsSince1970);
            return times;
        }

        final int[] shape = rawData.getShape();
        int height = shape[0];
        int width = shape[1];
        final ArrayInt.D2 times = new ArrayInt.D2(height, width);

        final int timesFillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final Index index = rawData.getIndex();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index.set(i, j);
                final float seconds = rawData.getFloat(index);
                if (seconds != fillValue) {
                    final int secondsSince1970 = getSecondsSince1970(startTimeMilliSecondsSince1970, seconds);
                    times.set(i, j, secondsSince1970);
                } else {
                    times.set(i, j, timesFillValue);
                }
            }
        }
        return times;
    }

    static int getSecondsSince1970(long startTimeMilliSecondsSince1970, float seconds) {
        final float milliSeconds = seconds * 1000.f;
        return (int) Math.round(((double) milliSeconds + startTimeMilliSecondsSince1970) * 0.001);
    }

    // package access for testing only tb 2016-03-31
    static int getProductWidth(NetcdfFile netcdfFile) {
        final List<Dimension> dimensions = netcdfFile.getDimensions();
        for (final Dimension dimension : dimensions) {
            if ("ni".equalsIgnoreCase(dimension.getFullName())) {
                return dimension.getLength();
            }
        }
        throw new RuntimeException("missing dimension 'ni'");
    }
}
