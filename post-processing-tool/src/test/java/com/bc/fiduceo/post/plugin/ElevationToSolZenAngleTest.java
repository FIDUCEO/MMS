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

import org.junit.Test;
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ElevationToSolZenAngleTest {

    @Test
    public void testGetVariableNamesToRemove() {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source", "tar-get", true));
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source_remain", "tar-get-two", false));

        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);
        final List<String> namesToRemove = postProcessing.getVariableNamesToRemove();
        assertEquals(1, namesToRemove.size());
        assertEquals("source", namesToRemove.get(0));
    }

    @Test
    public void testPrepare_oneVariable() throws IOException, InvalidRangeException {
        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();
        configuration.conversions.add(new ElevationToSolZenAngle.Conversion("source", "tar-get", true));

        final ElevationToSolZenAngle postProcessing = new ElevationToSolZenAngle(configuration);

        final Variable sourceVariable = mock(Variable.class);
        final ArrayList<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("width", 27));
        dimensions.add(new Dimension("height", 77));
        when(sourceVariable.getDimensions()).thenReturn(dimensions);

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.findVariable(null, "source")).thenReturn(sourceVariable);

        final Variable targetVariable = mock(Variable.class);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addVariable(null, "tar-get", DataType.FLOAT, dimensions)).thenReturn(targetVariable);

        postProcessing.prepare(reader, writer);

        verify(reader, times(1)).findVariable(null, "source");
        verify(writer, times(1)).addVariable(null, "tar-get", DataType.FLOAT, dimensions);
        verify(targetVariable, times(1)).addAttribute(anyObject());
        verifyNoMoreInteractions(reader, writer);
    }
}
