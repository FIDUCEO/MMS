package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReferenceDataSectionTest {

    @Test
    public void testParse() throws ParseException {
        final String lineStart = "-59.000,+090.000,2016-01-01T08:00:00Z,ICECHART_DMI,0.0,-59.000,+090.000, ...";

        final Date date = ReferenceDataSection.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2016, 1, 1, 8, 0, 0, date);
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = ReferenceDataSection.getVariables();

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
