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

class AMSR2SectionParser extends AbstractSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables, "AMSR2_");

        variables.add(createBTVariableProxy("AMSR2_6.9GHzH"));
        variables.add(createBTVariableProxy("AMSR2_6.9GHzV"));
        variables.add(createBTVariableProxy("AMSR2_7.3GHzH"));
        variables.add(createBTVariableProxy("AMSR2_7.3GHzV"));
        variables.add(createBTVariableProxy("AMSR2_10.7GHzH"));
        variables.add(createBTVariableProxy("AMSR2_10.7GHzV"));
        variables.add(createBTVariableProxy("AMSR2_18.7GHzH"));
        variables.add(createBTVariableProxy("AMSR2_18.7GHzV"));
        variables.add(createBTVariableProxy("AMSR2_23.8GHzH"));
        variables.add(createBTVariableProxy("AMSR2_23.8GHzV"));
        variables.add(createBTVariableProxy("AMSR2_36.5GHzH"));
        variables.add(createBTVariableProxy("AMSR2_36.5GHzV"));
        variables.add(createBTVariableProxy("AMSR2_89.0GHzH"));
        variables.add(createBTVariableProxy("AMSR2_89.0GHzV"));

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "angle_of_incidence"));
        attributes.add(new Attribute(CF_UNITS_NAME, "deg"));
        variables.add(new VariableProxy("AMSR2_Earth-Incidence", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "deg"));
        variables.add(new VariableProxy("AMSR2_Earth-Azimuth", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Scanpos (0-243)"));
        variables.add(new VariableProxy("AMSR2_scanpos", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        variables.add(new VariableProxy("AMSR2_upstreamfile", DataType.CHAR, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Time difference between reference time and AMSR time (seconds)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "s"));
        variables.add(new VariableProxy("AMSR2_timediff", DataType.SHORT, attributes));

        return variables;
    }

    private static VariableProxy createBTVariableProxy(String name) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "toa_brightness_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        return new VariableProxy(name, DataType.FLOAT, attributes);
    }

    @Override
    int getNumVariables() {
        return 23;
    }

    @Override
    String getNamePrefix() {
        return "AMSR2";
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("AMSR2_latitude", parseFloat(tokens[offset]));
        section.add("AMSR2_longitude", parseFloat(tokens[offset + 1]));
        section.add("AMSR2_time", parseUtcTime(tokens[offset + 2]));
        section.add("AMSR2_reference-id", parseString(tokens[offset + 3]));
        section.add("AMSR2_6.9GHzH", parseFloat(tokens[offset + 4]));
        section.add("AMSR2_6.9GHzV", parseFloat(tokens[offset + 5]));
        section.add("AMSR2_7.3GHzH", parseFloat(tokens[offset + 6]));
        section.add("AMSR2_7.3GHzV", parseFloat(tokens[offset + 7]));
        section.add("AMSR2_10.7GHzH", parseFloat(tokens[offset + 8]));
        section.add("AMSR2_10.7GHzV", parseFloat(tokens[offset + 9]));
        section.add("AMSR2_18.7GHzH", parseFloat(tokens[offset + 10]));
        section.add("AMSR2_18.7GHzV", parseFloat(tokens[offset + 11]));
        section.add("AMSR2_23.8GHzH", parseFloat(tokens[offset + 12]));
        section.add("AMSR2_23.8GHzV", parseFloat(tokens[offset + 13]));
        section.add("AMSR2_36.5GHzH", parseFloat(tokens[offset + 14]));
        section.add("AMSR2_36.5GHzV", parseFloat(tokens[offset + 15]));
        section.add("AMSR2_89.0GHzH", parseFloat(tokens[offset + 16]));
        section.add("AMSR2_89.0GHzV", parseFloat(tokens[offset + 17]));
        section.add("AMSR2_Earth-Incidence", parseFloat(tokens[offset + 18]));
        section.add("AMSR2_Earth-Azimuth", parseFloat(tokens[offset + 19]));
        section.add("AMSR2_scanpos", parseShort(tokens[offset + 20]));
        section.add("AMSR2_upstreamfile", parseString(tokens[offset + 21]));
        section.add("AMSR2_timediff", parseShort(tokens[offset + 22]));

        return section;
    }
}
