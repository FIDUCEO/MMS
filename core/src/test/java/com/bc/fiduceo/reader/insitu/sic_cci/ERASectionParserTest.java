package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.NCTestUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ERASectionParserTest {

    @Test
    public void testGetVariables_ERA() {
        final ERASectionParser parser = new ERASectionParser("ERA");
        final List<Variable> variables = parser.getVariables();

        assertEquals(27, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(0);
        assertEquals("ERA_longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("ERA_upstreamfile", variable.getShortName());
        assertEquals(DataType.STRING, variable.getDataType());

        variable = variables.get(6);
        assertEquals("ERA_u10", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(8);
        assertEquals("ERA_ws", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(10);
        assertEquals("ERA_skt", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(12);
        assertEquals("ERA_istl2", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(14);
        assertEquals("ERA_istl4", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(16);
        assertEquals("ERA_d2m", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(18);
        assertEquals("ERA_tclw", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(20);
        assertEquals("ERA_ssrd", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(22);
        assertEquals("ERA_e", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(24);
        assertEquals("ERA_sf", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(26);
        assertEquals("ERA_siconc", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetVariables_ERA5() {
        final ERASectionParser parser = new ERASectionParser("ERA5");
        final List<Variable> variables = parser.getVariables();

        assertEquals(27, variables.size());
        assertEquals(variables.size(), parser.getNumVariables());

        Variable variable = variables.get(1);
        assertEquals("ERA5_latitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(5);
        assertEquals("ERA5_msl", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(7);
        assertEquals("ERA5_v10", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(9);
        assertEquals("ERA5_t2m", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(11);
        assertEquals("ERA5_istl1", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(13);
        assertEquals("ERA5_istl3", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(15);
        assertEquals("ERA5_sst", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(17);
        assertEquals("ERA5_tcwv", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(19);
        assertEquals("ERA5_tciw", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(21);
        assertEquals("ERA5_strd", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(23);
        assertEquals("ERA5_tp", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(25);
        assertEquals("ERA5_fal", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(26);
        assertEquals("ERA5_ci", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testParse_ERA5() throws ParseException {
        final String[] tokens = new String[]{"ICECHART_DMI", "0.0",
                "+63.500", "-035.000", "2018-01-02T16:00:00Z", "ERA5_ECMWF", "201801era5.nc", "994.4", "-7.38", "-9.52",
                "12.05", "276.12", "278.35", "271.46", "271.46", "271.46", "271.46", "278.47", "272.12", "5.9709",
                "0.00249", "0.028459", "noval", "noval", "-0.146", "0.011", "0.0000", "0.0600", "0.0000"};

        final ERASectionParser parser = new ERASectionParser("ERA5");

        // parse with offset here, the first two tokens are from a different section tb 2022-11-10
        final Section section = parser.parse(tokens, 2);

        assertEquals(-35.f, section.get("ERA5_longitude").getFloat(0), 1e-8);
        assertEquals(63.5f, section.get("ERA5_latitude").getFloat(0), 1e-8);
        assertEquals(1514908800, section.get("ERA5_time").getInt(0));

        Array refId = section.get("ERA5_reference-id");
        NCTestUtils.assertStringValue(refId, "ERA5_ECMWF");

        refId = section.get("ERA5_upstreamfile");
        NCTestUtils.assertStringValue(refId, "201801era5.nc");

        assertEquals(994.4f, section.get("ERA5_msl").getFloat(0), 1e-8);
        assertEquals(-7.38f, section.get("ERA5_u10").getFloat(0), 1e-8);
        assertEquals(-9.52f, section.get("ERA5_v10").getFloat(0), 1e-8);
        assertEquals(12.05f, section.get("ERA5_ws").getFloat(0), 1e-8);
        assertEquals(276.12f, section.get("ERA5_t2m").getFloat(0), 1e-8);
        assertEquals(278.35f, section.get("ERA5_skt").getFloat(0), 1e-8);
        assertEquals(271.46f, section.get("ERA5_istl1").getFloat(0), 1e-8);
        assertEquals(271.46f, section.get("ERA5_istl2").getFloat(0), 1e-8);
        assertEquals(271.46f, section.get("ERA5_istl3").getFloat(0), 1e-8);
        assertEquals(271.46f, section.get("ERA5_istl4").getFloat(0), 1e-8);
        assertEquals(278.47f, section.get("ERA5_sst").getFloat(0), 1e-8);
        assertEquals(272.12f, section.get("ERA5_d2m").getFloat(0), 1e-8);
        assertEquals(5.9709f, section.get("ERA5_tcwv").getFloat(0), 1e-8);
        assertEquals(0.00249f, section.get("ERA5_tclw").getFloat(0), 1e-8);
        assertEquals(0.028459f, section.get("ERA5_tciw").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36f, section.get("ERA5_ssrd").getFloat(0), 1e-8);
        assertEquals(9.969209968386869E36f, section.get("ERA5_strd").getFloat(0), 1e-8);
        assertEquals(-0.146f, section.get("ERA5_e").getFloat(0), 1e-8);
        assertEquals(0.011f, section.get("ERA5_tp").getFloat(0), 1e-8);
        assertEquals(0.f, section.get("ERA5_sf").getFloat(0), 1e-8);
        assertEquals(0.06f, section.get("ERA5_fal").getFloat(0), 1e-8);
        assertEquals(0.f, section.get("ERA5_ci").getFloat(0), 1e-8);
    }

    @Test
    public void testParse_ERA() throws ParseException {
        final String[] tokens = new String[]{"97", "8", "2",
                "-62.250", "+000.000", "2015-12-14T09:00:00Z", "NWP_ECMWF", "2015q4.nc", "984.1", "-5.67", "3.62", "6.73",
                "272.23", "273.03", "273.16", "271.74", "271.87", "271.69", "271.46", "269.45", "7.5239", "0.01543",
                "0.044711", "253.68", "413.80", "-0.146", "0.280", "0.1951", "0.4341", "0.6426",
                "-62.042,"};

        final ERASectionParser parser = new ERASectionParser("ERA");

        // parse with offset here, the first three and the last tokens are from a different section tb 2022-11-10
        final Section section = parser.parse(tokens, 3);

        assertEquals(0.f, section.get("ERA_longitude").getFloat(0), 1e-8);
        assertEquals(-62.25f, section.get("ERA_latitude").getFloat(0), 1e-8);
        assertEquals(1450083600, section.get("ERA_time").getInt(0));

        Array refId = section.get("ERA_reference-id");
        NCTestUtils.assertStringValue(refId, "NWP_ECMWF");

        refId = section.get("ERA_upstreamfile");
        NCTestUtils.assertStringValue(refId, "2015q4.nc");

        assertEquals(984.1f, section.get("ERA_msl").getFloat(0), 1e-8);
        assertEquals(-5.67f, section.get("ERA_u10").getFloat(0), 1e-8);
        assertEquals(3.62f, section.get("ERA_v10").getFloat(0), 1e-8);
        assertEquals(6.73f, section.get("ERA_ws").getFloat(0), 1e-8);
        assertEquals(272.23f, section.get("ERA_t2m").getFloat(0), 1e-8);
        assertEquals(273.03f, section.get("ERA_skt").getFloat(0), 1e-8);
        assertEquals(273.16f, section.get("ERA_istl1").getFloat(0), 1e-8);
        assertEquals(271.74f, section.get("ERA_istl2").getFloat(0), 1e-8);
        assertEquals(271.87f, section.get("ERA_istl3").getFloat(0), 1e-8);
        assertEquals(271.69f, section.get("ERA_istl4").getFloat(0), 1e-8);
        assertEquals(271.46f, section.get("ERA_sst").getFloat(0), 1e-8);
        assertEquals(269.45f, section.get("ERA_d2m").getFloat(0), 1e-8);
        assertEquals(7.5239f, section.get("ERA_tcwv").getFloat(0), 1e-8);
        assertEquals(0.01543f, section.get("ERA_tclw").getFloat(0), 1e-8);
        assertEquals(0.044711f, section.get("ERA_tciw").getFloat(0), 1e-8);
        assertEquals(253.68f, section.get("ERA_ssrd").getFloat(0), 1e-8);
        assertEquals(413.80f, section.get("ERA_strd").getFloat(0), 1e-8);
        assertEquals(-0.146f, section.get("ERA_e").getFloat(0), 1e-8);
        assertEquals(0.28f, section.get("ERA_tp").getFloat(0), 1e-8);
        assertEquals(0.1951f, section.get("ERA_sf").getFloat(0), 1e-8);
        assertEquals(0.4341f, section.get("ERA_fal").getFloat(0), 1e-8);
        assertEquals(0.6426f, section.get("ERA_siconc").getFloat(0), 1e-8);
    }

    @Test
    public void testGetNamePrefix()  {
        final ERASectionParser eraParser = new ERASectionParser("ERA");
        assertEquals("ERA", eraParser.getNamePrefix());

        final ERASectionParser era5Parser = new ERASectionParser("ERA5");
        assertEquals("ERA5", era5Parser.getNamePrefix());
    }
}
