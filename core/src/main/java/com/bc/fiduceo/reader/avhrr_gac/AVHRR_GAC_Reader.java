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
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.*;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;

public class AVHRR_GAC_Reader extends NetCDFReader {

    private static final int NUM_SPLITS = 2;
    private static final String START_TIME_ATTRIBUTE_NAME = "start_time";
    private static final String STOP_TIME_ATTRIBUTE_NAME = "stop_time";

    private final GeometryFactory geometryFactory;

    private BoundingPolygonCreator boundingPolygonCreator;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;
    private long startTimeMilliSecondsSince1970;

    AVHRR_GAC_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);

        final String startTimeString = NetCDFUtils.getGlobalAttributeString(START_TIME_ATTRIBUTE_NAME, netcdfFile);
        startTimeMilliSecondsSince1970 = parseDate(startTimeString).getTime();

        timeLocator = null;
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        pixelLocator = null;
        boundingPolygonCreator = null;
        super.close();
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
        if (geometries.getIntervals().length > 1) {
            setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometries.getIntervals());
        } else {
            ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
        }

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-(v[0-9].[0-9]-)?fv\\d\\d.\\d.nc";
    }

    @Override
    public String getLongitudeVariableName() {
        return null;
    }

    @Override
    public String getLatitudeVariableName() {
        return null;
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
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String yearString = fileName.substring(0, 4);
        final String monthString = fileName.substring(4, 6);
        final String dayString = fileName.substring(6, 8);

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(yearString);
        ymd[1] = Integer.parseInt(monthString);
        ymd[2] = Integer.parseInt(dayString);
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws InvalidRangeException, IOException {
        final Array rawArray = arrayCache.get(variableName);
        final Number fillValue = getFillValue(variableName);

        final com.bc.fiduceo.core.Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawArray, productSize);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array array = readRaw(centerX, centerY, interval, variableName);

        final double scaleFactor = getScaleFactorCf(variableName);
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

        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");
        final double fillValue = arrayCache.getNumberAttributeValue(CF_FILL_VALUE_NAME, "lon").doubleValue();
        final Interval[] intervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        if (intervals.length == 1) {
            return calculateGeometriesSmooth(boundingPolygonCreator, longitudes, latitudes);
        } else {
            return calculateGeometriesGapped(boundingPolygonCreator, longitudes, latitudes, intervals);
        }
    }

    // @todo 1 tb/b refactor, duplicated code 2019-06-25
    private void setTimeAxes(AcquisitionInfo acquisitionInfo, Geometry timeAxesGeometry, Interval[] intervals) throws IOException {
        final TimeLocator timeLocator = getTimeLocator();

        final GeometryCollection axesCollection = (GeometryCollection) timeAxesGeometry;
        final Geometry[] axesGeometries = axesCollection.getGeometries();
        final TimeAxis[] timeAxes = new TimeAxis[axesGeometries.length];

        int axesIdx = 0;
        for(final Interval interval: intervals) {
            final long intervalStart = timeLocator.getTimeFor(0, interval.getX());
            final long intervalStop = timeLocator.getTimeFor(0, interval.getY());

            timeAxes[axesIdx] = geometryFactory.createTimeAxis((LineString) axesGeometries[axesIdx], TimeUtils.create(intervalStart), TimeUtils.create(intervalStop));
            axesIdx++;
        }

        acquisitionInfo.setTimeAxes(timeAxes);
    }

    // @todo 1 tb/b refactor, duplicated code 2019-06-25
    private Geometries calculateGeometriesGapped(BoundingPolygonCreator boundingPolygonCreator, Array longitudes, Array latitudes, Interval[] intervals) throws IOException {
        final Geometries geometries = new Geometries();
        final List<Polygon> geometryList = new ArrayList<>();
        final List<LineString> timeAxesList = new ArrayList<>();

        final int[] shape = longitudes.getShape();

        final int[] offset = new int[2];
        final int[] subsetShape = new int[2];
        subsetShape[1] = shape[1];
        try {
            for (final Interval interval : intervals) {
                offset[0] = interval.getX();
                subsetShape[0] = interval.getY() - interval.getX() + 1;

                final Array lonSection = longitudes.section(offset, subsetShape);
                final Array latSection = latitudes.section(offset, subsetShape);

                final Polygon boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonSection, latSection);

                geometryList.add(boundingGeometry);
                final LineString timeAxis = boundingPolygonCreator.createTimeAxisGeometry(lonSection, latSection);
                timeAxesList.add(timeAxis);
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }

        final MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(geometryList);
        geometries.setBoundingGeometry(multiPolygon);
        final GeometryCollection timeAxes = geometryFactory.createGeometryCollection(timeAxesList.toArray(new Geometry[]{}));
        geometries.setTimeAxesGeometry(timeAxes);

        return geometries;
    }

    private Geometries calculateGeometriesSmooth(BoundingPolygonCreator boundingPolygonCreator, Array longitudes, Array latitudes) {
        final Geometries geometries = new Geometries();

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
}
