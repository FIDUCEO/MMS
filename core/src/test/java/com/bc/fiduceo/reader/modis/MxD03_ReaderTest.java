package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MxD03_ReaderTest {

    private MxD03_Reader reader;

    @Before
    public void setUp() {
        reader = new MxD03_Reader(new ReaderContext());
    }

    @Test
    public void testGetRegEx() {
        final String expected = "M([OY])D03.A\\d{7}.\\d{4}.\\d{3}.\\d{13}.hdf";

        final String readerRexExp = reader.getRegEx();
        assertEquals(expected, readerRexExp);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("MYD03.A2011168.2210.061.2018030150511.hdf");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("MOD03.A2003142.1445.061.2017192042416.hdf");
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
    public void testGetGroupName() {
        assertEquals("MODIS_Swath_Type_GEO/Geolocation_Fields", MxD03_Reader.getGroupName("Latitude"));
        assertEquals("MODIS_Swath_Type_GEO/Data_Fields", MxD03_Reader.getGroupName("Height"));
        assertNull(MxD03_Reader.getGroupName("Scan_number"));
    }
}
