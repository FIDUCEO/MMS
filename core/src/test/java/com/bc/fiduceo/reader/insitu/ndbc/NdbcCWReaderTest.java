package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class NdbcCWReaderTest {

    private NdbcCWReader reader;

    @Before
    public void setUp() {
        reader = new NdbcCWReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "\\w{5}c\\d{4}.txt";

        assertEquals(expected, reader.getRegEx());
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
    public void testParseLine()  {
        final Calendar calendar = TimeUtils.getUTCCalendar();

        String line = "2016 06 24 04 50 144  6.3 135  7.8 0431\n";

        CwRecord record = reader.parseLine(line, calendar);
        assertEquals(1466743800, record.utc);
        assertEquals(144, record.windDir);
        assertEquals(6.3f, record.windSpeed, 1e-8);
        assertEquals(135, record.gustDir);
        assertEquals(7.8f, record.gustSpeed, 1e-8);
        assertEquals(431, record.gustTime);

        line = "2016 07 26 14 40 178  6.0 999 99.0 9999\n";
        record = reader.parseLine(line, calendar);
        assertEquals(1469544000, record.utc);
        assertEquals(178, record.windDir);
        assertEquals(6.0f, record.windSpeed, 1e-8);
        assertEquals(999, record.gustDir);
        assertEquals(99.f, record.gustSpeed, 1e-8);
        assertEquals(9999, record.gustTime);
    }

    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final List<Variable> variables = reader.getVariables();

        assertEquals(15, variables.size());

        Variable variable = variables.get(0);
        assertEquals("station_id", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        variable = variables.get(3);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("air_temp_height", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("time", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        variable = variables.get(12);
        assertEquals("GDR", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("45008c2018.txt");
        assertEquals(3, ymd.length);
        assertEquals(2018, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("latitude", reader.getLatitudeVariableName());
    }
}
