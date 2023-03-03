package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AMSR2SectionParserTest {

    private AMSR2SectionParser parser;

    @Before
    public void setUp() {
        parser = new AMSR2SectionParser();
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();
        assertEquals(23, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(0);
        assertEquals("AMSR2_longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(2);
        assertEquals("AMSR2_time", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("AMSR2_6.9GHzH", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("AMSR2_7.3GHzV", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("AMSR2_10.7GHzH", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(11);
        assertEquals("AMSR2_18.7GHzV", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(12);
        assertEquals("AMSR2_23.8GHzH", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(15);
        assertEquals("AMSR2_36.5GHzV", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(17);
        assertEquals("AMSR2_89.0GHzV", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(18);
        assertEquals("AMSR2_Earth-Incidence", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(19);
        assertEquals("AMSR2_Earth-Azimuth", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(20);
        assertEquals("AMSR2_scanpos", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(21);
        assertEquals("AMSR2_upstreamfile", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

        variable = variables.get(22);
        assertEquals("AMSR2_timediff", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix() {
        assertEquals("AMSR2", parser.getNamePrefix());
    }

    @Test
    public void testParse() throws ParseException {
        // the first and the last token do not belong to the AMSR data - just for offset testing here tb 2022-11-11
        final String[] tokens = {"0.0000",
                "+54.974", "+179.959", "2016-01-02T01:00:00Z", "AMSR2_L1R_JAXA", "79.79", "160.12", "80.68", "160.68", "86.50",
                "168.86", "106.16", "186.98", "130.80", "200.80", "144.96", "212.90", "197.88", "244.46", "55.26", "-173.87",
                "093", "GW1AM2_201601020019_177A_L1SGRTBR_2210210.h5", "0",
                "+55.000,"};

        final Section section = parser.parse(tokens, 1);

        assertEquals(54.974f, section.get("AMSR2_latitude").getFloat(0), 1e-8);
        assertEquals(179.959f, section.get("AMSR2_longitude").getFloat(0), 1e-8);
        assertEquals(1451696400, section.get("AMSR2_time").getInt(0));

        final Array refId = section.get("AMSR2_reference-id");
        char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("AMSR2_L1R_JAXA", new String(valueAsArray));

        assertEquals(79.79f, section.get("AMSR2_6.9GHzH").getFloat(0), 1e-8);
        assertEquals(160.12f, section.get("AMSR2_6.9GHzV").getFloat(0), 1e-8);
        assertEquals(80.68f, section.get("AMSR2_7.3GHzH").getFloat(0), 1e-8);
        assertEquals(160.68f, section.get("AMSR2_7.3GHzV").getFloat(0), 1e-8);
        assertEquals(86.50f, section.get("AMSR2_10.7GHzH").getFloat(0), 1e-8);
        assertEquals(168.86f, section.get("AMSR2_10.7GHzV").getFloat(0), 1e-8);
        assertEquals(106.16f, section.get("AMSR2_18.7GHzH").getFloat(0), 1e-8);
        assertEquals(186.98f, section.get("AMSR2_18.7GHzV").getFloat(0), 1e-8);
        assertEquals(130.8f, section.get("AMSR2_23.8GHzH").getFloat(0), 1e-8);
        assertEquals(200.8f, section.get("AMSR2_23.8GHzV").getFloat(0), 1e-8);
        assertEquals(144.96f, section.get("AMSR2_36.5GHzH").getFloat(0), 1e-8);
        assertEquals(212.9f, section.get("AMSR2_36.5GHzV").getFloat(0), 1e-8);
        assertEquals(197.88f, section.get("AMSR2_89.0GHzH").getFloat(0), 1e-8);
        assertEquals(244.46f, section.get("AMSR2_89.0GHzV").getFloat(0), 1e-8);
        assertEquals(55.26f, section.get("AMSR2_Earth-Incidence").getFloat(0), 1e-8);
        assertEquals(93, section.get("AMSR2_scanpos").getInt(0));

        final Array fileName = section.get("AMSR2_upstreamfile");
        valueAsArray = (char[]) fileName.get1DJavaArray(DataType.CHAR);
        assertEquals("GW1AM2_201601020019_177A_L1SGRTBR_2210210.h5", new String(valueAsArray));

        assertEquals(0, section.get("AMSR2_timediff").getShort(0));
    }
}
