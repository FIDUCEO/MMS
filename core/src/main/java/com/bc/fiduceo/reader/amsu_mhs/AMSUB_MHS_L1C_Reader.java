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

package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AMSUB_MHS_L1C_Reader implements Reader {

    private static final String GEOLOCATION_GROUP_NAME = "Geolocation";
    private static final String DATA_GROUP_NAME = "Data";
    private static final int NUM_SPLITS = 2;

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;

    private ArrayCache arrayCache;
    private TimeLocator timeLocator;
    private PixelLocator pixelLocator;

    private BoundingPolygonCreator boundingPolygonCreator;
    private boolean isAmsuB;

    AMSUB_MHS_L1C_Reader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
        arrayCache = new ArrayCache(netcdfFile);
        timeLocator = null;

        isAmsuB = isAmsub(netcdfFile);
    }

    @Override
    public void close() throws IOException {
        timeLocator = null;
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        final int startYear = getGlobalAttributeAsInteger("startdatayr");
        final int startDay = getGlobalAttributeAsInteger("startdatady");
        final int startTime = getGlobalAttributeAsInteger("startdatatime_ms");

        final int endYear = getGlobalAttributeAsInteger("enddatayr");
        final int endDay = getGlobalAttributeAsInteger("enddatady");
        final int endTime = getGlobalAttributeAsInteger("enddatatime_ms");

        final Date sensingStart = TimeUtils.getDate(startYear, startDay, startTime);
        acquisitionInfo.setSensingStart(sensingStart);

        final Date sensingStop = TimeUtils.getDate(endYear, endDay, endTime);
        acquisitionInfo.setSensingStop(sensingStop);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = extractGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries, geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "\\w*.+[AMBX|MHSX].+[A-Z0-9]{2,3}.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[A-Z]{2}(.[A-Z]\\d{7})?.h5";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array longitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Longitude", "Scale", null);
            final Array latitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Latitude", "Scale", null);

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(NetCDFUtils.toFloat(longitudes), NetCDFUtils.toFloat(latitudes), width, height);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        final Array longitudes = arrayCache.get(GEOLOCATION_GROUP_NAME, "Longitude");
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
            final Array scnlinyr = arrayCache.get(DATA_GROUP_NAME, "scnlinyr");
            final Array scnlindy = arrayCache.get(DATA_GROUP_NAME, "scnlindy");
            final Array scnlintime = arrayCache.get(DATA_GROUP_NAME, "scnlintime");

            timeLocator = new TimeLocator_YearDoyMs(scnlinyr, scnlindy, scnlintime);
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        String rawVariableName = ReaderUtils.stripChannelSuffix(variableName);
        if (rawVariableName.contains("azimuth")) {
            rawVariableName = falsifyAzimuth(rawVariableName);
        }
        final String groupName = getGroupName(rawVariableName);
        Array array = arrayCache.get(groupName, rawVariableName);

        final int rank = array.getRank();
        if (rank == 3) {
            final int channelLayer = getChannelLayer(variableName);
            final int[] shape = array.getShape();
            shape[2] = 1;   // we only want one z-layer
            final int[] offsets = {0, 0, channelLayer};
            array = array.section(offsets, shape);
        } else if (rawVariableName.equals("chanqual")) {
            final int channelLayer = getChannelLayer(variableName);
            final int[] shape = array.getShape();
            shape[1] = 1;   // we only want one channel
            final int[] offsets = {0, channelLayer};
            array = array.section(offsets, shape);
        }

        final Number fillValue = getFillValue(rawVariableName, groupName, array);

        return RawDataReader.read(centerX, centerY, interval, fillValue, array, 90);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array array = readRaw(centerX, centerY, interval, variableName);

        final String strippedVariableName = ReaderUtils.stripChannelSuffix(variableName);

        double scaleFactor = getScaleFactor(strippedVariableName);
        if (ReaderUtils.mustScale(scaleFactor, 0.0)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, 0.0);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    // @todo 3 tb/** this method does the correct thing but there is room for improvement 2016-04-19
    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array scnlinyr = readRaw(x, y, interval, "scnlinyr");
        final int fillValue = getFillValue("scnlinyr", "Data", scnlinyr).intValue();
        final Array acquisitionTimeArray = scnlinyr.copy();
        final Index acquisitionTimeIndex = acquisitionTimeArray.getIndex();
        int yTime = y - interval.getY() / 2;
        final TimeLocator timeLocator = getTimeLocator();
        final Index scnlinyrIndex = scnlinyr.getIndex();

        for (int yIndex = 0; yIndex < interval.getY(); yIndex++) {
            for (int xIndex = 0; xIndex < interval.getX(); xIndex++) {
                scnlinyrIndex.set(yIndex, xIndex);
                acquisitionTimeIndex.set(yIndex, xIndex);
                if (scnlinyr.getInt(scnlinyrIndex) != fillValue) {
                    final long scanLineTime = timeLocator.getTimeFor(0, yTime);
                    final int scanLineTimeInSeconds = (int) (scanLineTime / 1000);
                    acquisitionTimeArray.setInt(acquisitionTimeIndex, scanLineTimeInSeconds);
                }
            }
            yTime++;
        }

        return (ArrayInt.D2) acquisitionTimeArray;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> result = new ArrayList<>();
        final List<Variable> variables = netcdfFile.getVariables();
        final int channelIndexOffset = isAmsuB ? 16 : 1;

        for (final Variable variable : variables) {
            final String variableName = variable.getFullName();
            if (variableName.contains("btemps")) {
                split3dVariableIntoLayers(result, variable, "btemps_ch", channelIndexOffset);
            } else if (variableName.contains("chanqual")) {
                split3dVariableIntoLayers(result, variable, "chanqual_ch", channelIndexOffset);
            } else if (variableName.contains("azimith")) {
                final String shortName = variable.getShortName();
                final String correctedVariableName = correctAzimuth(shortName);
                variable.setShortName(correctedVariableName);
                result.add(variable);
            } else {
                result.add(variable);
            }
        }

        return result;
    }

    @Override
    public Dimension getProductSize() {
        final Variable longitudes = netcdfFile.findVariable(GEOLOCATION_GROUP_NAME + "/Longitude");
        final int[] shape = longitudes.getShape();
        return new Dimension("longitude", shape[1], shape[0]);
    }

    // package access for testing only tb 2016-04-19
    static String falsifyAzimuth(String rawVariableName) {
        return rawVariableName.replace("azimuth", "azimith");
    }

    // package access for testing only tb 2016-04-19
    static String correctAzimuth(String shortName) {
        return shortName.replace("azimith", "azimuth");
    }

    // package access for testing only tb 2016-04-14
    static boolean isAmsub(NetcdfFile netcdfFile) throws IOException {
        final Attribute instrument = netcdfFile.findGlobalAttribute("instrument");
        final int instrumentId = instrument.getNumericValue().intValue();
        if (instrumentId == 11) {
            return true;
        } else if (instrumentId == 12) {
            return false;
        } else {
            throw new IOException("Unsupported instrument type");
        }
    }

    // package access for testing only tb 2016-04-15
    static String getGroupName(String variableName) {
        if ("btemps".equals(variableName) ||
                "chanqual".equals(variableName) ||
                "qualind".equals(variableName) ||
                "scanqual".equals(variableName) ||
                "scnlin".equals(variableName) ||
                "scnlindy".equals(variableName) ||
                "scnlintime".equals(variableName) ||
                "scnlinyr".equals(variableName) ||
                "instrtemp".equals(variableName)) {
            return DATA_GROUP_NAME;
        } else if ("Latitude".equals(variableName) ||
                "Longitude".equals(variableName) ||
                "Satellite_azimith_angle".equals(variableName) ||
                "Solar_azimith_angle".equals(variableName) ||
                "Satellite_zenith_angle".equals(variableName) ||
                "Solar_zenith_angle".equals(variableName)) {
            return GEOLOCATION_GROUP_NAME;
        }

        throw new RuntimeException("Requested invalid variable name: " + variableName);
    }

    // package access for testing only tb 2016-04-15
    static int getChannelLayer(String fullVariableName) {
        final int channelIndex = ReaderUtils.getChannelIndex(fullVariableName);
        return channelIndex > 5 ? channelIndex - 15 : channelIndex;
    }

    private void split3dVariableIntoLayers(List<Variable> result, Variable variable, String variableBaseName, int channelIndexOffset) throws InvalidRangeException {
        final int[] shape = variable.getShape();
        shape[0] = 1;
        final int[] origin = {0, 0, 0};

        for (int channel = 0; channel < 5; channel++) {
            final Section section = new Section(origin, shape);
            final Variable channelVariable = variable.section(section);
            final int channelIndex = channelIndexOffset + channel;
            channelVariable.setName(variableBaseName + channelIndex);
            result.add(channelVariable);
            origin[0]++;
        }
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            // @todo 2 tb/tb move intervals to config 2016-04-12
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(10, 40), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private int getGlobalAttributeAsInteger(String attributeName) throws IOException {
        final Attribute attribute = netcdfFile.findGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new IOException("Global attribute '" + attributeName + "' not found.");
        }
        return attribute.getNumericValue().intValue();
    }

    private Geometries extractGeometries() throws IOException {
        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Longitude", "Scale", null);
        final Array latitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Latitude", "Scale", null);

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        Geometry timeAxisGeometry;

        // AMSU-B scans from west to east on the ascending node. Thus we have to run clockwise around the lon/lat arrays to
        // have the correct inside/outside relation on the polygons tb 2016-04-12
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }

            final int height = longitudes.getShape()[0];
            geometries.setSubsetHeight(boundingPolygonCreator.getSubsetHeight(height, NUM_SPLITS));
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private Number getFillValue(String rawVariableName, String groupName, Array array) throws IOException {
        final String fillValueString = arrayCache.getStringAttributeValue("FillValue", groupName, rawVariableName);
        final Number fillValue;
        if (StringUtils.isNotNullAndNotEmpty(fillValueString)) {
            fillValue = Double.parseDouble(fillValueString);
        } else {
            fillValue = NetCDFUtils.getDefaultFillValue(array);
        }
        return fillValue;
    }

    private double getScaleFactor(String variableName) throws IOException {
        final String groupName = getGroupName(variableName);
        final Number scaleFactorValue = arrayCache.getNumberAttributeValue("Scale", groupName, variableName);
        if (scaleFactorValue != null) {
            return scaleFactorValue.doubleValue();
        }
        return 1.0;
    }
}
