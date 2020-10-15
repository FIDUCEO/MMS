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

package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileMergerTest {

    @Test
    public void testExtractCenterVector_invalidRank() throws IOException, InvalidRangeException {
        final Variable variable = mock(Variable.class);
        when(variable.getRank()).thenReturn(2);

        try {
            FileMerger.extractCenterVector(variable);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractCenterVector_rank_1() throws IOException, InvalidRangeException {
        final Array floatArray = NetCDFUtils.create(new float[]{2.4f, 3.5f, 4.6f});
        final Variable variable = mock(Variable.class);

        when(variable.getRank()).thenReturn(1);
        when(variable.read()).thenReturn(floatArray);

        final Array centerVector = FileMerger.extractCenterVector(variable);
        assertArrayEquals(new int[]{3}, centerVector.getShape());
        assertEquals(3.5f, centerVector.getFloat(1), 1e-8);
    }

    @Test
    public void testExtractCenterVector_rank_3() throws IOException, InvalidRangeException {
        final Array floatArray = Array.factory(DataType.FLOAT, new int[] {2, 3, 3}, new float[]{1.4f, 2.5f, 3.6f, 4.4f, 5.5f, 6.6f, 7.4f, 8.5f, 9.6f, 1.5f, 2.6f, 3.7f, 4.5f, 5.6f, 6.7f, 7.5f, 8.6f, 9.7f});
        final int[] origin = {0, 1, 1};
        final int[] shape = {2, 1, 1};
        final Variable variable = mock(Variable.class);

        final Array result = floatArray.section(origin, shape, new int[] {1, 1, 1});

        when(variable.getRank()).thenReturn(3);
        when(variable.getShape()).thenReturn(floatArray.getShape());
        when(variable.read(origin, shape)).thenReturn(result);

        final Array centerVector = FileMerger.extractCenterVector(variable);
        assertArrayEquals(new int[]{2}, centerVector.getShape());
        final Index index = centerVector.getIndex();
        index.set(0);
        assertEquals(5.5f, centerVector.getFloat(index), 1e-8);
        index.set(1);
        assertEquals(5.6f, centerVector.getFloat(index), 1e-8);
    }
}
