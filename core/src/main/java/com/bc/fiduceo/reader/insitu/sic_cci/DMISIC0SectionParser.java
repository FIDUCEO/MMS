package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_STANDARD_NAME;

class DMISIC0SectionParser extends ReferenceSectionParser {

    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables);

        final List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        variables.add(new VariableProxy("SIC", DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 5;
    }

    @Override
    String getNamePrefix() {
        return "REF";
    }

    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("latitude", parseFloat(tokens[offset]));
        section.add("longitude", parseFloat(tokens[offset + 1]));
        section.add("time", parseUtcTime(tokens[offset + 2]));
        section.add("reference-id", parseString(tokens[offset + 3]));
        section.add("SIC", parseFloat(tokens[offset + 4]));

        return section;
    }
}
