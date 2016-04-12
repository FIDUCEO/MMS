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
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ArrayCache;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Geometries;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AMSUB_MHS_L1C_Reader implements Reader {

    private static final String SCALE_ATTRIBUTE_NAME = "Scale";
    private static final String GEOLOCATION_GROUP_NAME = "Geolocation";
    private static final String LONGITUDE_VARIABLE_NAME = "Longitude";

    private NetcdfFile netcdfFile;

    private ArrayCache arrayCache;
    private GeometryFactory geometryFactory;
    private BoundingPolygonCreator boundingPolygonCreator;

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

        final Geometries geometries = new Geometries();

        final Array longitudes = arrayCache.get(GEOLOCATION_GROUP_NAME, "Longitude");
        final Array latitudes = arrayCache.get(GEOLOCATION_GROUP_NAME, "Latitude");

        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();
        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        acquisitionInfo.setBoundingGeometry(boundingGeometry);


        final int startYear = getGlobalAttributeAsInteger("startdatayr");
        final int startDay = getGlobalAttributeAsInteger("startdatady");
        final int startTime = getGlobalAttributeAsInteger("startdatatime_ms");

        final int endYear = getGlobalAttributeAsInteger("enddatayr");
        final int endDay = getGlobalAttributeAsInteger("enddatady");
        final int endTime = getGlobalAttributeAsInteger("enddatatime_ms");

        acquisitionInfo.setSensingStart(getDate(startYear, startDay, startTime));
        acquisitionInfo.setSensingStop(getDate(endYear, endDay, endTime));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        // @todo 1 tb/tb continue here 2016-02-25
        final Array longitudes = getLongitudes(netcdfFile);
        return new AMSU_MHS_GeoCoding();
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneIndex) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("Not yet implemented");
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
    public List<Variable> getVariables() {
        throw new RuntimeException("not implemented");
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

    static Array getLongitudes(NetcdfFile netcdfFile) throws IOException {
        final Group geolocationGroup = netcdfFile.findGroup(GEOLOCATION_GROUP_NAME);
        if (geolocationGroup == null) {
            throw new IOException("File does not contain the group '" + GEOLOCATION_GROUP_NAME + "' that is required");
        }

        final Variable longitudesVariable = geolocationGroup.findVariable(LONGITUDE_VARIABLE_NAME);
        if (longitudesVariable == null) {
            throw new IOException("File does not contain the variable '" + LONGITUDE_VARIABLE_NAME + "' that is required");
        }
        final Attribute scaleAtribute = longitudesVariable.findAttribute(SCALE_ATTRIBUTE_NAME);
        if (scaleAtribute == null) {
            throw new IOException("The variable '" + LONGITUDE_VARIABLE_NAME + "' does not contain the required attribute '" + SCALE_ATTRIBUTE_NAME + "'");
        }

        final Array longitudes = longitudesVariable.read();
        final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleAtribute.getNumericValue().doubleValue(), 0.0);
        return MAMath.convert2Unpacked(longitudes, scaleOffset);
    }

    static ArrayDouble.D2 rescaleCoordinate(ArrayInt.D2 coodinate, double scale) {
        int[] coordinates = (int[]) coodinate.copyTo1DJavaArray();
        int[] shape = coodinate.getShape();
        ArrayDouble arrayDouble = new ArrayDouble(shape);

        for (int i = 0; i < coordinates.length; i++) {
            arrayDouble.setDouble(i, ((coordinates[i] * scale)));
        }
        return (ArrayDouble.D2) arrayDouble.copy();
    }

    public static List<ArrayDouble.D2> getLat_Long(NetcdfFile netcdfFile) throws IOException {
        Array latitude = null;
        Array longitude = null;
        float latScale = 1;
        float longScale = 1;
        List<Variable> geolocation = netcdfFile.findGroup(GEOLOCATION_GROUP_NAME).getVariables();


        for (Variable geo : geolocation) {
            if (geo.getShortName().equals("Latitude")) {
                latitude = geo.read();
                latScale = (float) geo.findAttribute(SCALE_ATTRIBUTE_NAME).getNumericValue();
            } else if (geo.getShortName().equals(LONGITUDE_VARIABLE_NAME)) {
                longitude = geo.read();
                longScale = (float) geo.findAttribute("Scale").getNumericValue();
            }
        }
        List<ArrayDouble.D2> d2List = new ArrayList<>();

        ArrayDouble.D2 arrayLong = AMSUB_MHS_L1C_Reader.rescaleCoordinate((ArrayInt.D2) longitude, longScale);
        ArrayDouble.D2 arrayLat = AMSUB_MHS_L1C_Reader.rescaleCoordinate((ArrayInt.D2) latitude, latScale);

        d2List.add(arrayLong);// Index 0 Longitude
        d2List.add(arrayLat);// Index 0 Latitude
        return d2List;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator() {
        if (boundingPolygonCreator == null) {
            final GeometryFactory geometryFactory = getGeometryFactory();

            // @todo 2 tb/tb move intervals to config 2016-04-12
            boundingPolygonCreator = new BoundingPolygonCreator(new Interval(8, 50), geometryFactory);
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
}
