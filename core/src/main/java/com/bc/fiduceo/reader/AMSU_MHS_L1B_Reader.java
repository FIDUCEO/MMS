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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AMSU_MHS_L1B_Reader implements Reader {

    private final static int IntervalX = 50;
    private final static int IntervalY = 50;
    private final BoundingPolygonCreator boundingPolygonCreator;
    private NetcdfFile netcdfFile;


    public AMSU_MHS_L1B_Reader() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        boundingPolygonCreator = new BoundingPolygonCreator(new Interval(IntervalX, IntervalY), geometryFactory);
    }

    public ArrayDouble.D2 rescaleCoordinate(ArrayInt.D2 coodinate, double scale) {
        int[] coordinates = (int[]) coodinate.copyTo1DJavaArray();
        int[] shape = coodinate.getShape();
        ArrayDouble arrayDouble = new ArrayDouble(shape);

        for (int i = 0; i < coordinates.length; i++) {
            arrayDouble.setDouble(i, ((coordinates[i] * scale)));
        }
        return (ArrayDouble.D2) arrayDouble.copy();
    }

    @Override
    public String getRegEx() {
        return "'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5";
    }

    @Override
    public void open(File file) throws IOException {
        netcdfFile = NetcdfFile.open(file.getPath());
    }

    @Override
    public void close() throws IOException {
        netcdfFile.close();
    }

    @Override
    public String sensorTypeName() {
        return ReadersPlugin.NOAA_15.getType();
    }

    @Override
    public AcquisitionInfo read() throws IOException {

        Array latitude = null;
        Array longitude = null;
        float latScale = 1;
        float longScale = 1;

        List<Variable> geolocation = netcdfFile.findGroup("Geolocation").getVariables();
        for (Variable geo : geolocation) {
            if (geo.getShortName().equals("Latitude")) {
                latitude = geo.read();
                latScale = (float) geo.findAttribute("Scale").getNumericValue();
            } else if (geo.getShortName().equals("Longitude")) {
                longitude = geo.read();
                longScale = (float) geo.findAttribute("Scale").getNumericValue();
            }
        }
        if (latitude == null || longitude == null) {
            throw new IOException("The H5 file is courupted");
        }

        ArrayDouble.D2 arrayDoubleLatitude = rescaleCoordinate((ArrayInt.D2) latitude, latScale);
        ArrayDouble.D2 arrayDoubleLongitude = rescaleCoordinate((ArrayInt.D2) longitude, longScale);

        final AcquisitionInfo acquisitionInfo = boundingPolygonCreator.createBoundingPolygon(arrayDoubleLatitude, arrayDoubleLongitude);

        final int startYear = getGlobalAttributeAsInteger("startdatayr");
        final int startDay = getGlobalAttributeAsInteger("startdatady");
        final int startTime = getGlobalAttributeAsInteger("startdatatime_ms");

        final int endYear = getGlobalAttributeAsInteger("enddatayr");
        final int endDay = getGlobalAttributeAsInteger("enddatady");
        final int endTime = getGlobalAttributeAsInteger("enddatatime_ms");

        acquisitionInfo.setSensingStart(getDate(startYear, startDay, startTime));
        acquisitionInfo.setSensingStop(getDate(endYear, endDay, endTime));

        return acquisitionInfo;
    }

    private int getGlobalAttributeAsInteger(String attributeName) throws IOException {
        final Attribute attribute = netcdfFile.findGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new IOException("Global attribute '" + attributeName + "' not found.");
        }
        return attribute.getNumericValue().intValue();
    }

    private Date getDate(int year, int day_of_yr, int time) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, day_of_yr);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MILLISECOND, time);
        return calendar.getTime();
    }
}
