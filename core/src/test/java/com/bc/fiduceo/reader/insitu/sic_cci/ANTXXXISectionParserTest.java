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

public class ANTXXXISectionParserTest {

    private ANTXXXISectionParser parser;

    @Before
    public void setUp() {
        parser = new ANTXXXISectionParser();
    }

    @Test
    public void testParseTime() throws ParseException {
        final String lineStart = "-68.72273,-7.59800,2015-12-16T17:00:00Z,ASPeCt_ANT-XXXI_2_Polarstern,10,10,60,0.6,0.45,1.0,4,0.10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1.66,-4.7,2.8,157,97,8,2,-69.000,, ...";

        final Date date = parser.parseTime(lineStart);

        TestUtil.assertCorrectUTCDate(2015, 12, 16, 17, 0, 0, date);
    }

    @Test
    public void testParse() throws ParseException {
        final String[] tokens = {"-66.33530", "-4.32590", "2015-12-15T20:00:00Z", "ASPeCt_ANT-XXXI_2_Polarstern",
                "30", "10", "70", "0.9", "0.55", "1.0", "6", "0.20", "10", "70", "0.8", "0.25", "1.0", "6", "0.20", "10", "70",
                "0.70", "0.15", "1.0", "6", "0.1", "-1.49", "-2.4", "2.7", "267", "97", "8", "2"};

        final Section section = parser.parse(tokens, 0);
        assertEquals(-66.33530f, section.get("latitude").getFloat(0), 1e-8);
        assertEquals(-4.32590f, section.get("longitude").getFloat(0), 1e-8);
        assertEquals(1450209600, section.get("time").getInt(0));

        final Array refId = section.get("reference-id");
        final char[] valueAsArray = (char[]) refId.get1DJavaArray(DataType.CHAR);
        assertEquals("ASPeCt_ANT-XXXI_2_Polarstern", new String(valueAsArray));

        assertEquals(30, section.get("SIC-total").getByte(0));
        assertEquals(10, section.get("SIC-primary").getByte(0));
        assertEquals(70, section.get("Ice-type-primary").getByte(0));
        assertEquals(0.9f, section.get("SIT-primary").getFloat(0), 1e-8);
        assertEquals(0.55f, section.get("Ridged-ice-fraction-primary").getFloat(0), 1e-8);
        assertEquals(1.f, section.get("Ridged-height-primary").getFloat(0), 1e-8);
        assertEquals(6, section.get("Snow-cover-type-primary").getByte(0));
        assertEquals(0.2f, section.get("Snow-depth-primary").getFloat(0), 1e-8);
        assertEquals(10, section.get("SIC-secondary").getByte(0));
        assertEquals(70, section.get("Ice-type-secondary").getByte(0));
        assertEquals(0.8f, section.get("SIT-secondary").getFloat(0), 1e-8);
        assertEquals(0.25f, section.get("Ridged-ice-fraction-secondary").getFloat(0), 1e-8);
        assertEquals(1.f, section.get("Ridged-height-secondary").getFloat(0), 1e-8);
        assertEquals(6, section.get("Snow-cover-type-secondary").getByte(0));
        assertEquals(0.2f, section.get("Snow-depth-secondary").getFloat(0), 1e-8);
        assertEquals(10, section.get("SIC-tertiary").getByte(0));
        assertEquals(70, section.get("Ice-type-tertiary").getByte(0));
        assertEquals(0.7f, section.get("SIT-tertiary").getFloat(0), 1e-8);
        assertEquals(0.15f, section.get("Ridged-ice-fraction-tertiary").getFloat(0), 1e-8);
        assertEquals(1.f, section.get("Ridged-height-tertiary").getFloat(0), 1e-8);
        assertEquals(6, section.get("Snow-cover-type-tertiary").getByte(0));
        assertEquals(0.1f, section.get("Snow-depth-tertiary").getFloat(0), 1e-8);
        assertEquals(-1.49f, section.get("Sea-water-temperature").getFloat(0), 1e-8);
        assertEquals(-2.4f, section.get("Air-temperature").getFloat(0), 1e-8);
        assertEquals(2.7f, section.get("Wind-speed").getFloat(0), 1e-8);
        assertEquals(267, section.get("Wind-direction").getShort(0));
        assertEquals(97, section.get("Visibility").getByte(0));
        assertEquals(8, section.get("Cloud-cover").getByte(0));
        assertEquals(2, section.get("Weather").getByte(0));
    }

    @Test
    public void testGetVariables() {
        final List<Variable> variables = parser.getVariables();

        assertEquals(33, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(0);
        assertEquals("longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(3);
        assertEquals("reference-id", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

        variable = variables.get(4);
        assertEquals("SIC-total", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(5);
        assertEquals("SIC-primary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(6);
        assertEquals("Ice-type-primary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(7);
        assertEquals("SIT-primary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("Ridged-ice-fraction-primary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("Ridge-height-primary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("Snow-cover-type-primary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(11);
        assertEquals("Snow-depth-primary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(12);
        assertEquals("SIC-secondary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(13);
        assertEquals("Ice-type-secondary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(14);
        assertEquals("SIT-secondary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(15);
        assertEquals("Ridged-ice-fraction-secondary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(16);
        assertEquals("Ridge-height-secondary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(17);
        assertEquals("Snow-cover-type-secondary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(18);
        assertEquals("Snow-depth-secondary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(19);
        assertEquals("SIC-tertiary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(20);
        assertEquals("Ice-type-tertiary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(21);
        assertEquals("SIT-tertiary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(22);
        assertEquals("Ridged-ice-fraction-tertiary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(23);
        assertEquals("Ridge-height-tertiary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(24);
        assertEquals("Snow-cover-type-tertiary", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(25);
        assertEquals("Snow-depth-tertiary", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(26);
        assertEquals("Sea-water-temperature", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(27);
        assertEquals("Air-temperature", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(28);
        assertEquals("Wind-speed", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(29);
        assertEquals("Wind-direction", variable.getShortName());
        assertEquals(DataType.SHORT, variable.getDataType());

        variable = variables.get(30);
        assertEquals("Visibility", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(31);
        assertEquals("Cloud-cover", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());

        variable = variables.get(32);
        assertEquals("Weather", variable.getShortName());
        assertEquals(DataType.BYTE, variable.getDataType());
    }

    @Test
    public void testGetNamePrefix()  {
        assertEquals("REF", parser.getNamePrefix());
    }
}
