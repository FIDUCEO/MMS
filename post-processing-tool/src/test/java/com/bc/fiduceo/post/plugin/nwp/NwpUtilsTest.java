
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
import org.esa.snap.core.util.math.FracIndex;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NwpUtilsTest {

    private Array array;

    @Before
    public void setUp() {
        array = mock(Array.class);
    }

    @Test
    public void testComputeFutureTimeStepCount() {
        assertEquals(12, NwpUtils.computeFutureTimeStepCount(33));
        assertEquals(6, NwpUtils.computeFutureTimeStepCount(17));
    }

    @Test
    public void testComputePastTimeStepCount() throws Exception {
        assertEquals(20, NwpUtils.computePastTimeStepCount(33));
        assertEquals(10, NwpUtils.computePastTimeStepCount(17));
    }

    @Test
    public void testNearestTimeStep_oneTimeValue() {
        when(array.getSize()).thenReturn(1L);
        when(array.getInt(0)).thenReturn(15);

        final int nearestTimeStep = NwpUtils.nearestTimeStep(array, 18);
        assertEquals(0, nearestTimeStep);
    }

    @Test
    public void testNearestTimeStep_twoTimeValues() {
        when(array.getSize()).thenReturn(2L);
        when(array.getInt(0)).thenReturn(15);
        when(array.getInt(1)).thenReturn(19);

        final int nearestTimeStep = NwpUtils.nearestTimeStep(array, 18);
        assertEquals(1, nearestTimeStep);
    }

    @Test
    public void testNearestTimeStep_twoTimeValues_invertedOrder() {
        when(array.getSize()).thenReturn(2L);
        when(array.getInt(0)).thenReturn(19);
        when(array.getInt(1)).thenReturn(15);

        final int nearestTimeStep = NwpUtils.nearestTimeStep(array, 18);
        assertEquals(0, nearestTimeStep);
    }

    @Test
    public void testNearestTimeStep_threeTimeValues() {
        when(array.getSize()).thenReturn(3L);
        when(array.getInt(0)).thenReturn(21);
        when(array.getInt(1)).thenReturn(17);
        when(array.getInt(2)).thenReturn(13);

        final int nearestTimeStep = NwpUtils.nearestTimeStep(array, 18);
        assertEquals(1, nearestTimeStep);
    }

    @Test
    public void testGetInterpolationIndex_smallArray() {
        final int[] ints = {130};
        final Array timeArray = NetCDFUtils.create(ints);

        try {
            NwpUtils.getInterpolationIndex(timeArray, 130);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetInterpolationIndex_twoValues() {
        final int[] ints = {130, 134};
        final Array timeArray = NetCDFUtils.create(ints);

        final FracIndex fracIndex = NwpUtils.getInterpolationIndex(timeArray, 131);
        assertEquals(0, fracIndex.i);
        assertEquals(0.25f, fracIndex.f, 1e-8);
    }

    @Test
    public void testGetInterpolationIndex_threeValues() {
        final int[] ints = {130, 134, 137};
        final Array timeArray = NetCDFUtils.create(ints);

        final FracIndex fracIndex = NwpUtils.getInterpolationIndex(timeArray, 135);
        assertEquals(1, fracIndex.i);
        assertEquals(0.3333333333333333f, fracIndex.f, 1e-8);
    }
}
