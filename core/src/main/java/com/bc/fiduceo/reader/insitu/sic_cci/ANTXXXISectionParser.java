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

class ANTXXXISectionParser extends ReferenceSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createReferenceCommonVariables(variables);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-primary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-primary", DataType.SHORT, attributes));

        return variables;
    }

    @Override
    Section parse(String[] tokens) throws ParseException {
        throw new RuntimeException("not implemented");
    }
}
