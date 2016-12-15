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
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.*;

import java.io.IOException;
import java.util.List;

public class AddAmsreSolarAngles extends PostProcessing {

    private Configuration configuration;

    @Override
    protected void prepareImpl(NetcdfFile reader, NetcdfFileWriter writer) {
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
    protected void computeImpl(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Variable earthAzimuthVariable = getVariable(reader, configuration.earthAzimuthVariable);
        final Array earthAzimuth = readAndScale(earthAzimuthVariable);

        final Variable earthIncidenceVariable = getVariable(reader, configuration.earthIncidenceVariable);
        final Array earthIncidence = readAndScale(earthIncidenceVariable);

        final Variable sunAzimuthVariable = getVariable(reader, configuration.sunAzimuthVariable);
        final Array sunAzimuth = readAndScale(sunAzimuthVariable);

        final Variable sunElevationVariable = getVariable(reader, configuration.sunElevationVariable);
        final Array sunElevation = readAndScale(sunElevationVariable);

        final Array sza = Array.factory(DataType.FLOAT, earthAzimuth.getShape());
    }

    private Array readAndScale(Variable earthAzimuthVariable) throws IOException {
        final Array array = earthAzimuthVariable.read();

        final Attribute scaleFactorAttribute = earthAzimuthVariable.findAttribute("SCALE_FACTOR");
        final float scaleFactor = scaleFactorAttribute.getNumericValue().floatValue();
        final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, 0.0);
        return MAMath.convert2Unpacked(array, scaleOffset);
    }

    void configure(Configuration configuration) {
        this.configuration = configuration;
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
