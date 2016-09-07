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

package com.bc.fiduceo.reader.amsre;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.nc2.Attribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSRE_ReaderTest {

    @Test
    public void testAssembleUtcString() {
        final String dateString = "2005-02-17";
        final String timeString = "04:46:34.83Z";

        assertEquals("2005-02-17T04:46:34", AMSRE_Reader.assembleUTCString(dateString, timeString));
    }

    @Test
    public void testGetUtcDate() throws IOException {
        final String rangeStartDate = "2005-02-17";
        final String rangeStartTime = "06:25:56.89Z";

        final Attribute startDateAttribute = mock(Attribute.class);
        when(startDateAttribute.getStringValue()).thenReturn(rangeStartDate);

        final Attribute startTimeAttribute = mock(Attribute.class);
        when(startTimeAttribute.getStringValue()).thenReturn(rangeStartTime);

        final ProductData.UTC sensingStart = AMSRE_Reader.getUtcData(startDateAttribute, startTimeAttribute);
        TestUtil.assertCorrectUTCDate(2005, 2, 17, 6, 25, 56, sensingStart.getAsDate());
    }

    @Test
    public void testGetUtcDate_wrongFormat() throws IOException {
        final String rangeStartDate = "you_cant";
        final String rangeStartTime = "parse_this";

        final Attribute startDateAttribute = mock(Attribute.class);
        when(startDateAttribute.getStringValue()).thenReturn(rangeStartDate);

        final Attribute startTimeAttribute = mock(Attribute.class);
        when(startTimeAttribute.getStringValue()).thenReturn(rangeStartTime);

        try {
            AMSRE_Reader.getUtcData(startDateAttribute, startTimeAttribute);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testAssignNodeType() {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        AMSRE_Reader.assignNodeType(acquisitionInfo, "Ascending");
        assertEquals(NodeType.ASCENDING, acquisitionInfo.getNodeType());

        AMSRE_Reader.assignNodeType(acquisitionInfo, "Descending");
        assertEquals(NodeType.DESCENDING, acquisitionInfo.getNodeType());

        AMSRE_Reader.assignNodeType(acquisitionInfo, "quer");
        assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
    }

    @Test
    public void testGetGroupNameForVariable() {
        assertEquals("Low_Res_Swath/Geolocation_Fields", AMSRE_Reader.getGroupNameForVariable("Time"));
        assertEquals("Low_Res_Swath/Geolocation_Fields", AMSRE_Reader.getGroupNameForVariable("Latitude"));
        assertEquals("Low_Res_Swath/Geolocation_Fields", AMSRE_Reader.getGroupNameForVariable("Longitude"));

        assertEquals("Low_Res_Swath/Data_Fields", AMSRE_Reader.getGroupNameForVariable("Earth_Incidence"));
        assertEquals("Low_Res_Swath/Data_Fields", AMSRE_Reader.getGroupNameForVariable("36.5H_Res.1_TB"));
    }

    @Test
    public void testGetLayerIndexFromChannelFlagName() {
        assertEquals(0, AMSRE_Reader.getLayerIndexFromChannelFlagName("Channel_Quality_Flag_6V"));
        assertEquals(3, AMSRE_Reader.getLayerIndexFromChannelFlagName("Channel_Quality_Flag_10H"));
        assertEquals(11, AMSRE_Reader.getLayerIndexFromChannelFlagName("Channel_Quality_Flag_89H"));
    }

    @Test
    public void testGetLayerIndexFromChannelFlagName_invalidExtension() {
        try {
            AMSRE_Reader.getLayerIndexFromChannelFlagName("Channel_Quality_Flag_made_up");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetRegEx() {
         final String expected = "AMSR_E_L2A_BrightnessTemperatures_V\\d{2}_\\d{12}_[A-Z]{1}.hdf";

        final AMSRE_Reader reader = new AMSRE_Reader(null); // we do not need a geometry factory for this test tb 2016-09-07
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200607211944_A.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_201011230036_D.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.E2");
        assertFalse(matcher.matches());
    }
}
