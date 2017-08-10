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

package com.bc.fiduceo.reader.modis;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MxD06_ReaderTest {

    @Test
    public void testGetRegEx() {
        final String expected = "M([OY])D06_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

        final MxD06_Reader reader = new MxD06_Reader(null); // we do not need a geometry factory for this test tb 2017-05-30
        final String readerRexExp = reader.getRegEx();
        assertEquals(expected, readerRexExp);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("MOD06_L2.A2013037.1435.006.2015066015540.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MOD06_L2.A2017074.0815.006.2017074194513.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MYD06_L2.A2005144.0920.006.2014027110858.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("190546533.NSS.HIRX.NL.D11235.S1235.E1422.B5628788.WI.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502161217_A.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final MxD06_Reader reader = new MxD06_Reader(null); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final MxD06_Reader reader = new MxD06_Reader(null); // we do not need a geometry factory for this test tb 2017-08-10

        assertEquals("Latitude", reader.getLatitudeVariableName());
    }
}
