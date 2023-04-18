package com.bc.fiduceo.reader.insitu.tao;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bc.fiduceo.util.NetCDFUtils.CF_STANDARD_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaoReaderTest {

    private TaoReader reader;

    @Before
    public void setUp()  {
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

        assertEquals(1, variables.size());

        Variable variable = variables.get(0);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        Attribute attribute = variable.findAttribute(CF_STANDARD_NAME);
        assertEquals("longitude", attribute.getStringValue());
    }
}
