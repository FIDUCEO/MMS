package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DMISIC0SectionParserTest {

    private DMISIC0SectionParser parser;

    @Before
    public void setUp()  {
        parser = new DMISIC0SectionParser();
    }

    @Test
    public void testParseTime() throws ParseException {
        final String lineStart = "-59.000,+090.000,2016-01-01T08:00:00Z,ICECHART_DMI,0.0,-59.000,+090.000, ...";

        final Date date = parser.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2016, 1, 1, 8, 0, 0, date);
    }

    @Test
    public void testParse() throws ParseException {
        final String[] tokens = new String[]{"+55.000", "+180.000", "2016-01-02T01:00:00Z", "ICECHART_DMI", "0.0"};

        final Section section = parser.parse(tokens);
        assertEquals(180.f, section.get("longitude").getFloat(0), 1e-8);
        assertEquals(55.f, section.get("latitude").getFloat(0), 1e-8);
        assertEquals(1451696400, section.get("time").getInt(0));

        final Array refId = section.get("reference-id");
        final char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("ICECHART_DMI", new String(valueAsArray));

        assertEquals(0.f, section.get("SIC").getFloat(0), 1e-8);
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(5, variables.size());

        Variable variable = variables.get(0);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(1);
        assertEquals("latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(2);
        assertEquals("time", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        variable = variables.get(3);
        assertEquals("reference-id", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SIC", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }
}
