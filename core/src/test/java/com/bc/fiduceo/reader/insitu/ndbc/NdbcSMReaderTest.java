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

import static com.bc.fiduceo.util.NetCDFUtils.*;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class NdbcSMReaderTest {

    private NdbcSMReader reader;

    @Before
    public void setUp() {
        reader = new NdbcSMReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "\\w{5}h\\d{4}.txt";

        assertEquals(expected, reader.getRegEx());
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

    @Test
    public void testParseLine() {
        final Calendar calendar = TimeUtils.getUTCCalendar();

        String line = "2018 05 19 01 36  80  7.3  8.6 99.00 99.00 99.00 999 1017.1  13.6 999.0  10.8 99.0 99.00\n";

        SmRecord record = reader.parseLine(line, calendar);
        assertEquals(1526693760, record.utc);
        assertEquals(80, record.windDir);
        assertEquals(7.3f, record.windSpeed, 1e-8);
        assertEquals(8.6f, record.gustSpeed, 1e-8);
        assertEquals(99.f, record.waveHeight, 1e-8);
        assertEquals(99.f, record.domWavePeriod, 1e-8);
        assertEquals(99.f, record.avgWavePeriod, 1e-8);
        assertEquals(999, record.waveDir);
        assertEquals(1017.1f, record.seaLevelPressure, 1e-8);
        assertEquals(13.6f, record.airTemp, 1e-8);
        assertEquals(999.f, record.seaSurfTemp, 1e-8);
        assertEquals(10.8f, record.dewPointTemp, 1e-8);
        assertEquals(99.f, record.visibility, 1e-8);
        assertEquals(99.f, record.tideLevel, 1e-8);


        line = "2018 09 06 12 20  23  4.8  6.0 99.00 99.00 99.00 999 9999.0 999.0 999.0 999.0 99.0 99.00\n";
        record = reader.parseLine(line, calendar);
        assertEquals(1536236400, record.utc);
        assertEquals(23, record.windDir);
        assertEquals(4.8f, record.windSpeed, 1e-8);
        assertEquals(6.f, record.gustSpeed, 1e-8);
        assertEquals(99.f, record.waveHeight, 1e-8);
        assertEquals(99.f, record.domWavePeriod, 1e-8);
        assertEquals(99.f, record.avgWavePeriod, 1e-8);
        assertEquals(999, record.waveDir);
        assertEquals(9999.f, record.seaLevelPressure, 1e-8);
        assertEquals(999.f, record.airTemp, 1e-8);
        assertEquals(999.f, record.seaSurfTemp, 1e-8);
        assertEquals(999.f, record.dewPointTemp, 1e-8);
        assertEquals(99.f, record.visibility, 1e-8);
        assertEquals(99.f, record.tideLevel, 1e-8);
    }

    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final List<Variable> variables = reader.getVariables();

        //assertEquals(0, variables.size());
        Variable variable = variables.get(0);
        assertEquals("station_id", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());
        assertEquals("Station identifier", variable.findAttribute(CF_LONG_NAME).getStringValue());

        variable = variables.get(1);
        assertEquals("station_type", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());
        assertEquals("Station type. 0: OCEAN_BUOY, 1: COAST_BUOY, 2: LAKE_BUOY, 3: OCEAN_STATION, 4: COAST_STATION, 5: LAKE_STATION", variable.findAttribute(CF_LONG_NAME).getStringValue());

        variable = variables.get(4);
        assertEquals("latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        assertEquals(9.969209968386869E36f, variable.findAttribute(CF_FILL_VALUE_NAME).getNumericValue().floatValue(), 1e-8);

        variable = variables.get(7);
        assertEquals("barometer_height", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        assertEquals("Height of instrument above above mean sea level", variable.findAttribute(CF_LONG_NAME).getStringValue());

        variable = variables.get(10);
        assertEquals("WDIR", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
        assertEquals("wind_from_direction", variable.findAttribute(CF_STANDARD_NAME).getStringValue());

        variable = variables.get(13);
        assertEquals("WVHT", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        assertEquals("Significant wave height (meters) is calculated as the average of the highest one-third of all of the wave heights during the 20-minute sampling period.", variable.findAttribute(CF_LONG_NAME).getStringValue());

        variable = variables.get(16);
        assertEquals("MWD", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
        assertEquals("degT", variable.findAttribute(CF_UNITS_NAME).getStringValue());

        variable = variables.get(19);
        assertEquals("DEWP", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        assertEquals(999.f, variable.findAttribute(CF_FILL_VALUE_NAME).getNumericValue().floatValue(), 1e-8);

        variable = variables.get(21);
        assertEquals("TIDE", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
        assertEquals("The water level in feet above or below Mean Lower Low Water (MLLW).", variable.findAttribute(CF_LONG_NAME).getStringValue());
    }
}
