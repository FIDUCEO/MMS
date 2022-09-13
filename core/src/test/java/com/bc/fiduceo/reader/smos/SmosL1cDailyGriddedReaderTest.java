package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.reader.Reader;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SmosL1cDailyGriddedReaderTest {

    @Test
    public void testGetRegEx() {
        final Reader reader = new SmosL1CDailyGriddedReader(null); // this test does not require a context class tb 2022-09-13

        final String expected = "SM_RE07_MIR_CDF3T[AD]_(\\d{8}T\\d{6}_){2}\\d{3}_\\d{3}_\\d{1}.tgz";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher("SM_RE07_MIR_CDF3TA_20160610T000000_20160610T235959_330_001_7.tgz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }
}
