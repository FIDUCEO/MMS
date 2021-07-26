package com.bc.fiduceo.reader.insitu.sirds_sst;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SirdsInsituReaderTest {

    @Test
    public void testGetRegEx_mooring() {
        final SirdsInsituReader insituReader = new SirdsInsituReader("mooring-sirds");
        final String expected = "SSTCCI2_refdata_mooring_\\d{6}.nc";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("SSTCCI2_refdata_mooring_202005.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_bottle_198904.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("insitu_5_WMOID_5901880_20100514_20100627.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetRegEx_argosurf() {
        final SirdsInsituReader insituReader = new SirdsInsituReader("argosurf-sirds");
        final String expected = "SSTCCI2_refdata_argo_surf_\\d{6}.nc";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("SSTCCI2_refdata_argo_surf_202005.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SSTCCI2_refdata_bottle_198904.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("insitu_5_WMOID_5901880_20100514_20100627.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final SirdsInsituReader insituReader = new SirdsInsituReader("whatever");

        assertEquals("longitude", insituReader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final SirdsInsituReader insituReader = new SirdsInsituReader("whatever");

        assertEquals("latitude", insituReader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final SirdsInsituReader insituReader = new SirdsInsituReader("whatever");

        int[] ymd = insituReader.extractYearMonthDayFromFilename("SSTCCI2_refdata_drifter_201304.nc");
        assertEquals(3, ymd.length);
        assertEquals(2013, ymd[0]);
        assertEquals(4, ymd[1]);
        assertEquals(1, ymd[2]);

        ymd = insituReader.extractYearMonthDayFromFilename("SSTCCI2_refdata_mooring_201602.nc");
        assertEquals(3, ymd.length);
        assertEquals(2016, ymd[0]);
        assertEquals(2, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testToRegExPart() {
        assertEquals("argo", SirdsInsituReader.toRegExPart("argo-sirds"));
        assertEquals("argo_surf", SirdsInsituReader.toRegExPart("argosurf-sirds"));
        assertEquals("drifter", SirdsInsituReader.toRegExPart("drifter-sirds"));
        assertEquals("drifter_cmems", SirdsInsituReader.toRegExPart("driftercmems-sirds"));
    }

    @Test
    public void testToFileName() {
        assertEquals("DEPTH", SirdsInsituReader.toFileName("depth"));
        assertEquals("DEPTH_CORR", SirdsInsituReader.toFileName("depth_corr"));
        assertEquals("QC1", SirdsInsituReader.toFileName("qc1"));
    }

    @Test
    public void testToMMSName() {
        assertEquals("latitude", SirdsInsituReader.toMMSName("LATITUDE"));
        assertEquals("ob_id", SirdsInsituReader.toMMSName("OB_ID"));
        assertEquals("qc2", SirdsInsituReader.toMMSName("QC2"));
    }
}
