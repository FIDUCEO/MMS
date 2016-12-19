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

package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.post.PostProcessing;
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

public class AddAmsreSolarAngles extends PostProcessing {

    private Configuration configuration;

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final Variable earthAzimuthVariable = getVariable(reader, configuration.earthAzimuthVariable);
        final List<Dimension> dimensions = earthAzimuthVariable.getDimensions();

        writer.addVariable(null, configuration.szaVariable, DataType.FLOAT, dimensions);
        writer.addVariable(null, configuration.saaVariable, DataType.FLOAT, dimensions);
    }

    private Variable getVariable(NetcdfFile reader, String name) {
        final Variable earthAzimuthVariable = reader.findVariable(null, name);
        if (earthAzimuthVariable == null) {
            throw new RuntimeException("Input Variable '" + configuration.earthAzimuthVariable + "' not present in input file");
        }
        return earthAzimuthVariable;
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable earthAzimuthVariable = getVariable(reader, configuration.earthAzimuthVariable);
        final Array earthAzimuth = readAndScale(earthAzimuthVariable);

        final Variable earthIncidenceVariable = getVariable(reader, configuration.earthIncidenceVariable);
        final Array earthIncidence = readAndScale(earthIncidenceVariable);

        final Variable sunAzimuthVariable = getVariable(reader, configuration.sunAzimuthVariable);
        final Array sunAzimuth = readAndScale(sunAzimuthVariable);

        final Variable sunElevationVariable = getVariable(reader, configuration.sunElevationVariable);
        final Array sunElevation = readAndScale(sunElevationVariable);

        final Array sza = Array.factory(DataType.FLOAT, earthAzimuth.getShape());
        final Array saa = Array.factory(DataType.FLOAT, earthAzimuth.getShape());

        calculateAngles(earthAzimuth, earthIncidence, sunAzimuth, sunElevation, sza, saa);

        final Variable szaVariable = writer.findVariable(configuration.szaVariable);
        final Variable saaVariable = writer.findVariable(configuration.saaVariable);

        writer.write(szaVariable, sza);
        writer.write(saaVariable, saa);
    }

    // package access for testing only tb 2016-12-16
    static void calculateAngles(Array earthAzimuth, Array earthIncidence, Array sunAzimuth, Array sunElevation, Array sza, Array saa) {
        final IndexIterator szaIterator = sza.getIndexIterator();
        final IndexIterator saaIterator = saa.getIndexIterator();

        // @todo 2 tb/tb check if we need to call hasNext for all variables or if it is sufficient to call it once to
        // initialize the iterators and check on ly one in the loop - as we assume all arrays having the same size 2016-12-16
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

        final Attribute scaleFactorAttribute = unscaledVariable.findAttribute("SCALE_FACTOR");
        final float scaleFactor = scaleFactorAttribute.getNumericValue().floatValue();
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
