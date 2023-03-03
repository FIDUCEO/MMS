package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SMAPSectionParserTest {

    private SMAPSectionParser parser;

    @Before
    public void setUp() {
        parser = new SMAPSectionParser();
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(10, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());


        Variable variable = variables.get(2);
        assertEquals("SMAP_time", variable.getShortName());
        assertEquals(DataType.INT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SMAP_upstreamfile", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        variable = variables.get(5);
        assertEquals("SMAP_Tbv", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("SMAP_Tbh", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("SMAP_RMSE_v", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("SMAP_RMSE_h", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("SMAP_nmp", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix() {
        assertEquals("SMAP", parser.getNamePrefix());
    }

    @Test
    public void testParse() throws ParseException {
        // the first two tokens do not belong to SMAP, just for offset testing tb 2022-11-14
        final String[] tokens = new String[]{"212", "20",
                "+83.026", "-122.542", "2016-06-28T12:00:00Z", "SMAP_UHAM", "SMAP_40deg_e12.5_20160628.nc", "235.42841",
                "212.40732", "1.55241", "1.53897", "10"};

        final Section section = parser.parse(tokens, 2);

        assertEquals(83.026f, section.get("SMAP_latitude").getFloat(0), 1e-8);
        assertEquals(-122.542f, section.get("SMAP_longitude").getFloat(0), 1e-8);
        assertEquals(1467115200, section.get("SMAP_time").getInt(0));

        final Array refId = section.get("SMAP_reference-id");
        char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("SMAP_UHAM", new String(valueAsArray));

        final Array filename = section.get("SMAP_upstreamfile");
        valueAsArray = (char[]) filename.get1DJavaArray(DataType.CHAR);
        assertEquals("SMAP_40deg_e12.5_20160628.nc", new String(valueAsArray));

        assertEquals(235.42841f, section.get("SMAP_Tbv").getFloat(0), 1e-8);
        assertEquals(212.40732f, section.get("SMAP_Tbh").getFloat(0), 1e-8);
        assertEquals(1.55241f, section.get("SMAP_RMSE_v").getFloat(0), 1e-8);
        assertEquals(1.53897f, section.get("SMAP_RMSE_h").getFloat(0), 1e-8);
        assertEquals(10, section.get("SMAP_nmp").getShort(0));
    }
}
