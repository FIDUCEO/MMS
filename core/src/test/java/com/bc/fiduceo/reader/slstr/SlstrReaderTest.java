package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SlstrReaderTest {

    @Test
    public void testGetRegEx() {
        final String expected = "S3([AB])_SL_1_RBT_.*(.SEN3|zip)?";

        final SlstrReader reader = new SlstrReader(new ReaderContext());// we do not need a gemetry factory here tb 2019-05-10
        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("S3A_SL_1_RBT____20181026T231611_20181026T231911_20181028T023445_0180_037_187_0900_LN2_O_NT_003.zip");
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
        final SlstrReader reader = new SlstrReader(new ReaderContext());// we do not need a gemetry factory here tb 2019-05-10

        assertEquals("longitude_tx", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        final SlstrReader reader = new SlstrReader(new ReaderContext());// we do not need a gemetry factory here tb 2019-05-10

        assertEquals("latitude_tx", reader.getLatitudeVariableName());
    }

    @Test
    public void testSubsampleTimes() {
        final long[] full_times = {12L, 13L, 14L, 15L, 16L, 17L};
        final long[] subs_times = {12L, 14L, 16L};

        assertArrayEquals(subs_times, SlstrReader.subSampleTimes(full_times));
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final SlstrReader reader = new SlstrReader(new ReaderContext());// we do not need a gemetry factory here tb 2019-05-27

        final int[] ymd = reader.extractYearMonthDayFromFilename("S3A_SL_1_RBT____20181013T222436_20181013T222736_20181015T035102_0179_037_001_1620_LN2_O_NT_003.SEN3");
        assertEquals(2018, ymd[0]);
        assertEquals(10, ymd[1]);
        assertEquals(13, ymd[2]);
    }
}
