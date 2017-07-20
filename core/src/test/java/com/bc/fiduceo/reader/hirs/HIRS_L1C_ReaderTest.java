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

package com.bc.fiduceo.reader.hirs;


import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

import org.junit.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HIRS_L1C_ReaderTest {

    private HIRS_L1C_Reader reader;

    @Before
    public void setUp() throws Exception {
        reader = new HIRS_L1C_Reader(null);    // we do not need a geometry factory for this test tb 2016-08-02
    }

    @Test
    public void testGetRegEx() {
        final String expected = "(\\w*.)?[A-Z]{3}.HIRX.[A-Z0-9]{2}.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.[A-Z]{2}.nc";

        final String regEx = reader.getRegEx();
        assertEquals(expected, regEx);

        final Pattern pattern = Pattern.compile(regEx);

        Matcher matcher = pattern.matcher("NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("1971928533.NSS.HIRX.M1.D15203.S1216.E1311.B1474445.SV.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.NC.D83089.S0758.E0944.B0910405.WI.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("L0522933.NSS.AMBX.NK.D07234.S1640.E1824.B4821617.GC.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() throws Exception {
        String hirsFileName;
        int[] ymd;

        hirsFileName = "189800453.NSS.HIRX.NN.D11233.S0808.E1003.B3221112.GC.nc";
        ymd = reader.extractYearMonthDayFromFilename(hirsFileName);
        assertArrayEquals(new int[]{2011, 8, 21}, ymd);

        hirsFileName = "191062833.NSS.HIRX.NN.D88123.S1356.E1551.B3227172.WI.nc";
        ymd = reader.extractYearMonthDayFromFilename(hirsFileName);
        assertArrayEquals(new int[]{1988, 5, 2}, ymd);
    }
}
