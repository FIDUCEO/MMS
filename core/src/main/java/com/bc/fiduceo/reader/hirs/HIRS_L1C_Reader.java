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

package com.bc.fiduceo.reader.hirs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
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
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class HIRS_L1C_Reader implements Reader {

    private static final int CHANNEL_DIMENSION_INDEX = 2;
    private static final int NUM_BT_CHANNELS = 19;
    private static final int NUM_RADIANCE_CHANNELS = 20;
    private static final Interval INTERVAL = new Interval(4, 10);
    private static final NumberFormat CHANNEL_INDEX_FORMAT = new DecimalFormat("00");

    private final GeometryFactory geometryFactory;
    private final HashMap<String, Number> fillValueCache;
    private NetcdfFile netcdfFile;
    private PixelLocator pixelLocator;
    private ArrayCache arrayCache;

    HIRS_L1C_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        fillValueCache = new HashMap<>();
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
    }

    @Override
    public void close() throws IOException {
        arrayCache = null;
        fillValueCache.clear();
        pixelLocator = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = calculateGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "(\\w*.)?[A-Z]{3}.HIRX.[A-Z0-9]{2}.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.[A-Z]{2}.nc";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array lonArray = arrayCache.get("lon");
            final Array latArray = arrayCache.get("lat");

            final int[] shape = lonArray.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(NetCDFUtils.toFloat(lonArray), NetCDFUtils.toFloat(latArray), width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        final Array lonArray = arrayCache.get("lon");
        final int[] shape = lonArray.getShape();
        final int height = shape[0];
        final int width = shape[1];
        final int subsetHeight = getBoundingPolygonCreator().getSubsetHeight(height, 2);
        final PixelLocator pixelLocator = getPixelLocator();

        return PixelLocatorFactory.getSubScenePixelLocator(sceneGeometry, width, height, subsetHeight, pixelLocator);
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array timeArray = arrayCache.get("time");
        return new HIRS_TimeLocator(timeArray);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String[] strings = fileName.split("\\.");
        final String datePart = strings[4].substring(1);
        final Date yyDDD = TimeUtils.parse(datePart, "yyDDD");
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(yyDDD);
        return new int[]{
                    utcCalendar.get(Calendar.YEAR),
                    utcCalendar.get(Calendar.MONTH) + 1,
                    utcCalendar.get(Calendar.DAY_OF_MONTH),
                    };
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        if (variableName.equals("scanpos")) {
            return readScanPos(centerX, interval);
        }
        final String fullVariableName = ReaderUtils.stripChannelSuffix(variableName);

        Array array = arrayCache.get(fullVariableName);
        final int rank = array.getRank();

        if (rank == 3) {
            final int channelIndex = ReaderUtils.getChannelIndex(variableName);
            final int[] shape = array.getShape();
            shape[2] = 1;   // we only want one z-layer
            final int[] offsets = {0, 0, channelIndex};
            array = array.section(offsets, shape);
        }

        final Number fillValue = getFillValue(fullVariableName);

        final Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, array, productSize.getNx());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);   // all variables are already scaled tb 2016-08-03
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int centerX, int centerY, Interval interval) throws IOException, InvalidRangeException {
        final Array timeArray = arrayCache.get("time");
        final Number fillValue = NetCDFUtils.getDefaultFillValue(timeArray);

        final Dimension productSize = getProductSize();
        return (ArrayInt.D2) RawDataReader.read(centerX, centerY, interval, fillValue, timeArray, productSize.getNx());
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> result = new ArrayList<>();
        final List<Variable> fileVariables = netcdfFile.getVariables();
        for (final Variable variable : fileVariables) {
            final String variableName = variable.getFullName();
            if (variableName.equals("bt")) {
                addLayered3DVariables(result, variable, NUM_BT_CHANNELS);
            } else if (variableName.equals("radiance")) {
                addLayered3DVariables(result, variable, NUM_RADIANCE_CHANNELS);
            } else if (variableName.equals("counts")) {
                addLayered3DVariables(result, variable, NUM_RADIANCE_CHANNELS);
            } else {
                result.add(variable);
            }
        }
        return result;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array lon = arrayCache.get("lon");
        final int[] shape = lon.getShape();
        return new Dimension("lon", shape[1], shape[0]);
    }

    private Number getFillValue(String fullVariableName) {
        if (!fillValueCache.containsKey(fullVariableName)) {
            final Variable variable = NetCDFUtils.getVariable(netcdfFile, fullVariableName);
            fillValueCache.put(fullVariableName, NetCDFUtils.getFillValue(variable));
        }
        return fillValueCache.get(fullVariableName);
    }

    private void addLayered3DVariables(List<Variable> result, Variable variable, int numChannels) throws InvalidRangeException {
        final String variableName = variable.getFullName();
        final int[] shape = variable.getShape();
        shape[CHANNEL_DIMENSION_INDEX] = 1;
        final int[] origin = {0, 0, 0};

        final String variableBaseName = variableName + "_ch";
        for (int channel = 0; channel < numChannels; channel++) {
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String channelVariableName = variableBaseName + CHANNEL_INDEX_FORMAT.format(channel + 1);
            channelVariable.setName(channelVariableName);
            result.add(channelVariable);
            origin[CHANNEL_DIMENSION_INDEX]++;
        }
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array time = arrayCache.get("time");
        final long startMillisSince1970 = time.getLong(0) * 1000;
        final Date sensingStart = TimeUtils.create(startMillisSince1970);
        acquisitionInfo.setSensingStart(sensingStart);

        final int[] shape = time.getShape();
        final long stopMillisSince1970 = time.getLong(shape[0] - 1) * 1000;
        final Date sensingStop = TimeUtils.create(stopMillisSince1970);
        acquisitionInfo.setSensingStop(sensingStop);
    }

    private Geometries calculateGeometries() throws IOException {
        final Geometries geometries = new Geometries();
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Array lon = arrayCache.get("lon");
        final Array lat = arrayCache.get("lat");

        Geometry timeAxisGeometry;
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lon, lat);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(lon, lat, 2, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(lon, lat, 2);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lon, lat);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        return new BoundingPolygonCreator(INTERVAL, geometryFactory);
    }

    private Array readScanPos(int centerX, Interval interval) throws IOException {
        final Array scanpos = arrayCache.get("scanpos");
        final int originalWidth = scanpos.getShape()[0];
        final Number fillValue = NetCDFUtils.getDefaultFillValue(scanpos);
        final int width = interval.getX();
        final int height = interval.getY();

        final int[] shape = new int[2];
        shape[0] = height;
        shape[1] = width;
        final Array result = Array.factory(scanpos.getElementType(), shape);

        int originalX = centerX - width / 2;

        final Index index = result.getIndex();
        for (int x = 0; x < width; x++) {
            int value = fillValue.intValue();
            if (originalX >= 0 && originalX < originalWidth) {
                value = scanpos.getInt(originalX);
            }
            for (int y = 0; y < height; y++) {
                index.set(y, x);
                result.setInt(index, value);
            }
            originalX++;
        }

        return result;
    }
}
