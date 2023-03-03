package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.netcdf.StringVariable;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

public class ASCATSectionParser extends AbstractSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables, "ASCAT_");

        ArrayList<Attribute> attributes = new ArrayList<>();
        variables.add(new StringVariable(new VariableProxy("ASCAT_upstreamfile", DataType.STRING, attributes), 32));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "ASCAT backscatter (dB)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("ASCAT_sigma_40", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "ASCAT backscatter (dB) - masked"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("ASCAT_sigma_40_mask", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of ASCAT samples"));
        variables.add(new VariableProxy("ASCAT_nb_samples", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "ASCAT flag"));
        variables.add(new VariableProxy("ASCAT_warning", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Standard deviation of ASCAT data"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("ASCAT_std", DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 10;
    }

    @Override
    String getNamePrefix() {
        return "ASCAT";
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("ASCAT_latitude", parseFloat(tokens[offset]));
        section.add("ASCAT_longitude", parseFloat(tokens[offset + 1]));
        section.add("ASCAT_time", parseUtcTime(tokens[offset + 2]));
        section.add("ASCAT_reference-id", parseString(tokens[offset + 3]));
        section.add("ASCAT_upstreamfile", parseString(tokens[offset + 4]));
        section.add("ASCAT_sigma_40", parseFloat(tokens[offset + 5]));
        section.add("ASCAT_sigma_40_mask", parseFloat(tokens[offset + 6]));
        section.add("ASCAT_nb_samples", parseShort(tokens[offset + 7]));
        section.add("ASCAT_warning", parseShort(tokens[offset + 8]));
        section.add("ASCAT_std", parseFloat(tokens[offset + 9]));

        return section;
    }
}
