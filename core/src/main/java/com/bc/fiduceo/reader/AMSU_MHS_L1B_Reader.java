
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
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
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

    private final static int IntervalX = 10;
    private final static int IntervalY = 10;

    private final BoundingPolygonCreator boundingPolygonCreator;
    private NetcdfFile netcdfFile;


    public AMSU_MHS_L1B_Reader() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        boundingPolygonCreator = new BoundingPolygonCreator(new Interval(IntervalX, IntervalY), geometryFactory);
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
    public String getReaderName() {
        return "AMSU-B";
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        Array latitude = null;
        Array longitude = null;


        List<Variable> geolocation = netcdfFile.findGroup("Geolocation").getVariables();
        for (Variable geo : geolocation) {
            if (geo.getShortName().equals("Latitude")) {
                latitude = geo.read();
            } else if (geo.getShortName().equals("Longitude")) {
                longitude = geo.read();
            }
        }
        if (latitude == null || longitude == null) {
            throw new IOException("The H5 file is courupted");
        }

        final AcquisitionInfo acquisitionInfo = boundingPolygonCreator.createPixelCodedBoundingPolygon((ArrayInt.D2) latitude, (ArrayInt.D2) longitude, NodeType.ASCENDING);

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
