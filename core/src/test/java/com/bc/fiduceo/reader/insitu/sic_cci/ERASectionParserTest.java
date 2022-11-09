package com.bc.fiduceo.reader.insitu.sic_cci;

import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ERASectionParserTest {

    @Test
    public void testGetVariables_ERA() {
        final ERASectionParser parser = new ERASectionParser("ERA");
        final List<Variable> variables = parser.getVariables();

        // @todo 1 tb/tb reactivate 2022-11-09
        //assertEquals(4, variables.size());

        Variable variable = variables.get(0);
        assertEquals("ERA_longitude", variable.getShortName());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = variables.get(4);
        assertEquals("ERA_upstreamfile", variable.getShortName());
        assertEquals(DataType.CHAR, variable.getDataType());

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
    }

    @Test
    public void testGetVariables_ERA5() {
        final ERASectionParser parser = new ERASectionParser("ERA5");
        final List<Variable> variables = parser.getVariables();

        // @todo 1 tb/tb reactivate 2022-11-09
        //assertEquals(4, variables.size());

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
    }
}
