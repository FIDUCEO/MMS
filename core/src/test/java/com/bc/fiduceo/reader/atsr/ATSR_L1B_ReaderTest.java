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

package com.bc.fiduceo.reader.atsr;

import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ATSR_L1B_ReaderTest {

    @Test
    public void testGetRegEx() {
        final String expected = "AT(1|2|S)_TOA_1P[A-Z0-9]{4}\\d{8}_\\d{6}_\\d{12}_\\d{5}_\\d{5}_\\d{4}.(N|E)(1|2)";

        final ATSR_L1B_Reader reader = new ATSR_L1B_Reader(null);// we do not need a gemetry factory here tb 2016-08-10
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AT1_TOA_1PURAL19930805_210030_000000004015_00085_10751_0000.E1");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.OT");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetShape() {
        final Interval interval = new Interval(12, 23);

        final int[] shape = ATSR_L1B_Reader.getShape(interval);
        assertEquals(2, shape.length);
        assertEquals(23, shape[0]);
        assertEquals(12, shape[1]);
    }

    @Test
    public void testCreateReadingArray() {
        final int[] shape = {2, 3};

        Array array = ATSR_L1B_Reader.createReadingArray(DataType.FLOAT, shape);
        assertNotNull(array);
        assertEquals(DataType.FLOAT, array.getDataType());
        assertArrayEquals(shape, array.getShape());

        array = ATSR_L1B_Reader.createReadingArray(DataType.SHORT, shape);
        assertNotNull(array);
        assertEquals(DataType.INT, array.getDataType());
        assertArrayEquals(shape, array.getShape());
    }

    @Test
    public void testCreateReadingArray_unsupported() {
        final int[] shape = {2, 3};

        try {
            ATSR_L1B_Reader.createReadingArray(DataType.DOUBLE, shape);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }

        try {
            ATSR_L1B_Reader.createReadingArray(DataType.LONG, shape);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }
}
