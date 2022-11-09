package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class ERASectionParser extends AbstractSectionParser {

    final private String type;

    public ERASectionParser(String type) {
        this.type = type;
    }

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();
        final String prefix = type + "_";

        createCommonVariables(variables, prefix);

        ArrayList<Attribute> attributes = new ArrayList<>();
        variables.add(new VariableProxy(prefix + "upstreamfile", DataType.CHAR, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_pressure_at_mean_sea_level"));
        attributes.add(new Attribute(CF_UNITS_NAME, "Pa"));
        variables.add(new VariableProxy(prefix + "msl", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "10 metre u-wind component"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m s-1"));
        variables.add(new VariableProxy(prefix + "u10", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "10 metre v-wind component"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m s-1"));
        variables.add(new VariableProxy(prefix + "v10", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m s-1"));
        variables.add(new VariableProxy(prefix + "ws", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "2 m air temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "t2m", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Skin temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "skt", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice temperature layer 1"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "istl1", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice temperature layer 2"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "istl2", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice temperature layer 3"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "istl3", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice temperature layer 4"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "istl4", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_surface_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "sst", DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    Section parse(String[] tokens) throws ParseException {
        throw new RuntimeException("not implemented");
    }
}
