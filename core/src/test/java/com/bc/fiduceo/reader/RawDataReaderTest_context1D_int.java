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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RawDataReaderTest_context1D_int {

    private Interval windowSize;
    private Number fillValue;
    private Array rawArray;

    @Before
    public void setUp() throws Exception {
        windowSize = new Interval(3, 3);
        fillValue = -2;
        rawArray = getIntRawArray();
    }

    @Test
    public void testWindowCenter() throws Exception {
        final Array array = RawDataReader.read(3, 3, windowSize, fillValue, rawArray, 8);
        assertNotNull(array);
        assertEquals(int.class, array.getElementType());
        assertEquals(9, array.getSize());
//        final byte[] expecteds = {22, 32, 42, 23, 33, 43, 24, 34, 44};
//        final byte[] actuals = (byte[]) array.get1DJavaArray(array.getElementType());
//        assertArrayEquals(expecteds, actuals);
    }

    private Array getIntRawArray() {
        final int[] ints = {22, 23, 24, 25, 26, 27, 28, 29};
        return Array.factory(ints);
    }
}
