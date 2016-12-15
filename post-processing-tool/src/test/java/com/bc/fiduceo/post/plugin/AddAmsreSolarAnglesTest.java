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

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class AddAmsreSolarAnglesTest {

    private AddAmsreSolarAngles addAmsreSolarAngles;
    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        addAmsreSolarAngles = new AddAmsreSolarAngles();

        final AddAmsreSolarAngles.Configuration configuration = new AddAmsreSolarAngles.Configuration();
        configuration.earthAzimuthVariable = "Earth_Azimuth";
        configuration.earthIncidenceVariable = "Earth_Incidence";
        configuration.sunElevationVariable = "Sun_Elevation";
        configuration.sunAzimuthVariable = "Sun_Azimuth";
        configuration.szaVariable = "sun_zenith_angle";
        configuration.saaVariable = "sun_azimuth_angle";
        addAmsreSolarAngles.configure(configuration);
        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @Test
    public void testPrepare() {
        final Variable earthAzimuthVariable = mock(Variable.class);
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("width", 12));
        dimensions.add(new Dimension("height", 105));
        when(earthAzimuthVariable.getDimensions()).thenReturn(dimensions);
        when(reader.findVariable(null, "Earth_Azimuth")).thenReturn(earthAzimuthVariable);

        addAmsreSolarAngles.prepareImpl(reader, writer);

        verify(writer, times(1)).addVariable(null, "sun_zenith_angle", DataType.FLOAT, dimensions);
        verify(writer, times(1)).addVariable(null, "sun_azimuth_angle", DataType.FLOAT, dimensions);
        verify(reader, times(1)).findVariable(null, "Earth_Azimuth");
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testPrepare_missingInputVariable() {
        try {
            addAmsreSolarAngles.prepareImpl(reader, writer);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCompute_singlePixel() throws IOException, InvalidRangeException {
        final Variable earthIncidenceVariable = createVariableWithData(new short[]{11046}, 0.005);
        when(reader.findVariable(null, "Earth_Incidence")).thenReturn(earthIncidenceVariable);

        final Variable sunElevationVariable = createVariableWithData(new short[]{495}, 0.1);
        when(reader.findVariable(null, "Sun_Elevation")).thenReturn(sunElevationVariable);

        final Variable earthAzimuthVariable = createVariableWithData(new short[]{-8915}, 0.01);
        when(reader.findVariable(null, "Earth_Azimuth")).thenReturn(earthAzimuthVariable);

        final Variable sunAzimuthVariable = createVariableWithData(new short[]{789}, 0.1);
        when(reader.findVariable(null, "Sun_Azimuth")).thenReturn(sunAzimuthVariable);

        addAmsreSolarAngles.computeImpl(reader, writer);

    }

    private Variable createVariableWithData(short[] data, double scaleFactor) throws IOException {
        final Array dataArray = Array.factory(data);

        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(dataArray);

        final Attribute scaleFactorAttribute = mock(Attribute.class);
        when(scaleFactorAttribute.getNumericValue()).thenReturn(scaleFactor);
        when(variable.findAttribute("SCALE_FACTOR")).thenReturn(scaleFactorAttribute);

        return variable;
    }
}
