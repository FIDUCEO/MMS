package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.TestUtil;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ANTXXXISectionParserTest {

    private ANTXXXISectionParser parser;

    @Before
    public void setUp()  {
        parser = new ANTXXXISectionParser();
    }

    @Test
    public void testParseTime() throws ParseException {
        final String lineStart = "-68.72273,-7.59800,2015-12-16T17:00:00Z,ASPeCt_ANT-XXXI_2_Polarstern,10,10,60,0.6,0.45,1.0,4,0.10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1.66,-4.7,2.8,157,97,8,2,-69.000,, ...";

        final Date date = parser.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2015, 12, 16, 17, 0, 0, date);
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(7, variables.size());

        Variable variable = variables.get(0);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(3);
        assertEquals("reference-id", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SIC-primary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(5);
        assertEquals("Ice-type-primary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(6);
        assertEquals("SIT-primary", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
    }
}
