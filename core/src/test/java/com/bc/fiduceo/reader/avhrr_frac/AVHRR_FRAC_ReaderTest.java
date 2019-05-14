package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AVHRR_FRAC_ReaderTest {

    @Test
    public void testGetRegEx() {
        final String expected = "NSS.FRAC.M2.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.[A-Z]{2,2}(.gz){0,1}";

        final AVHRR_FRAC_Reader reader = new AVHRR_FRAC_Reader(new ReaderContext());// we do not need a geometry factory here tb 2019-01-16
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("NSS.FRAC.M2.D18131.S1404.E1544.B5998081.SV");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.FRAC.M2.D17235.S1549.E1730.B5627475.SV");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.FRAC.M2.D19004.S1511.E1652.B6336263.SV");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.FRAC.M2.D12002.S1611.E1752.B2700708.SV.gz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.FRAC.M2.D12001.S0121.E0210.B2698484.MM.gz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.OT");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final AVHRR_FRAC_Reader reader = new AVHRR_FRAC_Reader(new ReaderContext());// we do not need a geometry factory here tb 2019-01-16

        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final AVHRR_FRAC_Reader reader = new AVHRR_FRAC_Reader(new ReaderContext());// we do not need a geometry factory here tb 2019-01-16

        assertEquals("latitude", reader.getLatitudeVariableName());
    }
}
