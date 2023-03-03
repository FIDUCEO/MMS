package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ASCATSectionParserTest {

    private ASCATSectionParser parser;

    @Before
    public void setUp() {
        parser = new ASCATSectionParser();
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(10, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(0);
        assertEquals("ASCAT_longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("ASCAT_upstreamfile", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

        variable = variables.get(5);
        assertEquals("ASCAT_sigma_40", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("ASCAT_sigma_40_mask", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("ASCAT_nb_samples", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("ASCAT_warning", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("ASCAT_std", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix() {
        assertEquals("ASCAT", parser.getNamePrefix());
    }

    @Test
    public void testParse() throws ParseException {
        // first two tokens don't belong to ASCAT - just as offset for testing tb 2022-11-11
        final String[] tokens = {"GW1AM2_201701210013_184D_L1SGRTBR_2220220.h5", "4852",
                "-70.990", "-044.510", "2017-01-20T12:00:00Z", "ASCAT_CERSAT_IFREMER", "ASCAT_A_B_20170120.nc", "-16.44920",
                "-16.44920", "21", "0", "0.10924"};

        final Section section = parser.parse(tokens, 2);

        assertEquals(-70.99f, section.get("ASCAT_latitude").getFloat(0), 1e-8);
        assertEquals(-44.51f, section.get("ASCAT_longitude").getFloat(0), 1e-8);
        assertEquals(1484913600, section.get("ASCAT_time").getInt(0));

        final Array refId = section.get("ASCAT_reference-id");
        char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("ASCAT_CERSAT_IFREMER", new String(valueAsArray));

        final Array filename = section.get("ASCAT_upstreamfile");
        valueAsArray = (char[]) filename.get1DJavaArray(DataType.CHAR);
        assertEquals("ASCAT_A_B_20170120.nc", new String(valueAsArray));

        assertEquals(-16.4492f, section.get("ASCAT_sigma_40").getFloat(0), 1e-8);
        assertEquals(-16.4492f, section.get("ASCAT_sigma_40_mask").getFloat(0), 1e-8);
        assertEquals(21, section.get("ASCAT_nb_samples").getShort(0));
        assertEquals(0, section.get("ASCAT_warning").getShort(0));
        assertEquals(0.10924f, section.get("ASCAT_std").getFloat(0), 1e-8);
    }
}
