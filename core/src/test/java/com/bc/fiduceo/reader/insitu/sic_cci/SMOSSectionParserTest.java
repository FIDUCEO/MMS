package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SMOSSectionParserTest {

    private SMOSSectionParser parser;

    @Before
    public void setUp() {
        parser = new SMOSSectionParser();
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(11, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(1);
        assertEquals("SMOS_latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SMOS_upstreamfile", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        variable = variables.get(5);
        assertEquals("SMOS_Tbv", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("SMOS_Tbh", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("SMOS_RMSE_v", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("SMOS_RMSE_h", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("SMOS_nmp", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("SMOS_dataloss", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix() {
        assertEquals("SMOS", parser.getNamePrefix());
    }

    @Test
    public void testParse() throws ParseException {
        // the first and last toke do not belong to the section, just for offset testing tb 2022-11-14
        final String[] tokens = new String[]{"0.24418",
                "+83.026", "-122.542", "2016-06-28T12:00:00Z", "SMOS_UHAM", "SMOS_40deg_e12.5_20160628.nc", "237.47273",
                "223.45746", "5.61943", "5.73495", "212", "20",
                "+83.026"};

        final Section section = parser.parse(tokens, 1);

        assertEquals(83.026f, section.get("SMOS_latitude").getFloat(0), 1e-8);
        assertEquals(-122.542f, section.get("SMOS_longitude").getFloat(0), 1e-8);
        assertEquals(1467115200, section.get("SMOS_time").getInt(0));

        final Array refId = section.get("SMOS_reference-id");
        char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("SMOS_UHAM", new String(valueAsArray));

        final Array filename = section.get("SMOS_upstreamfile");
        valueAsArray = (char[]) filename.get1DJavaArray(DataType.CHAR);
        assertEquals("SMOS_40deg_e12.5_20160628.nc", new String(valueAsArray));

        assertEquals(237.47273f, section.get("SMOS_Tbv").getFloat(0), 1e-8);
        assertEquals(223.45746f, section.get("SMOS_Tbh").getFloat(0), 1e-8);
        assertEquals(5.61943f, section.get("SMOS_RMSE_v").getFloat(0), 1e-8);
        assertEquals(5.73495f, section.get("SMOS_RMSE_h").getFloat(0), 1e-8);
        assertEquals(212, section.get("SMOS_nmp").getShort(0));
        assertEquals(20, section.get("SMOS_dataloss").getShort(0));
    }
}
