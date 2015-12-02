
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

import com.bc.fiduceo.core.SatelliteObservation;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;


public class EumetsatIASIReader implements Reader {

    private static final int GEO_INTERVAL_X = 6;
    private static final int GEO_INTERVAL_Y = 6;

    private NetcdfFile netcdfFile;
    private BoundingPolygonCreator boundingPolygonCreator;

    public EumetsatIASIReader() {
        boundingPolygonCreator = new BoundingPolygonCreator(GEO_INTERVAL_X, GEO_INTERVAL_Y);
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
    public AcquisitionInfo read() throws IOException {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        final Date timeConverageStart = getGlobalAttributeAsDate("time_converage_start", netcdfFile);
        final Date timeConverageEnd = getGlobalAttributeAsDate("time_converage_end", netcdfFile);

        final Variable latVariable = netcdfFile.findVariable("lat");
        final Variable lonVariable = netcdfFile.findVariable("lon");
        final Array latArray = latVariable.read();
        final Array lonArray = lonVariable.read();

        final AcquisitionInfo acquisitionInfo = boundingPolygonCreator.createIASIBoundingPolygon((ArrayFloat.D2) latArray, (ArrayFloat.D2) lonArray);
        acquisitionInfo.setSensingStart(timeConverageStart);
        acquisitionInfo.setSensingStop(timeConverageEnd);
        return acquisitionInfo;
    }

    static Date getGlobalAttributeAsDate(String timeCoverage, NetcdfFile netcdfFile) throws IOException {
        try {
            final Attribute globalAttribute = netcdfFile.findGlobalAttribute(timeCoverage);
            if (globalAttribute == null) {
                throw new IOException("Requested attribute '" + timeCoverage + "' not found");
            }

            final String attributeValue = globalAttribute.getStringValue();
            return ProductData.UTC.parse(attributeValue, "yyyy-MM-dd'T'HH:mm:ss").getAsDate();
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }
}
