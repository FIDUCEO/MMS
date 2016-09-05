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
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.TimeLocator;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

class AMSRE_Reader implements Reader {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String[] CHANNEL_QUALITY_FLAG_EXTENSIONS = new String[]{"6V", "6H", "10V", "10H", "18V", "18H", "23V", "23H", "36V", "36H", "89V", "89H"};

    private final GeometryFactory geometryFactory;
    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;
    private ArrayCache arrayCache;

    AMSRE_Reader(GeometryFactory geometryFactory) {
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
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        final Array timeArray = arrayCache.get("Low_Res_Swath/Geolocation_Fields", "Time");
        return new AMSRE_TimeLocator(timeArray);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemenetd");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> variables = new ArrayList<>();

        final Group lowResSwathGroup = netcdfFile.findGroup("Low_Res_Swath");
        final Group geoLocationGroup = lowResSwathGroup.findGroup("Geolocation_Fields");
        final List<Variable> geolocationVariables = geoLocationGroup.getVariables();
        variables.addAll(geolocationVariables);

        final Group dataGroup = lowResSwathGroup.findGroup("Data_Fields");
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
                    shortName.contains("(not-resampled)")) {
                continue;
            }

            if (shortName.contains("Land_Ocean_Flag")) {
                // this is a three-d dataset where we currently just pick the lowest layer tb 2016-09-05
                final int[] shape = variable.getShape();
                shape[2] = 1;   // pick channel 6
                final int[] origin = {0, 0, 0};
                final Section section = new Section(origin, shape);
                final Variable channelVariable = variable.section(section);
                channelVariable.setName("Land_Ocean_Flag_6");
                variables.add(channelVariable);
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

            variables.add(variable);
        }

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemenetd");
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
        final Array latitudes = arrayCache.get("Low_Res_Swath/Geolocation_Fields", "Latitude");
        final Array longitudes = arrayCache.get("Low_Res_Swath/Geolocation_Fields", "Longitude");

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
}
