package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@SuppressWarnings("resource")
public class NdbcSMReaderTest {

    @Test
    public void testGetRegEx() {
        final NdbcSMReader insituReader = new NdbcSMReader();
        final String expected = "\\w{5}h\\d{4}.txt";

        assertEquals(expected, insituReader.getRegEx());
        final Pattern pattern = Pattern.compile(expected);

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
