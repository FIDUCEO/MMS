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

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.PixelLocatorFactory;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AMSUB_MHS_L1C_Reader implements Reader {

    private static final String GEOLOCATION_GROUP_NAME = "Geolocation";
    private static final String DATA_GROUP_NAME = "Data";
    private static final int NUM_SPLITS = 2;

    private NetcdfFile netcdfFile;

    private ArrayCache arrayCache;
    private TimeLocator timeLocator;
    private PixelLocator pixelLocator;
    private GeometryFactory geometryFactory;
    private BoundingPolygonCreator boundingPolygonCreator;
    private boolean isAmsuB;

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

        final Date sensingStart = getDate(startYear, startDay, startTime);
        acquisitionInfo.setSensingStart(sensingStart);

        final Date sensingStop = getDate(endYear, endDay, endTime);
        acquisitionInfo.setSensingStop(sensingStop);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        final Geometries geometries = extractGeometries();
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());

        setTimeAxes(acquisitionInfo, sensingStart, sensingStop, geometries);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array longitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Longitude", "Scale", null);
            final Array latitudes = arrayCache.getScaled(GEOLOCATION_GROUP_NAME, "Latitude", "Scale", null);

            final int[] shape = longitudes.getShape();
            final int width = shape[1];
            final int height = shape[0];
            pixelLocator = PixelLocatorFactory.getSwathPixelLocator(toFloat(longitudes), toFloat(latitudes), width, height);
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

            timeLocator = new AMSUB_MHS_TimeLocator(scnlinyr, scnlindy, scnlintime);
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final String rawVariableName = stripChannelSuffix(variableName);
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

        // @todo 1 tb/tb provide correct invalid Pixel value 2015-04-15
        return RawDataReader.read(centerX, centerY, interval, -1, array, 90);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException {
        final List<Variable> result = new ArrayList<>();
        final List<Variable> variables = netcdfFile.getVariables();
        final int channelIndexOffset = isAmsuB ? 16 : 1;

        for (final Variable variable : variables) {
            final String variableName = variable.getFullName();
            if (variableName.contains("btemps")) {
                split3dVariableIntoLayers(result, variable, "btemp_ch", channelIndexOffset);
            } else if (variableName.contains("chanqual")) {
                split3dVariableIntoLayers(result, variable, "chanqual_ch", channelIndexOffset);
            } else {
                result.add(variable);
            }
        }

        return result;
    }

    // package access for testing only tb 2016-04-12
    static Date getDate(int year, int dayOfYear, int millisecsInDay) {
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MILLISECOND, millisecsInDay);
        return calendar.getTime();
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

    static String stripChannelSuffix(String fullVariableName) {
        final int splitIndex = fullVariableName.indexOf("_ch");
        if (splitIndex > 0) {
            return fullVariableName.substring(0, splitIndex);
        }
        return fullVariableName;
    }

    // package access for testing only tb 2016-04-15
    static int getChannelLayer(String fullVariableName) {
        final int splitIndex = fullVariableName.indexOf("_ch");
        if (splitIndex < 0) {
            return 0;
        }
        final String channelNumber = fullVariableName.substring(splitIndex + 3);
        final int channelIndex = Integer.parseInt(channelNumber) - 1;
        return channelIndex > 5 ? channelIndex - 15: channelIndex;
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
            final GeometryFactory geometryFactory = getGeometryFactory();

            // @todo 2 tb/tb move intervals to config 2016-04-12
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(10, 40), geometryFactory);
        }

        return boundingPolygonCreator;
    }

    private GeometryFactory getGeometryFactory() {
        if (geometryFactory == null) {
            // @todo 1 tb/tb inject geometry factory 2016-04-12
            geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        }

        return geometryFactory;
    }

    private int getGlobalAttributeAsInteger(String attributeName) throws IOException {
        final Attribute attribute = netcdfFile.findGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new IOException("Global attribute '" + attributeName + "' not found.");
        }
        return attribute.getNumericValue().intValue();
    }

    // @todo 3 tb/tb duplicated code - refactor and move to common reader helper class 2016-04-12
    private void setTimeAxes(AcquisitionInfo acquisitionInfo, Date startDate, Date stopDate, Geometries geometries) {
        final Geometry timeAxesGeometry = geometries.getTimeAxesGeometry();
        if (timeAxesGeometry instanceof GeometryCollection) {
            final GeometryCollection axesCollection = (GeometryCollection) timeAxesGeometry;
            final Geometry[] axesGeometries = axesCollection.getGeometries();
            final TimeAxis[] timeAxes = new TimeAxis[axesGeometries.length];
            final TimeInterval timeInterval = new TimeInterval(startDate, stopDate);
            final TimeInterval[] timeSplits = timeInterval.split(axesGeometries.length);
            for (int i = 0; i < axesGeometries.length; i++) {
                final LineString axisGeometry = (LineString) axesGeometries[i];
                final TimeInterval currentTimeInterval = timeSplits[i];
                timeAxes[i] = geometryFactory.createTimeAxis(axisGeometry, currentTimeInterval.getStartTime(), currentTimeInterval.getStopTime());
            }
            acquisitionInfo.setTimeAxes(timeAxes);
        } else {
            final TimeAxis timeAxis = geometryFactory.createTimeAxis((LineString) timeAxesGeometry, startDate, stopDate);
            acquisitionInfo.setTimeAxes(new TimeAxis[]{timeAxis});
        }
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

    // @todo 2 tb/** make static, package local and write test 2016-04-15
    private Array toFloat(Array original) {
        final Array floatArray = Array.factory(Float.class, original.getShape());
        MAMath.copyFloat(floatArray, original);
        return floatArray;
    }
}
