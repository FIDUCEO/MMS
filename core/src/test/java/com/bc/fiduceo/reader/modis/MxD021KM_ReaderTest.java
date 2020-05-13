package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MxD021KM_ReaderTest {

    @Test
    public void testGetRegEx() {
        final String expected = "M([OY])D021KM.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

        final MxD021KM_Reader reader = new MxD021KM_Reader(new ReaderContext()); // we do not need a geometry factory for this test tb 2020-05-13
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
        final MxD021KM_Reader reader = new MxD021KM_Reader(new ReaderContext()); // we do not need a geometry factory for this test tb 2020-05-13

        assertEquals("Longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final MxD021KM_Reader reader = new MxD021KM_Reader(new ReaderContext()); // we do not need a geometry factory for this test tb 2020-05-13

        assertEquals("Latitude", reader.getLatitudeVariableName());
    }
}
