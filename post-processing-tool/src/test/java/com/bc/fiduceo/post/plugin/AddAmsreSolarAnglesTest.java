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
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(null, "sun_zenith_angle", DataType.FLOAT, dimensions)).thenReturn(targetVariable);
        when(writer.addVariable(null, "sun_azimuth_angle", DataType.FLOAT, dimensions)).thenReturn(targetVariable);

        addAmsreSolarAngles.prepare(reader, writer);

        verify(writer, times(1)).addVariable(null, "sun_zenith_angle", DataType.FLOAT, dimensions);
        verify(writer, times(1)).addVariable(null, "sun_azimuth_angle", DataType.FLOAT, dimensions);
        verify(reader, times(1)).findVariable(null, "Earth_Azimuth");
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testPrepare_missingInputVariable() {
        try {
            addAmsreSolarAngles.prepare(reader, writer);
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

        when(writer.findVariable("sun_zenith_angle")).thenReturn(mock(Variable.class));
        when(writer.findVariable("sun_azimuth_angle")).thenReturn(mock(Variable.class));

        addAmsreSolarAngles.compute(reader, writer);

        verify(reader, times(1)).findVariable(null, "Earth_Incidence");
        verify(reader, times(1)).findVariable(null, "Sun_Elevation");
        verify(reader, times(1)).findVariable(null, "Earth_Azimuth");
        verify(reader, times(1)).findVariable(null, "Sun_Azimuth");

        verify(writer, times(1)).findVariable("sun_zenith_angle");
        verify(writer, times(1)).findVariable("sun_azimuth_angle");
        verify(writer, times(2)).write(any(), any());
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testCalculateAngles_3by3_singleLayer() {
        final int[] shape = new int[]{3, 3};
        final float[] earthIncidences = {55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f};
        final Array earthIncidenceArray = Array.factory(float.class, shape, earthIncidences);

        final float[] sunElevation = {41.4f, 41.4f, 41.5f, 41.5f, 41.5f, 41.6f, 41.5f, 41.6f, 41.7f};
        final Array sunElevationArray = Array.factory(float.class, shape, sunElevation);

        final float[] earthAzimuth = {65.67f, 64.7f, 63.71f, 65.53f, 64.55f, 63.56f, 65.39f, 64.41f, 63.4f};
        final Array earthAzimuthArray = Array.factory(float.class, shape, earthAzimuth);

        final float[] sunAzimuth = {-125.1f, -125.8f, -126.4f, -125.2f, -125.8f, -126.4f, -125.1f, -125.8f, -126.4f};
        final Array sunAzimuthArray = Array.factory(float.class, shape, sunAzimuth);

        final Array sza = Array.factory(DataType.FLOAT, shape);
        final Array saa = Array.factory(DataType.FLOAT, shape);

        AddAmsreSolarAngles.calculateAngles(earthAzimuthArray, earthIncidenceArray, sunAzimuthArray, sunElevationArray, sza, saa);

        final float[] expectedSza = new float[]{96.505005f, 96.505005f, 96.604996f, 96.604996f, 96.604996f, 96.705f, 96.604996f, 96.705f, 96.805f};
        final float[] expectedSaa = new float[]{10.769989f, 10.5f, 10.109985f, 10.72998f, 10.350006f, 9.960022f, 10.48999f, 10.210022f, 9.799988f};

        assertArrayEquals(expectedSza, (float[]) sza.get1DJavaArray(float.class), 1e-8f);
        assertArrayEquals(expectedSaa, (float[]) saa.get1DJavaArray(float.class), 1e-8f);
    }

    @Test
    public void testCalculateAngles_3by3_singleLayer_withInvalidPixels() {
        final int[] shape = new int[]{3, 3};
        final float[] earthIncidences = {55.105f, N3iosp.NC_FILL_FLOAT, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f, 55.105f};
        final Array earthIncidenceArray = Array.factory(float.class, shape, earthIncidences);

        final float[] sunElevation = {41.4f, 41.4f, 41.5f, N3iosp.NC_FILL_FLOAT, 41.5f, 41.6f, 41.5f, 41.6f, 41.7f};
        final Array sunElevationArray = Array.factory(float.class, shape, sunElevation);

        final float[] earthAzimuth = {65.67f, 64.7f, 63.71f, 65.53f, 64.55f, N3iosp.NC_FILL_FLOAT, 65.39f, 64.41f, 63.4f};
        final Array earthAzimuthArray = Array.factory(float.class, shape, earthAzimuth);

        final float[] sunAzimuth = {-125.1f, -125.8f, -126.4f, -125.2f, -125.8f, -126.4f, -125.1f, N3iosp.NC_FILL_FLOAT, -126.4f};
        final Array sunAzimuthArray = Array.factory(float.class, shape, sunAzimuth);

        final Array sza = Array.factory(DataType.FLOAT, shape);
        final Array saa = Array.factory(DataType.FLOAT, shape);

        AddAmsreSolarAngles.calculateAngles(earthAzimuthArray, earthIncidenceArray, sunAzimuthArray, sunElevationArray, sza, saa);

        final float[] expectedSza = new float[]{96.505005f, N3iosp.NC_FILL_FLOAT, 96.604996f, N3iosp.NC_FILL_FLOAT, 96.604996f, 96.705f, 96.604996f, 96.705f, 96.805f};
        final float[] expectedSaa = new float[]{10.769989f, 10.5f, 10.109985f, 10.72998f, 10.350006f, N3iosp.NC_FILL_FLOAT, 10.48999f, N3iosp.NC_FILL_FLOAT, 9.799988f};

        assertArrayEquals(expectedSza, (float[]) sza.get1DJavaArray(float.class), 1e-8f);
        assertArrayEquals(expectedSaa, (float[]) saa.get1DJavaArray(float.class), 1e-8f);
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
