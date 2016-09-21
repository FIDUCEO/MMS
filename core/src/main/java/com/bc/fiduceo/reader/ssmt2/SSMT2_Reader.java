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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dByte;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dDouble;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dFloat;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dInt;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dLong;
import com.bc.fiduceo.reader.window_reader.Read1dFrom3dAndExpandTo2dShort;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dByte;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dDouble;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dFloat;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dInt;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dLong;
import com.bc.fiduceo.reader.window_reader.Read2dFrom3dShort;
import com.bc.fiduceo.reader.window_reader.WindowReader;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SSMT2_Reader implements Reader {

    private static final String DIM_NAME_TIME_STEP = "time_step";
    private static final String DIM_NAME_SCAN_POSITION = "scan_position";
    private static final String DIM_NAME_CHANNEL = "channel";
    private static final String DIM_NAME_CALIBRATION_NUMBER = "calib_number";

    private static final String START_DATE_UTC_NAME = "start_date_UTC";
    private static final String START_TIME_UTC_NAME = "start_time_UTC";
    private static final String END_DATE_UTC_NAME = "end_date_UTC";
    private static final String END_TIME_UTC_NAME = "end_time_UTC";
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String REG_EX = "F(11|12|14|15)[0-9]{12}.nc";

    private final GeometryFactory geometryFactory;

    private NetcdfFile netcdfFile;
    private ArrayCache arrayCache;
    private BoundingPolygonCreator boundingPolygonCreator;
    private boolean needVariablesInitialisation = true;
    private ArrayList<Variable> variablesList;
    private HashMap<String, WindowReader> readersMap;

    public SSMT2_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        setSensingTimes(acquisitionInfo);
        setNodeType(acquisitionInfo);
        setGeometries(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        ensureInitialisation();
        return readersMap.get(variableName).read(centerX, centerY, interval);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        ensureInitialisation();
        return readersMap.get(variableName).read(centerX, centerY, interval);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        ensureInitialisation();
        return variablesList;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array lonArray = arrayCache.get("lon");
        final int[] shape = lonArray.getShape();
        return new Dimension("lon", shape[1], shape[0]);
    }

    // package access for testing only tb 2016-09-09
    static String assembleDateString(String startDateString, String startTimeString) {
        return startDateString + "T" + startTimeString.substring(0, startTimeString.length() - 7);
    }

    private void splitAndCollect2DVariables(ArrayList<Variable> result, Variable variable) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        final int lenX = getNumX();
        final int chanels = getNumChannels();
        final String shortName = variable.getShortName();

        if (shape[1] == lenX) {
            result.add(variable);
            readersMap.put(shortName, new Read2dFrom2d(arrayCache, lenX, shortName));
        } else if (shape[1] == chanels) {
            collect1dChannelsFrom2dVariable(variable, result);
        }
    }

    private void splitAndCollect3DVariables(ArrayList<Variable> result, Variable variable) throws InvalidRangeException {
        final int scanIdx = variable.findDimensionIndex(DIM_NAME_SCAN_POSITION);
        final int channelIdx = variable.findDimensionIndex(DIM_NAME_CHANNEL);
        final int calibIdx = variable.findDimensionIndex(DIM_NAME_CALIBRATION_NUMBER);

        if (scanIdx == 1 && channelIdx == 2) {
            collect2dChannelsFrom3dVariable(variable, result);
        } else if (channelIdx == 1 && calibIdx == 2) {
            collect1dCalibrationsPerChannelFrom3dVariable(variable, result);
        } else {
            throw new RuntimeException("State scanIndex = " + scanIdx + ", channelIndex = " + channelIdx + ", calibrationIndex = " + calibIdx + " is not supported.");
        }
    }

    private void ensureInitialisation() throws InvalidRangeException {
        if (needVariablesInitialisation) {
            initializeVariables();
        }
    }

    private void initializeVariables() throws InvalidRangeException {
        variablesList = new ArrayList<>();
        readersMap = new HashMap<>();

        final int lenY = getNumY();
        final int lenX = getNumX();

        final List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            String shortName = variable.getShortName();
            if (shortName.equalsIgnoreCase("ancil_data")
                || shortName.equalsIgnoreCase("Temperature_misc_housekeeping")) {
                continue;
            }
            int[] shape = variable.getShape();
            if (shape[0] != lenY
                || shape.length > 3) {
                continue;
            }
            if (shape.length == 3) {
                splitAndCollect3DVariables(this.variablesList, variable);
            } else if (shape.length == 2) {
                splitAndCollect2DVariables(this.variablesList, variable);
            } else {
                this.variablesList.add(variable);
                readersMap.put(shortName, new Read2dFrom1d(arrayCache, shortName, lenX));
            }
        }
        needVariablesInitialisation = false;
    }

    private int getNumX() {
        return getDimLen(DIM_NAME_SCAN_POSITION);
    }

    private int getNumY() {
        return getDimLen(DIM_NAME_TIME_STEP);
    }

    private int getNumChannels() {
        return getDimLen(DIM_NAME_CHANNEL);
    }

    private int getNumCalibrations() {
        return getDimLen(DIM_NAME_CALIBRATION_NUMBER);
    }

    private int getDimLen(String name) {
        return netcdfFile.findDimension(name).getLength();
    }

    private void collect2dChannelsFrom3dVariable(Variable variable, ArrayList<Variable> result) throws InvalidRangeException {
        final int[] origin = {0, 0, 0};
        final int[] shape = variable.getShape();
        final int chanels = shape[2];
        shape[2] = 1;
        final String shortName = variable.getShortName();
        String baseName = shortName + "_ch";

        final DataType dataType = variable.getDataType();
        final Number fillValue = NetCDFUtils.getDefaultFillValue(dataType.getPrimitiveClassType());

        for (int channel = 0; channel < chanels; channel++) {
            origin[2] = channel;
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String channelName = baseName + (channel + 1);
            channelVariable.setName(channelName);
            result.add(channelVariable);
            final String[] offsetMapping = {"y", "x", "" + channel};
            if (DataType.DOUBLE.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dDouble(() -> (ArrayDouble.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else if (DataType.FLOAT.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dFloat(() -> (ArrayFloat.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else if (DataType.LONG.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dLong(() -> (ArrayLong.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else if (DataType.INT.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dInt(() -> (ArrayInt.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else if (DataType.SHORT.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dShort(() -> (ArrayShort.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else if (DataType.BYTE.equals(dataType)) {
                readersMap.put(channelName, new Read2dFrom3dByte(() -> (ArrayByte.D3) arrayCache.get(shortName), offsetMapping, fillValue));
            } else {
                throw new RuntimeException("Data type '" + dataType.name() + "' not supported");
            }
        }
    }

    private void collect1dChannelsFrom2dVariable(Variable variable, ArrayList<Variable> result) throws InvalidRangeException {
        final int[] origin = {0, 0};
        final int[] shape = variable.getShape();
        final int chanels = shape[1];
        shape[1] = 1;
        final String channelPrefix = "_ch";
        final String baseName = variable.getShortName();

        for (int channel = 0; channel < chanels; channel++) {
            origin[1] = channel;
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String shortName = baseName + channelPrefix + (channel + 1);
            channelVariable.setName(shortName);
            result.add(channelVariable);
            readersMap.put(shortName, new Read1dFrom2d(arrayCache, baseName, channel));
        }
    }

    private void collect1dCalibrationsPerChannelFrom3dVariable(Variable variable, ArrayList<Variable> result) throws InvalidRangeException {
        final int[] origin = {0, 0, 0};
        final int[] shape = variable.getShape();
        final int chanels = shape[1];
        final int calibrations = shape[2];
        shape[1] = 1;
        shape[2] = 1;
        final String channelPrefix = "_ch";
        final String calibPrefix = "_cal";
        final String baseName = variable.getShortName();
        final DataType dataType = variable.getDataType();
        final Number fillValue = NetCDFUtils.getDefaultFillValue(dataType.getPrimitiveClassType());

        for (int channel = 0; channel < chanels; channel++) {
            for (int calib = 0; calib < calibrations; calib++) {
                origin[1] = channel;
                origin[2] = calib;
                final Section section = new Section(origin, shape);
                final Variable channelVariable = variable.section(section);
                final String varName = baseName + channelPrefix + (channel + 1) + calibPrefix + (calib + 1);
                channelVariable.setName(varName);
                result.add(channelVariable);
                final String[] offsetMapping = {"y", "" + channel, "" + calib};
                if (DataType.DOUBLE.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dDouble(() -> (ArrayDouble.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else if (DataType.FLOAT.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dFloat(() -> (ArrayFloat.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else if (DataType.LONG.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dLong(() -> (ArrayLong.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else if (DataType.INT.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dInt(() -> (ArrayInt.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else if (DataType.SHORT.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dShort(() -> (ArrayShort.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else if (DataType.BYTE.equals(dataType)) {
                    readersMap.put(varName, new Read1dFrom3dAndExpandTo2dByte(() -> (ArrayByte.D3) arrayCache.get(baseName), offsetMapping, fillValue));
                } else {
                    throw new RuntimeException("Data type '" + dataType.name() + "' not supported");
                }
            }
        }
    }


    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final String startDateString = NetCDFUtils.getGlobalAttributeString(START_DATE_UTC_NAME, netcdfFile);
        final String startTimeString = NetCDFUtils.getGlobalAttributeString(START_TIME_UTC_NAME, netcdfFile);
        final String endDateString = NetCDFUtils.getGlobalAttributeString(END_DATE_UTC_NAME, netcdfFile);
        final String endTimeString = NetCDFUtils.getGlobalAttributeString(END_TIME_UTC_NAME, netcdfFile);
        try {
            final String startUTCString = assembleDateString(startDateString, startTimeString);
            final ProductData.UTC startDateUTC = ProductData.UTC.parse(startUTCString, DATE_PATTERN);
            acquisitionInfo.setSensingStart(startDateUTC.getAsDate());

            final String endUTCString = assembleDateString(endDateString, endTimeString);
            final ProductData.UTC endDateUTC = ProductData.UTC.parse(endUTCString, DATE_PATTERN);
            acquisitionInfo.setSensingStop(endDateUTC.getAsDate());
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void setNodeType(AcquisitionInfo acquisitionInfo) throws IOException {
        final String startDirection = NetCDFUtils.getGlobalAttributeString("start_direction", netcdfFile);
        if ("ascending".equals(startDirection)) {
            acquisitionInfo.setNodeType(NodeType.ASCENDING);
        } else if ("descending".equals(startDirection)) {
            acquisitionInfo.setNodeType(NodeType.DESCENDING);
        } else {
            acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        }
    }

    private void setGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array lonArray = arrayCache.get("lon");
        final Array latArray = arrayCache.get("lat");
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(lonArray, latArray);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Must split geometries");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);

        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(lonArray, latArray);

        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2016-03-02
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(5, 25), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private static class Read2dFrom1d extends WindowReader {

        private final String shortName;
        private final int defaultWidth;
        private ArrayCache arrayCache;
        private Array dataArray;
        private Number fillValue;
        private boolean needData = true;

        public Read2dFrom1d(ArrayCache arrayCache, String shortName, int defaultWidth) {
            this.shortName = shortName;
            this.defaultWidth = defaultWidth;
            this.arrayCache = arrayCache;
        }

        @Override
        public Array read(int centerX, int centerY, Interval interval) throws IOException {
            if (needData) {
                initData();
            }
            try {
                return RawDataReader.read(centerX, centerY, interval, fillValue, dataArray, defaultWidth);
            } catch (InvalidRangeException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        void initData() throws IOException {
            dataArray = arrayCache.get(shortName);
            fillValue = NetCDFUtils.getDefaultFillValue(dataArray);
            needData = false;
        }
    }

    private static class Read2dFrom2d extends WindowReader {

        private final int defaultWidth;
        private final String shortName;
        private ArrayCache arrayCache;
        private Array dataArray;
        private Number fillValue;
        private boolean needData = true;

        public Read2dFrom2d(ArrayCache arrayCache, int defaultWidth, String shortName) {
            this.arrayCache = arrayCache;
            this.defaultWidth = defaultWidth;
            this.shortName = shortName;
        }

        @Override
        public Array read(int centerX, int centerY, Interval interval) throws IOException {
            if (needData) {
                initData();
            }
            try {
                return RawDataReader.read(centerX, centerY, interval, fillValue, dataArray, defaultWidth);
            } catch (InvalidRangeException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        void initData() throws IOException {
            dataArray = arrayCache.get(shortName);
            fillValue = NetCDFUtils.getDefaultFillValue(dataArray);
            needData = false;
        }
    }

    private static class Read1dFrom2d extends WindowReader {

        private final String shortName;
        private final int sourceChannel;
        private ArrayCache arrayCache;
        private Array dataArray;
        private double fillValue;
        private boolean needData;

        public Read1dFrom2d(ArrayCache arrayCache, String shortName, int sourceChannel) {
            this.arrayCache = arrayCache;
            this.shortName = shortName;
            this.sourceChannel = sourceChannel;
            needData = true;
        }

        @Override
        public Array read(int centerX, int centerY, Interval interval) throws IOException {
            if (needData) {
                dataArray = arrayCache.get(shortName);
                fillValue = NetCDFUtils.getDefaultFillValue(dataArray).doubleValue();
                needData = false;
            }

            final int targetWidth = interval.getX();
            final int targetHeight = interval.getY();
            final int offsetX = centerX - targetWidth / 2;
            final int offsetY = centerY - targetHeight / 2;
            final Array targetArray = Array.factory(dataArray.getDataType(), new int[]{targetWidth, targetHeight});
            final Index targetIdx = targetArray.getIndex();
            final int[] sourceShape = dataArray.getShape();
            final Index sourceIdx = dataArray.getIndex();
            final int srcWidth = sourceShape[1];
            final int srcHeight = sourceShape[0];
            fillArray(offsetX, offsetY,
                      targetWidth, targetHeight,
                      0, srcHeight,
                      (y, x) -> {
                          targetIdx.set(y, x);
                          targetArray.setDouble(targetIdx, fillValue);
                      },
                      (y, x, yRaw, xRaw) -> {
                          targetIdx.set(y, x);
                          sourceIdx.set(yRaw, sourceChannel);
                          targetArray.setDouble(targetIdx, dataArray.getDouble(sourceIdx));
                      }
            );
            return targetArray;
        }
    }
}
