package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MxD021KM_ReaderTest {

    private MxD021KM_Reader reader;

    @Before
    public void setUp() {
        reader = new MxD021KM_Reader(new ReaderContext());
    }

    @Test
    public void testGetRegEx() {
        final String expected = "M([OY])D021KM.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

        final String readerRexExp = reader.getRegEx();
        assertEquals(expected, readerRexExp);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("MOD021KM.A2003142.1445.061.2017194130122.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MYD021KM.A2011168.2210.061.2018032001033.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MOD06_L2.A2017074.0815.006.2017074194513.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MYD06_L2.A2005144.0920.006.2014027110858.hdf");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190546533.NSS.HIRX.NL.D11235.S1235.E1422.B5628788.WI.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("AMSR_E_L2A_BrightnessTemperatures_V12_200502161217_A.hdf");
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
    public void testExtractYMDFromFileName() {
        int[] ymd = reader.extractYearMonthDayFromFilename("MYD021KM.A2011168.2210.061.2018032001033.hdf");
        assertEquals(2011, ymd[0]);
        assertEquals(6, ymd[1]);
        assertEquals(17, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("MOD021KM.A2003142.1445.061.2017194130122.hdf");
        assertEquals(2003, ymd[0]);
        assertEquals(5, ymd[1]);
        assertEquals(22, ymd[2]);
    }

    @Test
    public void testGetLayerIndex_250m() {
        assertEquals(0, MxD021KM_Reader.getLayerIndex("EV_250_Aggr1km_RefSB_ch01"));
        assertEquals(1, MxD021KM_Reader.getLayerIndex("EV_250_Aggr1km_RefSB_Uncert_Indexes_ch02"));
    }

    @Test
    public void testGetLayerIndex_500m() {
        assertEquals(2, MxD021KM_Reader.getLayerIndex("EV_500_Aggr1km_RefSB_Samples_Used_ch05"));
        assertEquals(4, MxD021KM_Reader.getLayerIndex("EV_500_Aggr1km_RefSB_Uncert_Indexes_ch07"));
    }

    @Test
    public void testGetLayerIndex_1km_refl() {
        assertEquals(4, MxD021KM_Reader.getLayerIndex("EV_1KM_RefSB_ch12"));
        assertEquals(5, MxD021KM_Reader.getLayerIndex("EV_1KM_RefSB_ch13L"));
        assertEquals(8, MxD021KM_Reader.getLayerIndex("EV_1KM_RefSB_ch14H"));
        assertEquals(10, MxD021KM_Reader.getLayerIndex("EV_1KM_RefSB_ch16"));
        assertEquals(14, MxD021KM_Reader.getLayerIndex("EV_1KM_RefSB_ch26"));
    }

    @Test
    public void testGetLayerIndex_1km_emissive() {
        assertEquals(0, MxD021KM_Reader.getLayerIndex("EV_1KM_Emissive_ch20"));
        assertEquals(5, MxD021KM_Reader.getLayerIndex("EV_1KM_Emissive_ch25"));
        assertEquals(6, MxD021KM_Reader.getLayerIndex("EV_1KM_Emissive_ch27"));
        assertEquals(12, MxD021KM_Reader.getLayerIndex("EV_1KM_Emissive_ch33"));
    }

    @Test
    public void testGetLayerIndex_unlayeredVariable() {
        assertEquals(0, MxD021KM_Reader.getLayerIndex("an_arbitrary_variable_without_layers"));
    }

    @Test
    public void testGetScaleFactorAttributeName() {
        assertEquals("radiance_scales", MxD021KM_Reader.getScaleFactorAttributeName("EV_250_Aggr1km_RefSB_ch01"));
        assertEquals("scaling_factor", MxD021KM_Reader.getScaleFactorAttributeName("EV_250_Aggr1km_RefSB_Uncert_Indexes_ch02"));
        assertEquals("radiance_scales", MxD021KM_Reader.getScaleFactorAttributeName("EV_1KM_Emissive_ch23"));
    }

    @Test
    public void testGetOffsetAttributeName() {
        assertEquals("radiance_offsets", MxD021KM_Reader.getOffsetAttributeName("EV_250_Aggr1km_RefSB_ch01"));
        assertNull(MxD021KM_Reader.getOffsetAttributeName("EV_250_Aggr1km_RefSB_Uncert_Indexes_ch02"));
        assertEquals("radiance_offsets", MxD021KM_Reader.getOffsetAttributeName("EV_1KM_Emissive_ch24"));
    }
}
