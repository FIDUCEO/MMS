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

package com.bc.fiduceo.reader.avhrr_gac;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class AVHRR_GAC_ReaderTest {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        reader = new AVHRR_GAC_Reader(new GeometryFactory(GeometryFactory.Type.S2));
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
        final Date date = AVHRR_GAC_Reader.parseDate("20060526T054530Z");
        TestUtil.assertCorrectUTCDate(2006, 5, 26, 5, 45, 30, date);
    }

    @Test
    public void testParseDateAttribute_NullAttribute() throws Exception {
        try {
            AVHRR_GAC_Reader.parseDate(null);
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Return_Null_Value() throws Exception {
        try {
            AVHRR_GAC_Reader.parseDate("");
            fail("IO Exception expected");
        } catch (IOException e) {
        }
    }

    @Test
    public void testParseDateAttribute_Unparseable_Attribute() throws Exception {
        try {
            AVHRR_GAC_Reader.parseDate("234390123T77");
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testConvertToAcquisitionTime_1970_01_01() throws Exception {
        final int startTimeMilliSecondsSince1970 = 0;
        final ArrayFloat.D2 rawData = (ArrayFloat.D2) Array.factory(new float[][]{
                new float[]{1.1f, 2.2f, 3.3f},
                new float[]{4.4f, 5.5f, 6.6f},
        });
        final int[] expectedSeconds = {1, 2, 3, 4, 6, 7};

        // test
        final Array acquisitionTime = AVHRR_GAC_Reader.convertToAcquisitionTime(rawData, startTimeMilliSecondsSince1970, -128.5f);

        // verifiying
        assertNotNull(acquisitionTime);
        assertArrayEquals(expectedSeconds, (int[]) acquisitionTime.getStorage());
    }

    @Test
    public void testConvertToAcquisitionTime_1970_01_01_useFillValue() throws Exception {
        final int startTimeMilliSecondsSince1970 = 0;
        final ArrayFloat.D2 rawData = (ArrayFloat.D2) Array.factory(new float[][]{
                new float[]{1.1f, 2.2f, -19.7f},
                new float[]{4.4f, 5.5f, -19.7f},
        });
        final int[] expectedSeconds = {1, 2, -2147483647, 4, 6, -2147483647};

        // test
        final Array acquisitionTime = AVHRR_GAC_Reader.convertToAcquisitionTime(rawData, startTimeMilliSecondsSince1970, -19.7f);

        // verifiying
        assertNotNull(acquisitionTime);
        assertArrayEquals(expectedSeconds, (int[]) acquisitionTime.getStorage());
    }

    @Test
    public void testConvertToAcqusitionTime_2015_03_23() throws Exception {
        final ProductData.UTC startUTC = ProductData.UTC.parse("2015-03-23 12:34:56", "yyyy-MM-dd HH:mm:ss");
        final long startTimeMilliSecondsSince1970 = startUTC.getAsDate().getTime();
        final int v = (int) (startTimeMilliSecondsSince1970 * 0.001);
        final ArrayFloat.D2 rawData = (ArrayFloat.D2) Array.factory(new float[][]{
                new float[]{1.1f, 2.2f, 3.3f},
                new float[]{4.4f, 5.5f, 6.6f},
        });
        final int[] expectedSeconds = {1 + v, 2 + v, 3 + v, 4 + v, 6 + v, 7 + v};

        // test
        final Array aquisitionTime = AVHRR_GAC_Reader.convertToAcquisitionTime(rawData, startTimeMilliSecondsSince1970, -128.5f);

        // verifiying
        assertEquals(1427114096000L, startTimeMilliSecondsSince1970);
        assertNotNull(aquisitionTime);
        assertArrayEquals(expectedSeconds, (int[]) aquisitionTime.getStorage());
    }

    @Test
    public void testGetProductWidth() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        Dimension dimension = mock(Dimension.class);
        when(dimension.getFullName()).thenReturn("ni");
        when(dimension.getLength()).thenReturn(108);
        ArrayList<Dimension> dimensionList = new ArrayList<>();
        dimensionList.add(dimension);
        when(netcdfFile.getDimensions()).thenReturn(dimensionList);

        assertEquals(108, AVHRR_GAC_Reader.getProductWidth(netcdfFile));
    }

    @Test
    public void testGetProductWidth_dimensionMissing() {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension dimension = mock(Dimension.class);
        when(dimension.getFullName()).thenReturn("theWrongOne");
        when(dimension.getLength()).thenReturn(2008);
        final ArrayList<Dimension> dimensionList = new ArrayList<>();
        dimensionList.add(dimension);
        when(netcdfFile.getDimensions()).thenReturn(dimensionList);

        try {
            AVHRR_GAC_Reader.getProductWidth(netcdfFile);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetSecondsSince1970() {
         assertEquals(0, AVHRR_GAC_Reader.getSecondsSince1970(0, 0));
         assertEquals(1, AVHRR_GAC_Reader.getSecondsSince1970(1000, 0));
         assertEquals(2, AVHRR_GAC_Reader.getSecondsSince1970(0, 2));
         assertEquals(4, AVHRR_GAC_Reader.getSecondsSince1970(2000, 2));
    }
}
