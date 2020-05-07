/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ElevationToSolZenAngleTest {

    private NetcdfFile reader;
    private NetcdfFileWriter writer;

    @Before
    public void setUp() {
        reader = mock(NetcdfFile.class);
        writer = mock(NetcdfFileWriter.class);
    }

    @Test
    public void testGetVariableNamesToRemove() {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source", "tar-create", true));
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source_remain", "tar-create-two", false));

        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);
        final List<String> namesToRemove = postProcessing.getVariableNamesToRemove();
        assertEquals(1, namesToRemove.size());
        assertEquals("source", namesToRemove.get(0));
    }

    @Test
    public void testPrepare_oneVariable() throws IOException, InvalidRangeException {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source", "tar-create", true));

        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);

        final Variable sourceVariable = mock(Variable.class);
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("width", 27));
        dimensions.add(new Dimension("height", 77));
        when(sourceVariable.getDimensions()).thenReturn(dimensions);

        when(reader.findVariable(null, "source")).thenReturn(sourceVariable);

        final Variable targetVariable = mock(Variable.class);
        when(writer.addVariable(null, "tar-create", DataType.FLOAT, dimensions)).thenReturn(targetVariable);

        postProcessing.prepare(reader, writer);

        verify(reader, times(1)).findVariable(null, "source");
        verify(writer, times(1)).addVariable(null, "tar-create", DataType.FLOAT, dimensions);
        verify(targetVariable, times(1)).addAttribute(any());
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testCompute_oneVariable() throws IOException, InvalidRangeException {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("elevation", "zenith", false));
        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);


        final Variable sourceVariable = createVariableWithData(new float[]{108.5f, 109.3f});
        when(sourceVariable.getDataType()).thenReturn(DataType.FLOAT);
        when(reader.findVariable(null, "elevation")).thenReturn(sourceVariable);

        final Variable targetVariable = createVariableWithData(new float[]{0.f, 0.f});
        when(writer.findVariable("zenith")).thenReturn(targetVariable);

        postProcessing.compute(reader, writer);

        verify(reader, times(1)).findVariable(null, "elevation");
        verify(writer, times(1)).findVariable("zenith");
        verify(writer, times(1)).write(any(Variable.class), any());
        verifyNoMoreInteractions(reader, writer);
    }
    
    @Test
    public void testCompute_twoVariables() throws IOException, InvalidRangeException {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("elevation", "zenith", false));
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("up_angle", "zen_angle", false));
        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);


        final Variable sourceVariable_1 = createVariableWithData(new float[]{108.5f, 109.3f});
        when(sourceVariable_1.getDataType()).thenReturn(DataType.FLOAT);
        final Variable sourceVariable_2 = createVariableWithData(new float[]{107.5f, 108.3f});
        when(sourceVariable_2.getDataType()).thenReturn(DataType.FLOAT);
        when(reader.findVariable(null, "elevation")).thenReturn(sourceVariable_1);
        when(reader.findVariable(null, "up_angle")).thenReturn(sourceVariable_2);

        final Variable targetVariable_1 = createVariableWithData(new float[]{0.f, 0.f});
        final Variable targetVariable_2 = createVariableWithData(new float[]{0.f, 0.f});
        when(writer.findVariable("zenith")).thenReturn(targetVariable_1);
        when(writer.findVariable("zen_angle")).thenReturn(targetVariable_2);

        postProcessing.compute(reader, writer);

        verify(reader, times(1)).findVariable(null, "elevation");
        verify(reader, times(1)).findVariable(null, "up_angle");
        verify(writer, times(1)).findVariable("zenith");
        verify(writer, times(1)).findVariable("zen_angle");
        verify(writer, times(2)).write(any(Variable.class), any());
        verifyNoMoreInteractions(reader, writer);
    }

    @Test
    public void testCalculateZenithAngle() {
        final float[] source = {34.7f, 35.8f, 33.6f};
        final float[] target = {0.f, 0.f, 0.f};

        final Array sourceArray = NetCDFUtils.create(source);
        final Array targetArray = NetCDFUtils.create(target);

        ElevationToSolZenAngle.calculateZenithAngle(sourceArray, targetArray, Float.NaN);

        assertEquals(55.29999923706055f, targetArray.getFloat(0), 1e-8);
        assertEquals(54.20000076293945f, targetArray.getFloat(1), 1e-8);
        assertEquals(56.400001525878906f, targetArray.getFloat(2), 1e-8);
    }

    @Test
    public void testCalculateZenithAngle_withFillValue() {
        final float[] source = {34.7f, -1000.f, 33.6f};
        final float[] target = {0.f, 0.f, 0.f};

        final Array sourceArray = NetCDFUtils.create(source);
        final Array targetArray = NetCDFUtils.create(target);

        ElevationToSolZenAngle.calculateZenithAngle(sourceArray, targetArray, -1000.f);

        assertEquals(55.29999923706055f, targetArray.getFloat(0), 1e-8);
        assertEquals(NetCDFUtils.getDefaultFillValue(float.class).floatValue(), targetArray.getFloat(1), 1e-8);
        assertEquals(56.400001525878906f, targetArray.getFloat(2), 1e-8);
    }

    private Variable createVariableWithData(float[] data) throws IOException {
        final Array dataArray = NetCDFUtils.create(data);

        final Variable variable = mock(Variable.class);
        when(variable.read()).thenReturn(dataArray);

        return variable;
    }
}
