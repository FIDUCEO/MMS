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

package com.bc.fiduceo.reader.insitu.sst_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SSTInsituReaderTest {

    private SSTInsituReader insituReader;

    @Before
    public void setUp() {
        insituReader = new SSTInsituReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "insitu_[0-9][0-9]?_WMOID_[^_]+_[12][09]\\d{2}[01]\\d[0123]\\d_[12][09]\\d{2}[01]\\d[0123]\\d.nc";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("insitu_0_WMOID_51993_20040402_20060207.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_2_WMOID_ZXCS_19890623_19890626.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_3_WMOID_13001_20060608_20131126.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_3_WMOID_51019_19910722_20120610.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_4_WMOID_LeSuroit_20070110_20070218.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_5_WMOID_5901880_20100514_20100627.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_9_WMOID_14456569_19980913_19981123.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_10_WMOID_9733500_19840123_19840404.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_11_WMOID_370055_19810820_19810901.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_12_WMOID_Q9900579_20130401_20130812.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_13_WMOID_1983407_19880404_19880406.nc");
        assertTrue(matcher.matches());


        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("insitu.lon", insituReader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("insitu.lat", insituReader.getLatitudeVariableName());
    }

    @Test
    public void testCreateIdArray() {
        final int[] mohc_ids = {5674, 11245667, -32768, 10062352, -32768};
        final Array mohcArray = NetCDFUtils.create(mohc_ids);

        final int[] times = {1112752200, 1116097799, 1118170800, 1123031999, 1123659600};
        final Array timeArray = NetCDFUtils.create(times);

        final Array idArray = SSTInsituReader.createIdArray(mohcArray, timeArray, -32768);
        assertNotNull(idArray);
        assertEquals(2013040000005674L, idArray.getLong(0));
        assertEquals(2013050011245667L, idArray.getLong(1));
        assertEquals(-32768, idArray.getLong(2));
        assertEquals(2013080010062352L, idArray.getLong(3));
        assertEquals(-32768, idArray.getLong(4));
    }
}
