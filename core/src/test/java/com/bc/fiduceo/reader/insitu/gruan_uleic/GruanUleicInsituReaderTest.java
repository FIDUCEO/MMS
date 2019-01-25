package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GruanUleicInsituReaderTest {

    private GruanUleicInsituReader reader;

    @Before
    public void setUp() {
        reader = new GruanUleicInsituReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "[a-z]{3}_matchup_points.txt";

        assertEquals(expected, reader.getRegEx());

        final Pattern pattern = Pattern.compile(expected);

        Matcher matcher = pattern.matcher("nya_matchup_points.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("sod_matchup_points.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("AT2_TOA_1PURAL19980424_055754_000000001031_00348_15733_0000.OT");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("lon", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("lat", reader.getLatitudeVariableName());
    }

    @Test
    public void testDecodeLine() {
        GruanUleicInsituReader.Line line = GruanUleicInsituReader.decodeLine("1232516371.0, -156.61623, 71.323074, BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090121T060000_1-000-001.nc");
        assertNotNull(line);

        TestUtil.assertCorrectUTCDate(2009, 1, 21, 5, 40, 15, 488, line.date);
        assertEquals(-156.61623, line.lon, 1e-5);
        assertEquals(71.323074, line.lat, 1e-5);
        assertEquals("BAR/2009/BAR-RS-01_2_RS92-GDP_002_20090121T060000_1-000-001.nc", line.path);
    }
}
