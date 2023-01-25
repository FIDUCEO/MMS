package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.insitu.sirds_sst.SirdsInsituReader;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;
import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("resource")
public class NdbcReaderTest {

    @Test
    public void testGetRegEx_constant_wind() {
        final NdbcInsituReader insituReader = new NdbcInsituReader(CONSTANT_WIND);
        final String expected = "\\w{5}c\\d{4}.txt";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("41025c2017.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("mdrm1c2017.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("41002h2018.txt");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2016-N.text");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetRegEx_standard_meteorological() {
        final NdbcInsituReader insituReader = new NdbcInsituReader(STANDARD_METEOROLOGICAL);
        final String expected = "\\w{5}h\\d{4}.txt";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("41009h2016.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("46005h2018.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("41048c2016.txt");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("insitu_2_WMOID_DBBH_19780118_20151025.nc");
        assertFalse(matcher.matches());
    }
}
