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

public class DTUSIC1SectionParserTest {

    private DTUSIC1SectionParser parser;

    @Before
    public void setUp()  {
        parser = new DTUSIC1SectionParser();
    }

    @Test
    public void testParseTime() throws ParseException {
        final String lineStart = "-69.000,-051.628,2017-03-03T07:04:40Z,COMPRESSIONCELLS_DTU,1.0,0.995,-69.000, ...";

        final Date date = parser.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2017, 3, 3, 7, 4, 40, date);
    }

    @Test
    public void testParse() throws ParseException {
        final String[] tokens = new String[]{"-73.000", "-053.143", "2017-03-16T06:57:05Z", "COMPRESSIONCELLS_DTU", "1.0", "0.994"};

        final Section section = parser.parse(tokens, 0);
        assertEquals(-53.143f, section.get("longitude").getFloat(0), 1e-8);
        assertEquals(-73.f, section.get("latitude").getFloat(0), 1e-8);
        assertEquals(1489647425, section.get("time").getInt(0));

        final Array refId = section.get("reference-id");
        final char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("COMPRESSIONCELLS_DTU", new String(valueAsArray));

        assertEquals(1.f, section.get("SIC").getFloat(0), 1e-8);
        assertEquals(0.994f, section.get("areachange").getFloat(0), 1e-8);
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(6, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

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
