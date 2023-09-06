package com.bc.fiduceo.reader.smap;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SmapReaderTest {

    private SmapReader reader;

    @Before
    public void setUp() {
        reader = new SmapReader(new ReaderContext(), 1); // empty context sufficient in this test tb 2022-11-17
    }

    @Test
    public void testGetRegEx() {
        final String expected = "RSS_SMAP_SSS_L2C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_FNL_V05\\.0\\.nc";

        final String regEx = reader.getRegEx();
        assertEquals(expected, regEx);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("RSS_SMAP_SSS_L2C_r71126_20161001T095140_2016275_FNL_V05.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("RSS_SMAP_SSS_L2C_r16092_20180204T202311_2018035_FNL_V05.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.DBL.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("cellon", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("cellat", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("RSS_SMAP_SSS_L2C_r16092_20180224T202311_2018035_FNL_V05.0.nc");
        assertArrayEquals(new int[]{2018, 2, 24}, ymd);

        ymd = reader.extractYearMonthDayFromFilename("RSS_SMAP_SSS_L2C_r71126_20161001T095140_2016275_FNL_V05.0.nc");
        assertArrayEquals(new int[]{2016, 10, 01}, ymd);
    }
}
