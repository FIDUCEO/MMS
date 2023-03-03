package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QSCATSectionParserTest {

    private QSCATSectionParser parser;

    @Before
    public void setUp() {
        parser = new QSCATSectionParser();
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(13, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(0);
        assertEquals("QSCAT_longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("QSCAT_upstreamfile", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        variable = variables.get(5);
        assertEquals("QSCAT_sigma0_inner", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(6);
        assertEquals("QSCAT_sigma0_mask_inner", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("QSCAT_nb_inner", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("QSCAT_std_inner", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("QSCAT_sigma0_outer", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("QSCAT_sigma0_mask_outer", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(11);
        assertEquals("QSCAT_nb_outer", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(12);
        assertEquals("QSCAT_std_outer", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix() {
        assertEquals("QSCAT", parser.getNamePrefix());
    }

    @Test
    public void testParse() throws ParseException {
        final String[] tokens = new String[]{"-67.000", "+017.295", "2016-11-21T18:54:33Z", "QSCAT_CERSAT_IFREMER", "QSCATnovalfile.nc",
                "noval", "noval", "noval", "noval", "noval", "noval", "noval", "noval"};

        final Section section = parser.parse(tokens, 0);

        assertEquals(-67.f, section.get("QSCAT_latitude").getFloat(0), 1e-8);
        assertEquals(17.295f, section.get("QSCAT_longitude").getFloat(0), 1e-8);
        assertEquals(1479754473, section.get("QSCAT_time").getInt(0));

        final Array refId = section.get("QSCAT_reference-id");
        char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("QSCAT_CERSAT_IFREMER", new String(valueAsArray));

        final Array filename = section.get("QSCAT_upstreamfile");
        valueAsArray = (char[]) filename.get1DJavaArray(DataType.CHAR);
        assertEquals("QSCATnovalfile.nc", new String(valueAsArray));

        assertEquals(9.969209968386869E36f, section.get("QSCAT_sigma0_inner").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36f, section.get("QSCAT_sigma0_mask_inner").getFloat(0), 1e-8);
        assertEquals(-32767, section.get("QSCAT_nb_inner").getShort(0));
        assertEquals(9.969209968386869E36f, section.get("QSCAT_std_inner").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36f, section.get("QSCAT_sigma0_outer").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36f, section.get("QSCAT_sigma0_mask_outer").getFloat(0), 1e-8);
        assertEquals(-32767, section.get("QSCAT_nb_outer").getShort(0));
        assertEquals(9.969209968386869E36f, section.get("QSCAT_std_outer").getFloat(0), 1e-8);
    }
}
