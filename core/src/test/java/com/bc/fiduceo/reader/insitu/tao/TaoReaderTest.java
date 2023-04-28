package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bc.fiduceo.util.NetCDFUtils.*;
import static org.junit.Assert.*;

public class TaoReaderTest {

    private TaoReader reader;

    @Before
    public void setUp() {
        reader = new TaoReader();
    }

    @Test
    public void testGetRegEx() {
        final String expected = "(?:TAO|TRITON)_\\w+_\\w+(-\\w+)??\\d{4}-\\d{2}.txt";

        assertEquals(expected, reader.getRegEx());
        final Pattern pattern = java.util.regex.Pattern.compile(expected);

        Matcher matcher = pattern.matcher("TRITON_TR0N156E_1998_2016-07.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("TAO_T0N170W_DM207A-20160829_2017-04.txt");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("TAO_T2S110W_DM233A-20170608_2018-02.txt");
        assertTrue(matcher.matches());
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
    public void testGetVariables() throws InvalidRangeException, IOException {
        final List<Variable> variables = reader.getVariables();

        assertEquals(13, variables.size());

        // --- longitude ---
        Variable variable = variables.get(0);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        Attribute attribute = variable.findAttribute(CF_STANDARD_NAME);
        assertNotNull(attribute);
        assertEquals("longitude", attribute.getStringValue());

        // --- latitude ---
        variable = variables.get(1);
        assertEquals("latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_UNITS_NAME);
        assertNotNull(attribute);
        assertEquals("degree_north", attribute.getStringValue());

        // --- time ---
        variable = variables.get(2);
        assertEquals("time", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        attribute = variable.findAttribute(CF_STANDARD_NAME);
        assertNotNull(attribute);
        assertEquals("time", attribute.getStringValue());

        // --- SSS ---
        variable = variables.get(3);
        assertEquals("SSS", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        assertNotNull(attribute);
        assertEquals(-9.999f, attribute.getNumericValue().floatValue(), 1e-8);

        // --- SST ---
        variable = variables.get(4);
        assertEquals("SST", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_UNITS_NAME);
        assertNotNull(attribute);
        assertEquals("degree_Celsius", attribute.getStringValue());

        // --- AIRT ---
        variable = variables.get(5);
        assertEquals("AIRT", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_STANDARD_NAME);
        assertNotNull(attribute);
        assertEquals("air_temperature", attribute.getStringValue());

        // --- RH ---
        variable = variables.get(6);
        assertEquals("RH", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        assertNotNull(attribute);
        assertEquals(-9.99f, attribute.getNumericValue().floatValue(), 1e-8);

        // --- WSPD ---
        variable = variables.get(7);
        assertEquals("WSPD", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_UNITS_NAME);
        assertNotNull(attribute);
        assertEquals("m/s", attribute.getStringValue());

        // --- WDIR ---
        variable = variables.get(8);
        assertEquals("WDIR", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_STANDARD_NAME);
        assertNotNull(attribute);
        assertEquals("wind_to_direction", attribute.getStringValue());

        // --- BARO ---
        variable = variables.get(9);
        assertEquals("BARO", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_FILL_VALUE_NAME);
        assertNotNull(attribute);
        assertEquals(-9.9f, attribute.getNumericValue().floatValue(), 1e-8);

        // --- RAIN ---
        variable = variables.get(10);
        assertEquals("RAIN", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attribute = variable.findAttribute(CF_UNITS_NAME);
        assertNotNull(attribute);
        assertEquals("mm/hour", attribute.getStringValue());

        // --- Q ---
        variable = variables.get(11);
        assertEquals("Q", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        attribute = variable.findAttribute(CF_LONG_NAME);
        assertNotNull(attribute);
        assertEquals("Data Quality Codes", attribute.getStringValue());

        // --- M ---
        variable = variables.get(12);
        assertEquals("M", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        attribute = variable.findAttribute(CF_LONG_NAME);
        assertNotNull(attribute);
        assertEquals("Data Mode Codes", attribute.getStringValue());
    }

    @Test
    public void testParseLine() {
        final TaoRecord record = TaoReader.parseLine("1464854400 -139.99 -2.0402594 35.461 -9.999 25.63 79.46 5.4 273.0 -9.9 -9.99 19111199 DDDDDDDD");
        assertNotNull(record);

        assertEquals(1464854400, record.time);
        assertEquals(-139.99f, record.longitude, 1e-8);
        assertEquals(-2.0402594f, record.latitude, 1e-8);
        assertEquals(35.461f, record.SSS, 1e-8);
        assertEquals(-9.999f, record.SST, 1e-8);
        assertEquals(25.63f, record.AIRT, 1e-8);
        assertEquals(79.46f, record.RH, 1e-8);
        assertEquals(5.4f, record.WSPD, 1e-8);
        assertEquals(273.f, record.WDIR, 1e-8);
        assertEquals(-9.9f, record.BARO, 1e-8);
        assertEquals(-9.99f, record.RAIN, 1e-8);
        assertEquals(19111199, record.Q);
        assertEquals("DDDDDDDD", record.M);
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        try {
            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            // expected
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(6, 9),
                geometryFactory.createPoint(7, 0),
                geometryFactory.createPoint(7, 10)
        ));

        try {
            reader.getSubScenePixelLocator(polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            // expected
        }
    }
}
