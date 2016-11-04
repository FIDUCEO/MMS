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

package com.bc.fiduceo.reader.amsre;


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
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.*;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class AMSRE_Reader implements Reader {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String[] CHANNEL_QUALITY_FLAG_EXTENSIONS = new String[]{"6V", "6H", "10V", "10H", "18V", "18H", "23V", "23H", "36V", "36H", "89V", "89H"};
    private static final String LO_RES_SWATH_GEO_GROUP = "Low_Res_Swath/Geolocation_Fields";
    private static final String LO_RES_SWATH_DATA_GROUP = "Low_Res_Swath/Data_Fields";
    private static final String LAND_OCEAN_FLAGS_NAME = "Land_Ocean_Flag_for_6_10_18_23_36_50_89A";
    private static final String CHANNEL_QUALITY_FLAGS_NAME = "Channel_Quality_Flag_6_to_52";
    private static final String REG_EX = "AMSR_E_L2A_BrightnessTemperatures_V\\d{2}_\\d{12}_[A-Z]{1}.hdf";

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;
    private PixelLocator pixelLocator;
    private final VariableNamesConverter namesConverter;

    AMSRE_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        namesConverter = new VariableNamesConverter();
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
        if (pixelLocator == null) {
            final Array latitudes = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Latitude");
            final Array longitudes = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Longitude");

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(longitudes, latitudes, width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array timeArray = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Time");
        return new AMSRE_TimeLocator(timeArray);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        if (variableName.equals("Land_Ocean_Flag_6")) {
            return readLandOceanFlag(centerX, centerY, interval);
        }
        if (variableName.contains("Channel_Quality_Flag_")) {
            return readChannelQualityFlag(variableName, centerX, centerY, interval);
        }

        final String hdfVariableName = namesConverter.toHdf(variableName);
        final String groupName = getGroupNameForVariable(hdfVariableName);
        final Array rawArray = arrayCache.get(groupName, hdfVariableName);
        final Number fillValue = getFillValue(groupName, hdfVariableName);

        final Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawArray, productSize.getNx());
    }

    private Array readChannelQualityFlag(String variableName, int centerX, int centerY, Interval interval) throws IOException, InvalidRangeException {
        final Array rawArray = arrayCache.get(LO_RES_SWATH_DATA_GROUP, CHANNEL_QUALITY_FLAGS_NAME);
        final Number fillValue = getFillValue(LO_RES_SWATH_DATA_GROUP, CHANNEL_QUALITY_FLAGS_NAME);
        final Dimension productSize = getProductSize();

        final int layerIndex = getLayerIndexFromChannelFlagName(variableName);
        final int[] shape = rawArray.getShape();
        shape[1] = 1;
        final int[] origins = {0, layerIndex};
        final Array channelLayer = rawArray.section(origins, shape);
        return RawDataReader.read(centerX, centerY, interval, fillValue, channelLayer, productSize.getNx());
    }

    private Array readLandOceanFlag(int centerX, int centerY, Interval interval) throws IOException, InvalidRangeException {
        final Array rawArray = arrayCache.get(LO_RES_SWATH_DATA_GROUP, LAND_OCEAN_FLAGS_NAME);
        final Number fillValue = getFillValue(LO_RES_SWATH_DATA_GROUP, LAND_OCEAN_FLAGS_NAME);
        final Dimension productSize = getProductSize();

        final int[] shape = rawArray.getShape();
        shape[2] = 1;
        final int[] origins = {0, 0, 0};
        final Array channel6Layer = rawArray.section(origins, shape);
        return RawDataReader.read(centerX, centerY, interval, fillValue, channel6Layer, productSize.getNx());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        Array array = readRaw(centerX, centerY, interval, variableName);
        if (variableName.equals("Land_Ocean_Flag_6") || variableName.contains("Channel_Quality_Flag_")) {
            return array;
        }

        final String hdfVariableName = namesConverter.toHdf(variableName);
        final String groupName = getGroupNameForVariable(hdfVariableName);
        double scaleFactor = getScaleFactor(groupName, hdfVariableName);
        double offset = getOffset(groupName, hdfVariableName);
        if (ReaderUtils.mustScale(scaleFactor, offset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array rawTimeTAI = readRaw(x, y, interval, "Time");
        final Array acquisitionTimeUtc = Array.factory(DataType.INT, rawTimeTAI.getShape());

        for (int i = 0; i < rawTimeTAI.getSize(); i++) {
            final Date utcDate = TimeUtils.tai1993ToUtc(rawTimeTAI.getDouble(i));
            acquisitionTimeUtc.setInt(i, (int) (utcDate.getTime() * 0.001));
        }

        return (ArrayInt.D2) acquisitionTimeUtc;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> variables = new ArrayList<>();

        final Group geoLocationGroup = netcdfFile.findGroup(LO_RES_SWATH_GEO_GROUP);
        final List<Variable> geolocationVariables = geoLocationGroup.getVariables();
        variables.addAll(geolocationVariables);

        final Group dataGroup = netcdfFile.findGroup(LO_RES_SWATH_DATA_GROUP);
        final List<Variable> dataGroupVariables = dataGroup.getVariables();
        for (final Variable variable : dataGroupVariables) {
            final String shortName = variable.getShortName();
            if (shortName.contains("_Res.2_") ||
                    shortName.contains("_Res.3_") ||
                    shortName.contains("_Res.4_") ||
                    shortName.contains("Antenna_Temp_") ||
                    shortName.contains("Data_Quality") ||
                    shortName.contains("SPS_Temperature") ||
                    shortName.contains("Observation_Supplement") ||
                    shortName.contains("Position_in_Orbit") ||
                    shortName.contains("Navigation_Data") ||
                    shortName.contains("Attitude_Data") ||
                    shortName.contains("SPC_Temperature") ||
                    shortName.contains("Interpolation_Flag") ||
                    shortName.contains("Rx_Offset") ||
                    shortName.contains("Cold_Sky_") ||
                    shortName.contains("Hot_Load") ||
                    shortName.contains("(not-resampled)") ||
                    shortName.contains("Resampled_Channel_Quality_Flag") ||
                    shortName.contains("Effective_Cold_Space") ||
                    shortName.contains("Effective_Hot_Load") ||
                    shortName.contains("Res2_Surf") ||
                    shortName.contains("Res3_Surf") ||
                    shortName.contains("Res4_Surf") ||
                    shortName.contains("Sun_Glint") ||
                    shortName.contains("Geostationary")) {
                continue;
            }

            if (shortName.contains("Land_Ocean_Flag")) {
                // this is a three-d dataset where we currently just pick the lowest layer tb 2016-09-05
                addLandOceanChannel6Layer(variables, variable);
                continue;
            }

            if (shortName.contains("Channel_Quality_Flag_")) {
                final int[] shape = variable.getShape();
                shape[1] = 1;   // pick a single layer
                final int[] origin = {0, 0};
                String variableNamePrefix = "Channel_Quality_Flag_";
                for (int i = 0; i < CHANNEL_QUALITY_FLAG_EXTENSIONS.length; i++) {
                    origin[1] = i;
                    final Section section = new Section(origin, shape);
                    final Variable channelVariable = variable.section(section);
                    channelVariable.setName(variableNamePrefix + CHANNEL_QUALITY_FLAG_EXTENSIONS[i]);
                    variables.add(channelVariable);
                }
                continue;
            }

            final String mmsName = namesConverter.toMms(variable.getShortName());
            variable.setName(mmsName);
            variables.add(variable);
        }

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array latitudes = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Latitude");
        final int[] shape = latitudes.getShape();
        return new Dimension("lat", shape[1], shape[0]);
    }

    // Package access for testing only tb 2016-09-02
    static String assembleUTCString(String dateString, String timeString) {
        String startDateString = dateString + "T" + timeString;
        final int lastDotIndex = startDateString.lastIndexOf('.');
        startDateString = startDateString.substring(0, lastDotIndex);
        return startDateString;
    }

    // Package access for testing only tb 2016-09-02
    static ProductData.UTC getUtcData(Attribute rangeBeginningDateAttribute, Attribute rangeBeginningTimeAttribute) throws IOException {
        try {
            final String startDateString = assembleUTCString(rangeBeginningDateAttribute.getStringValue(), rangeBeginningTimeAttribute.getStringValue());
            return ProductData.UTC.parse(startDateString, DATE_PATTERN);
        } catch (ParseException | IndexOutOfBoundsException e) {
            throw new IOException(e.getMessage());
        }
    }

    // Package access for testing only tb 2016-09-02
    static void assignNodeType(AcquisitionInfo acquisitionInfo, String orbitDirection) {
        if ("Ascending".equalsIgnoreCase(orbitDirection)) {
            acquisitionInfo.setNodeType(NodeType.ASCENDING);
        } else if ("Descending".equalsIgnoreCase(orbitDirection)) {
            acquisitionInfo.setNodeType(NodeType.DESCENDING);
        } else {
            acquisitionInfo.setNodeType(NodeType.UNDEFINED);
        }
    }

    // Package access for testing only tb 2016-09-06
    static String getGroupNameForVariable(String variableName) {
        if (variableName.equals("Time") || variableName.equals("Longitude") || variableName.equals("Latitude")) {
            return LO_RES_SWATH_GEO_GROUP;
        }

        return LO_RES_SWATH_DATA_GROUP;
    }

    private void addLandOceanChannel6Layer(List<Variable> variables, Variable variable) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        shape[2] = 1;   // pick channel 6
        final int[] origin = {0, 0, 0};
        final Section section = new Section(origin, shape);
        final Variable channelVariable = variable.section(section);
        channelVariable.setName("Land_Ocean_Flag_6");
        variables.add(channelVariable);
    }

    private Attribute getGlobalAttributeSafe(String attributeName) {
        final Attribute globalAttribute = netcdfFile.findGlobalAttribute(attributeName);
        if (globalAttribute == null) {
            throw new RuntimeException("Required attribute not present in file: " + attributeName);
        }
        return globalAttribute;
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Attribute rangeBeginningDateAttribute = getGlobalAttributeSafe("RangeBeginningDate");
        final Attribute rangeBeginningTimeAttribute = getGlobalAttributeSafe("RangeBeginningTime");
        final Attribute rangeEndingDateAttribute = getGlobalAttributeSafe("RangeEndingDate");
        final Attribute rangeEndingTimeAttribute = getGlobalAttributeSafe("RangeEndingTime");

        final ProductData.UTC sensingStart = getUtcData(rangeBeginningDateAttribute, rangeBeginningTimeAttribute);
        acquisitionInfo.setSensingStart(sensingStart.getAsDate());

        final ProductData.UTC sensingStop = getUtcData(rangeEndingDateAttribute, rangeEndingTimeAttribute);
        acquisitionInfo.setSensingStop(sensingStop.getAsDate());
    }

    private void setNodeType(AcquisitionInfo acquisitionInfo) {
        final Attribute orbitDirectionAttribute = getGlobalAttributeSafe("OrbitDirection");
        final String orbitDirection = orbitDirectionAttribute.getStringValue();
        assignNodeType(acquisitionInfo, orbitDirection);
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2016-09-02
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(20, 100), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private void setGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array latitudes = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Latitude");
        final Array longitudes = arrayCache.get(LO_RES_SWATH_GEO_GROUP, "Longitude");

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            // @todo 2 tb/tb implement splitted polygon approach if we encounter failures here 2016-09-02
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);
    }

    private Number getFillValue(String groupName, String variableName) throws IOException {
        final Number fillValue = arrayCache.getNumberAttributeValue("_FillValue", groupName, variableName);
        if (fillValue != null) {
            return fillValue;
        }
        final Array array = arrayCache.get(groupName, variableName);
        return NetCDFUtils.getDefaultFillValue(array);
    }

    static int getLayerIndexFromChannelFlagName(String channelQualityFlagName) {
        final int extensionIndex = channelQualityFlagName.lastIndexOf("_") + 1;
        final String extension = channelQualityFlagName.substring(extensionIndex, channelQualityFlagName.length());
        for (int i = 0; i < CHANNEL_QUALITY_FLAG_EXTENSIONS.length; i++) {
            if (CHANNEL_QUALITY_FLAG_EXTENSIONS[i].equals(extension)) {
                return i;
            }
        }
        throw new RuntimeException("Invalid channel variable extension: " + channelQualityFlagName);
    }

    // @todo 3 tb/tb copied from AVHRR reader - move to common helper class 2016-09-06
    private double getOffset(String groupName, String variableName) throws IOException {
        final Number offsetValue = arrayCache.getNumberAttributeValue("OFFSET", groupName, variableName);
        if (offsetValue != null) {
            return offsetValue.doubleValue();
        }
        return 0.0;
    }

    // @todo 3 tb/tb copied from AVHRR reader - move to common helper class 2016-09-06
    private double getScaleFactor(String groupName, String variableName) throws IOException {
        final Number scaleFactorValue = arrayCache.getNumberAttributeValue("SCALE_FACTOR", groupName, variableName);
        if (scaleFactorValue != null) {
            return scaleFactorValue.doubleValue();
        }
        return 1.0;
    }
}
