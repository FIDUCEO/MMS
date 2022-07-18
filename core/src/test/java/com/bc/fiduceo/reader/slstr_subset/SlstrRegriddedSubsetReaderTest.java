package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.slstr.ProductType;
import com.bc.fiduceo.reader.slstr.SlstrReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SlstrRegriddedSubsetReaderTest {

    @Test
    public void testExtractName() {
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal\\welcher\\pfad\\name.nc"));
        assertEquals("name.nc", SlstrRegriddedSubsetReader.extractName("egal/welcher/pfad/name.nc"));
    }

    @Test
    public void testGetRegEx() {
        final SlstrRegriddedSubsetReader reader = new SlstrRegriddedSubsetReader(null, true);

        final String expected = "S3[AB]_SL_1_RBT____(\\d{8}T\\d{6}_){3}\\d{4}(_\\d{3}){2}_\\d{4}_LN2_O_NT_\\d{3}(.SEN3|zip)";
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("FIDUCEO_FCDR_L1C_AVHRR_N19ALL_20110705055721_20110705073927_EASY_v0.2Bet_fv2.0.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        final Reader reader = new SlstrRegriddedSubsetReader(new ReaderContext(), true);

        assertEquals("longitude_tx", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final Reader reader = new SlstrRegriddedSubsetReader(new ReaderContext(), false);

        assertEquals("latitude_tx", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final SlstrRegriddedSubsetReader reader = new SlstrRegriddedSubsetReader(null, false);

        int[] ymd = reader.extractYearMonthDayFromFilename("S3B_SL_1_RBT____20191117T231801_20191117T232101_20191119T035119_0180_032_172_5400_LN2_O_NT_003.SEN3");
        assertEquals(3, ymd.length);
        assertEquals(2019, ymd[0]);
        assertEquals(11, ymd[1]);
        assertEquals(17, ymd[2]);

        ymd = reader.extractYearMonthDayFromFilename("S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.zip");
        assertEquals(3, ymd.length);
        assertEquals(2020, ymd[0]);
        assertEquals(5, ymd[1]);
        assertEquals(22, ymd[2]);
    }
}