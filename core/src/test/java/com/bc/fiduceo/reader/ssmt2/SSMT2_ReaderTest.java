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

package com.bc.fiduceo.reader.ssmt2;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SSMT2_ReaderTest {

    private SSMT2_Reader reader;

    @Before
    public void setUp() {
        reader = new SSMT2_Reader(new ReaderContext());
    }

    @Test
    public void testAssembleDateString() {
        assertEquals("2006-08-22T14:22:45", SSMT2_Reader.assembleDateString("2006-08-22", "14:22:45.012345"));
        assertEquals("1993-11-02T03:39:22", SSMT2_Reader.assembleDateString("1993-11-02", "03:39:22.654321"));
    }

    @Test
    public void testGetRegEx() {
        final String expected = "F(11|12|14|15)[0-9]{12}.nc";

        assertEquals(expected,reader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("F11199401280412.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("F14200106141229.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("lon", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("lat", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("F11199401280412.nc");
        assertArrayEquals(new int[]{1994, 1, 28}, ymd);

        ymd = reader.extractYearMonthDayFromFilename("F14200106141229.nc");
        assertArrayEquals(new int[]{2001, 6, 14}, ymd);
    }
}
