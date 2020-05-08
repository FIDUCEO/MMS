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

package com.bc.fiduceo.post.plugin.amsr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.IOException;
import java.util.List;

class AddAmsrSolarAngles extends PostProcessing {

    private Configuration configuration;

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        // unfortunately, we need custom escaping of the variable name tb 2020-05-08
        String escapedName = escapeName(configuration.earthAzimuthVariable);
        final Variable earthAzimuthVariable = NetCDFUtils.getVariable(reader, escapedName, false);
        final List<Dimension> dimensions = earthAzimuthVariable.getDimensions();

        escapedName = escapeName(configuration.szaVariable);
        final Variable variable = writer.addVariable(null, escapedName, DataType.FLOAT, dimensions);
        variable.addAttribute(new Attribute("description", "Calculated from AMSR data as sza = Sun_Elevation + Earth_Incidence"));
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class).floatValue()));

        escapedName = escapeName(configuration.saaVariable);
        writer.addVariable(null, escapedName, DataType.FLOAT, dimensions);
        variable.addAttribute(new Attribute("description", "Calculated from AMSR data as saa = (Earth_Azimuth - Sun_Azimuth - 180.0) mod 360.0"));
        variable.addAttribute(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class).floatValue()));
    }

    private String escapeName(String variableName) {
        return variableName.replace(".", "\\.");
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        // unfortunately, we need custom escaping of the variable name tb 2020-05-08
        String escapedName = escapeName(configuration.earthAzimuthVariable);
        final Variable earthAzimuthVariable = NetCDFUtils.getVariable(reader, escapedName, false);
        final Array earthAzimuth = readAndScale(earthAzimuthVariable);

        escapedName = escapeName(configuration.earthIncidenceVariable);
        final Variable earthIncidenceVariable = NetCDFUtils.getVariable(reader, escapedName, false);
        final Array earthIncidence = readAndScale(earthIncidenceVariable);

        escapedName = escapeName(configuration.sunAzimuthVariable);
        final Variable sunAzimuthVariable = NetCDFUtils.getVariable(reader, escapedName, false);
        final Array sunAzimuth = readAndScale(sunAzimuthVariable);

        escapedName = escapeName(configuration.sunElevationVariable);
        final Variable sunElevationVariable = NetCDFUtils.getVariable(reader, escapedName, false);
        final Array sunElevation = readAndScale(sunElevationVariable);

        final Array sza = Array.factory(DataType.FLOAT, earthAzimuth.getShape());
        final Array saa = Array.factory(DataType.FLOAT, earthAzimuth.getShape());

        calculateAngles(earthAzimuth, earthIncidence, sunAzimuth, sunElevation, sza, saa);

        escapedName = escapeName(configuration.szaVariable);
        final Variable szaVariable = NetCDFUtils.getVariable(writer, escapedName, true);
        escapedName = escapeName(configuration.saaVariable);
        final Variable saaVariable = NetCDFUtils.getVariable(writer, escapedName, true);

        writer.write(szaVariable, sza);
        writer.write(saaVariable, saa);
    }

    // package access for testing only tb 2016-12-16
    static void calculateAngles(Array earthAzimuth, Array earthIncidence, Array sunAzimuth, Array sunElevation, Array sza, Array saa) {
        final IndexIterator szaIterator = sza.getIndexIterator();
        final IndexIterator saaIterator = saa.getIndexIterator();

        while (earthAzimuth.hasNext() && earthIncidence.hasNext() && sunAzimuth.hasNext() && sunElevation.hasNext()) {
            final float sunElevationValue = sunElevation.nextFloat();
            final float earthIncidenceValue = earthIncidence.nextFloat();
            if (sunElevationValue == N3iosp.NC_FILL_FLOAT || earthIncidenceValue == N3iosp.NC_FILL_FLOAT) {
                szaIterator.setFloatNext(N3iosp.NC_FILL_FLOAT);
            } else {
                final float szaValue = sunElevationValue + earthIncidenceValue;
                szaIterator.setFloatNext(szaValue);
            }

            final float earthAzimuthValue = earthAzimuth.nextFloat();
            final float sunAzimuthValue = sunAzimuth.nextFloat();
            if (earthAzimuthValue == N3iosp.NC_FILL_FLOAT || sunAzimuthValue == N3iosp.NC_FILL_FLOAT) {
                saaIterator.setFloatNext(N3iosp.NC_FILL_FLOAT);
            } else {
                final float saaValue = (earthAzimuthValue - sunAzimuthValue + 180.f) % 360.f;
                saaIterator.setFloatNext(saaValue);
            }
        }
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    private Array readAndScale(Variable unscaledVariable) throws IOException {
        final Array array = unscaledVariable.read();
        final Array floatArray = Array.factory(DataType.FLOAT, array.getShape());
        final IndexIterator targetIterator = floatArray.getIndexIterator();

        final float fillValue = NetCDFUtils.getDefaultFillValue(float.class).floatValue();
        final float scaleFactor = NetCDFUtils.getAttributeFloat(unscaledVariable, "SCALE_FACTOR", fillValue);
        while (array.hasNext() && floatArray.hasNext()) {
            final float scaled = scaleFactor * array.nextShort();
            targetIterator.setFloatNext(scaled);
        }

        return floatArray;
    }

    static class Configuration {
        String sunElevationVariable;
        String earthIncidenceVariable;
        String sunAzimuthVariable;
        String earthAzimuthVariable;

        String szaVariable;
        String saaVariable;
    }
}
