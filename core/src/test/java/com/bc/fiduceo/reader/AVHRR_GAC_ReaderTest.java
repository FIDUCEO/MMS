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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.BcGeometryCollection;
import com.bc.fiduceo.geometry.Geometry;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AVHRR_GAC_ReaderTest {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        reader = new AVHRR_GAC_Reader();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] sensorKeys = reader.getSupportedSensorKeys();
        assertNotNull(sensorKeys);
        assertEquals(16, sensorKeys.length);
        assertEquals("avhrr-n06", sensorKeys[0]);
        assertEquals("avhrr-n14", sensorKeys[8]);
        assertEquals("avhrr-m02", sensorKeys[15]);
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.AMBX.NK.D15365.S1249.E1420.B9169697.GC");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L8912163.NSS.AMBX.NK.D08001.S0000.E0155.B5008586.GC.gz.l1c.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void testParseDateAttribute() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("20060526T054530Z");

        final Date date = AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
        TestUtil.assertCorrectUTCDate(2006, 5, 26, 5, 45, 30, date);
    }

    @Test
    public void testParseDateAttribute_NullAttribute() throws Exception {
        try {
            AVHRR_GAC_Reader.parseDateAttribute(null);
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Return_Null_Value() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("");

        try {
            AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Unparseable_Attribute() throws Exception {
        final Attribute timeAttribute = mock(Attribute.class);
        when(timeAttribute.getStringValue()).thenReturn("234390123T77");

        try {
            AVHRR_GAC_Reader.parseDateAttribute(timeAttribute);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testGetLongitudes() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Variable variable = mock(Variable.class);
        final Array array = mock(Array.class);

        when(variable.read()).thenReturn(array);
        when(netcdfFile.findVariable("lon")).thenReturn(variable);

        final Array longitudes = AVHRR_GAC_Reader.getLongitudes(netcdfFile);
        assertNotNull(longitudes);
    }

    @Test
    public void testGetLongitudes_missingVariable() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        when(netcdfFile.findVariable("lon")).thenReturn(null);

        try {
            AVHRR_GAC_Reader.getLongitudes(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetLatitudes() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Variable variable = mock(Variable.class);
        final Array array = mock(Array.class);

        when(variable.read()).thenReturn(array);
        when(netcdfFile.findVariable("lat")).thenReturn(variable);

        final Array latitudes = AVHRR_GAC_Reader.getLatitudes(netcdfFile);
        assertNotNull(latitudes);
    }

    @Test
    public void testCheckForValidity_valid() {
        final Geometry geometry_1 = mock(Geometry.class);
        when(geometry_1.isValid()).thenReturn(true);

        final Geometry geometry_2 = mock(Geometry.class);
        when(geometry_2.isValid()).thenReturn(true);

        final BcGeometryCollection geometryCollection = createGeometryCollection(geometry_1, geometry_2);

        try {
            AVHRR_GAC_Reader.checkForValidity(geometryCollection);
        } catch (Exception e) {
            fail("No exception expected");
        }
    }

    @Test
    public void testCheckForValidity_invalid() {
        final Geometry geometry_1 = mock(Geometry.class);
        when(geometry_1.isValid()).thenReturn(true);

        final Geometry geometry_2 = mock(Geometry.class);
        when(geometry_2.isValid()).thenReturn(false);

        final BcGeometryCollection geometryCollection = createGeometryCollection(geometry_1, geometry_2);

        try {
            AVHRR_GAC_Reader.checkForValidity(geometryCollection);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    BcGeometryCollection createGeometryCollection(Geometry geometry_1, Geometry geometry_2) {
        final Geometry[] geometries = new Geometry[]{geometry_1, geometry_2};
        final BcGeometryCollection geometryCollection = new BcGeometryCollection();
        geometryCollection.setGeometries(geometries);
        return geometryCollection;
    }
}
