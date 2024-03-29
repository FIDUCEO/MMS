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

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MxD35_ReaderTest {

    private MxD35_Reader reader;

    @Before
    public void setUp() {
        reader = new MxD35_Reader(new ReaderContext());
    }

    @Test
    public void testGetRegEx() {
        final String expected = "M([OY])D35_L2.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

        final String readerRexExp = reader.getRegEx();
        assertEquals(expected, readerRexExp);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("MOD35_L2.A2013037.1435.006.2015066015540.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MOD35_L2.A2017074.0815.006.2017074194513.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MYD35_L2.A2005144.0920.006.2014027110858.hdf");
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
        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("Latitude", reader.getLatitudeVariableName());
    }

    @Test
    public void testGetGroupName() {
        assertEquals("mod35/Geolocation_Fields", MxD35_Reader.getGroupName("Longitude"));
        assertEquals("mod35/Geolocation_Fields", MxD35_Reader.getGroupName("Latitude"));
        assertEquals("mod35/Data_Fields", MxD35_Reader.getGroupName("Sensor_Zenith"));
        assertEquals("mod35/Data_Fields", MxD35_Reader.getGroupName("Sensor_Zenith"));
        assertEquals("mod35/Data_Fields", MxD35_Reader.getGroupName("Sensor_Zenith"));
        assertEquals("mod35/Data_Fields", MxD35_Reader.getGroupName("Sensor_Zenith"));
        assertEquals("mod35/Data_Fields", MxD35_Reader.getGroupName("Sensor_Zenith"));
    }

    @Test
    public void testIs1KmVariable() {
        final Array array = mock(Array.class);

        when(array.getShape()).thenReturn(new int[] {100, 100, 34});
        assertFalse(MxD06_Reader.is1KmVariable(array));

        when(array.getShape()).thenReturn(new int[] {1, 1034, 209});
        assertTrue(MxD06_Reader.is1KmVariable(array));
    }

    @Test
    public void testStripLayerSuffix() {
        assertEquals("Quality_Assurance_5km", MxD06_Reader.stripLayerSuffix("Quality_Assurance_5km_03"));
        assertEquals("Quality_Assurance_5km", MxD06_Reader.stripLayerSuffix("Quality_Assurance_5km_09"));

        assertEquals("firlefanz", MxD06_Reader.stripLayerSuffix("firlefanz"));
        assertEquals("firlefanz_05_09", MxD06_Reader.stripLayerSuffix("firlefanz_05_09"));
    }

    @Test
    public void testExtractLayerIndex() {
        assertEquals(5, MxD06_Reader.extractLayerIndex("Quality_Assurance_5km_05"));
        assertEquals(9, MxD06_Reader.extractLayerIndex("Quality_Assurance_5km_09"));
    }

    @Test
    public void testExtractLayerIndex_incorrectVariableName() {
        try {
            MxD06_Reader.extractLayerIndex("a_strange_variable");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
