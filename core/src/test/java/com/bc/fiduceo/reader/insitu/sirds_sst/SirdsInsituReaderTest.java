package com.bc.fiduceo.reader.insitu.sirds_sst;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SirdsInsituReaderTest {

    private SirdsInsituReader insituReader;

    @Before
    public void setUp() {
        insituReader = new SirdsInsituReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "SSTCCI2_refdata_[a-z]+(_[a-z]+)?_\\d{6}.nc";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("SSTCCI2_refdata_argosurf_202005.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_bottle_198904.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_drifter_198802.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_drifter_cmems_201801.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_argosurf_201712.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("insitu_5_WMOID_5901880_20100514_20100627.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("insitu_9_WMOID_14456569_19980913_19981123.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("insitu_10_WMOID_9733500_19840123_19840404.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("LONGITUDE", insituReader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("LATITUDE", insituReader.getLatitudeVariableName());
    }
}
