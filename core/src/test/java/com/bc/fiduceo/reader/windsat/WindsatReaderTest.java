package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class WindsatReaderTest {

    private WindsatReader reader;

    @Before
    public void setUp() {
        reader = new WindsatReader(new ReaderContext()); // empty context sufficient in this test tb 2022-11-17
    }

    @Test
    public void testGetRegEx() {
        final String expected = "RSS_WindSat_TB_L1C_r\\d{5}_\\d{8}T\\d{6}_\\d{7}_V\\d{2}.\\d.nc";

        final String regEx = reader.getRegEx();
        assertEquals(expected, regEx);

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("RSS_WindSat_TB_L1C_r71126_20161001T095140_2016275_V08.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.DBL.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("RSS_SMAP_SSS_L2C_r16092_20180204T202311_2018035_FNL_V05.0.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("latitude", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("RSS_WindSat_TB_L1C_r74877_20170622T215510_2017173_V08.0.nc");
        assertArrayEquals(new int[]{2017, 6, 22}, ymd);

        ymd = reader.extractYearMonthDayFromFilename("RSS_WindSat_TB_L1C_r80689_20180806T174232_2018218_V08.0.nc");
        assertArrayEquals(new int[]{2018, 8, 6}, ymd);
    }
}
