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
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_YearDoyMs;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.ensureFillValue;

class SSMT2_Reader extends NetCDFReader {

    private static final int NUM_SPLITS = 2;

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

    private BoundingPolygonCreator boundingPolygonCreator;
    private boolean needVariablesInitialisation = true;
    private ArrayList<Variable> variablesList;
    private HashMap<String, WindowReader> readersMap;
    private PixelLocator pixelLocator;
    private TimeLocator_YearDoyMs timeLocator;

    SSMT2_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void close() throws IOException {
        needVariablesInitialisation = true;
        readersMap = null;
        variablesList = null;
        pixelLocator = null;
        timeLocator = null;

        super.close();
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
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
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
            final Variable ancil_data = netcdfFile.findVariable("ancil_data");
            final int numTimeSteps = NetCDFUtils.getDimensionLength("time_step", netcdfFile);
            final int[] yearOrigin = {0, 0};
            final int[] doyOrigin = {0, 1};
            final int[] milliesOrigin = {0, 2};
            final int[] shape = {numTimeSteps, 1};
            try {
                final Array yearPerScanline = ancil_data.read(yearOrigin, shape).reduce();
                final Array doyPerScanline = ancil_data.read(doyOrigin, shape).reduce();
                final Array secondsPerScanline = ancil_data.read(milliesOrigin, shape).reduce();
                final Array milliesPerScanline = MAMath.convert2Unpacked(secondsPerScanline, new MAMath.ScaleOffset(1000, 0));
                timeLocator = new TimeLocator_YearDoyMs(yearPerScanline, doyPerScanline, milliesPerScanline);
            } catch (InvalidRangeException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String dateStrings = fileName.substring(3, 11);

        final Date date = TimeUtils.parse(dateStrings, "yyyyMMdd");
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(date);
        return new int[]{
                utcCalendar.get(Calendar.YEAR),
                utcCalendar.get(Calendar.MONTH) + 1,
                utcCalendar.get(Calendar.DAY_OF_MONTH),
        };
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
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException {
        final Dimension productSize = getProductSize();
        final int maxX = productSize.getNx() - 1;
        final int maxY = productSize.getNy() - 1;
        final int height = interval.getY();
        final int width = interval.getX();
        final ArrayInt.D2 arrayInt = new ArrayInt.D2(height, width, false);
        final TimeLocator timeLocator = getTimeLocator();
        final int originX = x - width / 2;
        final int originY = y - height / 2;
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        for (int ya = 0; ya < height; ya++) {
            for (int xa = 0; xa < width; xa++) {
                final int productX = xa + originX;
                final int productY = ya + originY;
                final int value;
                if (productX < 0 || productX > maxX || productY < 0 || productY > maxY) {
                    value = fillValue;
                } else {
                    final long milliesSince1970 = timeLocator.getTimeFor(productX, productY);
                    final long secondsSince1970 = milliesSince1970 / 1000;
                    value = Math.toIntExact(secondsSince1970);
                }
                arrayInt.set(ya, xa, value);
            }
        }
        return arrayInt;
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

    HashMap<String, WindowReader> getReadersMap() throws InvalidRangeException {
        ensureInitialisation();
        return readersMap;
    }

    private void splitAndCollect2DVariables(Variable variable) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        final int lenX = getNumX();
        final int chanels = getNumChannels();
        final String shortName = variable.getShortName();

        if (shape[1] == lenX) {
            variablesList.add(variable);
            final Number fillValue = variable.findAttribute(CF_FILL_VALUE_NAME).getNumericValue();
            readersMap.put(shortName, new Read2dFrom2d(arrayCache, shortName, lenX, fillValue));
        } else if (shape[1] == chanels) {
            collect1dChannelsFrom2dVariable(variable);
        }
    }

    private void splitAndCollect3DVariables(Variable variable) throws InvalidRangeException {
        final int scanIdx = variable.findDimensionIndex(DIM_NAME_SCAN_POSITION);
        final int channelIdx = variable.findDimensionIndex(DIM_NAME_CHANNEL);
        final int calibIdx = variable.findDimensionIndex(DIM_NAME_CALIBRATION_NUMBER);

        if (scanIdx == 1 && channelIdx == 2) {
            collect2dChannelsFrom3dVariable(variable);
        } else if (channelIdx == 1 && calibIdx == 2) {
            collect1dCalibrationsPerChannelFrom3dVariable(variable);
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

        final int height = getNumY();
        final int width = getNumX();

        final List<Variable> variables = netcdfFile.getVariables();
        for (Variable variable : variables) {
            String shortName = variable.getShortName();
            int[] shape = variable.getShape();
            if (shape[0] != height
                    || shape.length > 3) {
                continue;
            }

            ensureFillValue(variable);

            if (shortName.equalsIgnoreCase("ancil_data")) {
                collectAncilDataVariables(variable);
            } else if (shortName.equalsIgnoreCase("Temperature_misc_housekeeping")) {
                collectHousekeepingVariables(variable);
            } else if (shape.length == 3) {
                splitAndCollect3DVariables(variable);
            } else if (shape.length == 2) {
                splitAndCollect2DVariables(variable);
            } else {
                variablesList.add(variable);
                readersMap.put(shortName, new Read2dFrom1d(arrayCache, shortName, width));
            }
        }

        addZenithAngleVariable(height, width);

        needVariablesInitialisation = false;
    }

    private void addZenithAngleVariable(int height, int width) {
        ZenithAngleVariable.SensorType sensorType = getSensorType();
        final ZenithAngleVariable zenithVariable = new ZenithAngleVariable(sensorType, height);
        variablesList.add(zenithVariable);
        arrayCache.inject(zenithVariable);
        final String shortName = zenithVariable.getShortName();
        readersMap.put(shortName, new Read2dFrom2d(arrayCache, shortName, width, getFillValue(zenithVariable)));
    }

    private Number getFillValue(ZenithAngleVariable zenithVariable) {
        return zenithVariable.findAttribute(NetCDFUtils.CF_FILL_VALUE_NAME).getNumericValue();
    }

    private ZenithAngleVariable.SensorType getSensorType() {
        final String spacecraftId = NetCDFUtils.getGlobalAttributeString("spacecraft_ID", netcdfFile);
        return ZenithAngleVariable.SensorType.fromString(spacecraftId);
    }

    private int getNumX() {
        return NetCDFUtils.getDimensionLength(DIM_NAME_SCAN_POSITION, netcdfFile);
    }

    private int getNumY() {
        return NetCDFUtils.getDimensionLength(DIM_NAME_TIME_STEP, netcdfFile);
    }

    private int getNumChannels() {
        return NetCDFUtils.getDimensionLength(DIM_NAME_CHANNEL, netcdfFile);
    }

    private int getNumCalibrations() {
        return NetCDFUtils.getDimensionLength(DIM_NAME_CALIBRATION_NUMBER, netcdfFile);
    }

    private void collect2dChannelsFrom3dVariable(Variable variable) throws InvalidRangeException {
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
            ensureFillValue(channelVariable);
            variablesList.add(channelVariable);
            final String[] offsetMapping = {"y", "x", "" + channel};
            readersMap.put(channelName, new Read2dFrom3d(() -> arrayCache.get(shortName), offsetMapping, fillValue));
        }
    }

    private void collectHousekeepingVariables(Variable variable) throws InvalidRangeException {
        final String baseName = variable.getShortName();
        final ucar.nc2.Dimension dimension = variable.getDimension(1);
        final int numHousekeeping = dimension.getLength();
        final int[] origin = {0, 0};
        final int[] shape = variable.getShape();
        shape[1] = 1;

        for (int i = 0; i < numHousekeeping; i++) {
            origin[1] = i;
            final Section section = new Section(origin, shape);
            final Variable housekeepingVariable = variable.section(section);
            final String vName = baseName + "_thermistorcount" + String.format("%02d", i + 1);
            housekeepingVariable.setName(vName);
            ensureFillValue(housekeepingVariable);
            variablesList.add(housekeepingVariable);
            readersMap.put(vName, new Read1dFrom2d(arrayCache, baseName, i));
        }
    }

    private void collectAncilDataVariables(Variable variable) throws InvalidRangeException {
        final String baseName = variable.getShortName();
        final ucar.nc2.Dimension dimension = variable.getDimension(1);
        final int numAncilData = dimension.getLength();
        final String dimName = dimension.getShortName();
        final String substring = dimName.substring(dimName.indexOf(":") + 1);
        final String[] names = substring.split("_");
        for (int i = 0; i < names.length; i++) {
            String name1 = names[i];
            for (int j = i + 1; j < names.length; j++) {
                String name2 = names[j];
                if (name1.equals(name2)) {
                    names[i] = name1 + "_1";
                    names[j] = name1 + "_2";
                }
            }
            names[i] = baseName + "_" + names[i];
        }
        final int[] origin = {0, 0};
        final int[] shape = variable.getShape();
        shape[1] = 1;

        for (int i = 0; i < numAncilData; i++) {
            origin[1] = i;
            final Section section = new Section(origin, shape);
            final Variable ancilVariable = variable.section(section);
            final String vName = names[i];
            ancilVariable.setName(vName);
            variablesList.add(ancilVariable);
            readersMap.put(vName, new Read1dFrom2d(arrayCache, baseName, i));
        }
    }

    private void collect1dChannelsFrom2dVariable(Variable variable) throws InvalidRangeException {
        final int channels = getNumChannels();
        final int[] origin = {0, 0};
        final int[] shape = variable.getShape();
        shape[1] = 1;
        final String channelPrefix = "_ch";
        final String baseName = variable.getShortName();

        for (int channel = 0; channel < channels; channel++) {
            origin[1] = channel;
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final String shortName = baseName + channelPrefix + (channel + 1);
            channelVariable.setName(shortName);
            variablesList.add(channelVariable);
            readersMap.put(shortName, new Read1dFrom2d(arrayCache, baseName, channel));
        }
    }

    private void collect1dCalibrationsPerChannelFrom3dVariable(Variable variable) throws InvalidRangeException {
        final int chanels = getNumChannels();
        final int calibrations = getNumCalibrations();
        final int[] origin = {0, 0, 0};
        final int[] shape = variable.getShape();
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
                final Variable channelCalibVariable = variable.section(section);
                final String varName = baseName + channelPrefix + (channel + 1) + calibPrefix + (calib + 1);
                channelCalibVariable.setName(varName);
                ensureFillValue(channelCalibVariable);
                variablesList.add(channelCalibVariable);
                final String[] offsetMapping = {"y", String.valueOf(channel), String.valueOf(calib)};
                readersMap.put(varName, new Read1dFrom3dAndExpandTo2d(() -> arrayCache.get(baseName), offsetMapping, fillValue));
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

    private void setNodeType(AcquisitionInfo acquisitionInfo) {
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
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
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
        private final Dimension productSize;
        private ArrayCache arrayCache;
        private Array dataArray;
        private Number fillValue;
        private boolean needData = true;

        Read2dFrom1d(ArrayCache arrayCache, String shortName, int defaultWidth) {
            this.shortName = shortName;
            this.productSize = new Dimension("size", defaultWidth, 0);
            this.arrayCache = arrayCache;
        }

        @Override
        public Array read(int centerX, int centerY, Interval interval) throws IOException {
            if (needData) {
                initData();
            }
            return RawDataReader.read(centerX, centerY, interval, fillValue, dataArray, productSize);
        }

        void initData() throws IOException {
            dataArray = arrayCache.get(shortName);
            fillValue = NetCDFUtils.getDefaultFillValue(dataArray);
            needData = false;
        }
    }

    private static class Read2dFrom2d extends WindowReader {

        private final Dimension productSize;
        private final String shortName;
        private final Number fillValue;
        private final ArrayCache arrayCache;

        Read2dFrom2d(ArrayCache arrayCache, String shortName, int defaultWidth, Number fillValue) {
            this.arrayCache = arrayCache;
            this.productSize = new Dimension("size", defaultWidth, 0);
            this.shortName = shortName;
            this.fillValue = fillValue;
        }

        @Override
        public Array read(int centerX, int centerY, Interval interval) throws IOException {
            return RawDataReader.read(centerX, centerY, interval, fillValue, arrayCache.get(shortName), productSize);
        }
    }

    private static class Read1dFrom2d extends WindowReader {

        private final String shortName;
        private final int sourceChannel;
        private ArrayCache arrayCache;
        private Array dataArray;
        private double fillValue;
        private boolean needData;

        Read1dFrom2d(ArrayCache arrayCache, String shortName, int sourceChannel) {
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
            final int srcHeight = sourceShape[0];
            final int srcWidth = sourceShape[1];
            fillArray(offsetX, offsetY,
                    targetWidth, targetHeight,
                    srcWidth, srcHeight,
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
