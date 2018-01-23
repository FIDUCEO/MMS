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

package com.bc.fiduceo.reader.amsu_mhs;


import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSUB_MHS_L1C_ReaderTest {

    @Test
    public void testGetRegEx() {
        final AMSUB_MHS_L1C_Reader reader = new AMSUB_MHS_L1C_Reader(new ReaderContext());

        final String regEx = reader.getRegEx();
        assertEquals("\\w*.+[AMBX|MHSX].+[A-Z0-9]{2,3}.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[A-Z]{2}(.[A-Z]\\d{7})?.h5", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("L0502033.NSS.AMBX.NK.D07234.S1004.E1149.B4821213.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("1893164003.NSS.MHSX.NN.D14302.S1252.E1447.B4865253.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("36321173.NSS.MHSX.NN.D15326.S1010.E1205.B5414142.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NP.D09244.S0301.E0448.B0291415.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("2153003623.NSS.MHSX.NP.D15356.S0408.E0603.B3540203.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.M2.D07173.S1835.E2016.B0349697.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("167601563.NSS.MHSX.NP.D11166.S1120.E1308.B1211415.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("163594483.NSS.MHSX.M2.D11150.S0413.E0557.B2391718.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.M1.D13040.S0054.E0237.B0205152.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("2270568163.NSS.AMBX.NK.D16069.S0519.E0713.B9267576.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("2236962993.NSS.AMBX.NK.D16046.S2133.E2327.B9235658.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("1531113603.NSS.AMBX.NL.D14103.S1227.E1419.B6989900.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("1606314663.NSS.AMBX.NL.D14143.S2252.E0047.B7047071.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("1629297203.NSS.AMBX.NL.D14156.S2004.E2158.B7065253.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("2256687383.NSS.MHSX.M1.D16060.S1721.E1817.B1790102.SV.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.AMBX.NL.D05248.S0003.E0143.B2553334.GC.L8746431.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        assertTrue(matcher.matches());


        matcher = pattern.matcher("19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,MetOpA+IASI_C_EUMP_20130528172543_34281_eps_o_l1.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testIsAmsub() throws IOException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Attribute attribute = mock(Attribute.class);
        when(netcdfFile.findGlobalAttribute("instrument")).thenReturn(attribute);

        when(attribute.getNumericValue()).thenReturn(11);
        assertTrue(AMSUB_MHS_L1C_Reader.isAmsub(netcdfFile));

        when(attribute.getNumericValue()).thenReturn(12);
        assertFalse(AMSUB_MHS_L1C_Reader.isAmsub(netcdfFile));

        when(attribute.getNumericValue()).thenReturn(108);

        try {
            AMSUB_MHS_L1C_Reader.isAmsub(netcdfFile);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testGetGroupName() {
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("btemps"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("chanqual"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("instrtemp"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("qualind"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("scanqual"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("scnlin"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("scnlindy"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("scnlintime"));
        assertEquals("Data", AMSUB_MHS_L1C_Reader.getGroupName("scnlinyr"));

        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Latitude"));
        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Longitude"));
        // notabene: some oddity in the input data contains the wrong spelling tb 2016-04-19
        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Satellite_azimith_angle"));
        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Satellite_zenith_angle"));
        // notabene: some oddity in the input data contains the wrong spelling tb 2016-04-19
        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Solar_azimith_angle"));
        assertEquals("Geolocation", AMSUB_MHS_L1C_Reader.getGroupName("Solar_zenith_angle"));
    }

    @Test
    public void testGetGroupName_invalidVariableName() {
        try {
            AMSUB_MHS_L1C_Reader.getGroupName("Der Messkanal");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetChannelLayer() {
        assertEquals(0, AMSUB_MHS_L1C_Reader.getChannelLayer("btemps_ch1"));
        assertEquals(0, AMSUB_MHS_L1C_Reader.getChannelLayer("chanqual_ch16"));

        assertEquals(1, AMSUB_MHS_L1C_Reader.getChannelLayer("chanqual_ch2"));
        assertEquals(1, AMSUB_MHS_L1C_Reader.getChannelLayer("btemps_ch17"));

        assertEquals(4, AMSUB_MHS_L1C_Reader.getChannelLayer("btemps_ch5"));
        assertEquals(4, AMSUB_MHS_L1C_Reader.getChannelLayer("chanqual_ch20"));
    }

    @Test
    public void testFalsifyAzimuth() {
        assertEquals("general_azimith", AMSUB_MHS_L1C_Reader.falsifyAzimuth("general_azimuth"));
    }

    @Test
    public void testCorrectAzimuth() {
        assertEquals("general_azimuth", AMSUB_MHS_L1C_Reader.correctAzimuth("general_azimith"));
    }

    @Test
    public void testGetLongitudeVariableName() {
        final AMSUB_MHS_L1C_Reader reader = new AMSUB_MHS_L1C_Reader(new ReaderContext());

        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final AMSUB_MHS_L1C_Reader reader = new AMSUB_MHS_L1C_Reader(new ReaderContext());

        assertEquals("Latitude", reader.getLatitudeVariableName());
    }
}
