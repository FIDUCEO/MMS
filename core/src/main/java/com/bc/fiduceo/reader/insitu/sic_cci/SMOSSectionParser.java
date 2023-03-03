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

public class SMOSSectionParser extends AbstractSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables, "SMOS_");

        ArrayList<Attribute> attributes = new ArrayList<>();
        variables.add(new VariableProxy("SMOS_upstreamfile", DataType.CHAR, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "SMOS TBV (at 40 deg incidence angle)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMOS_Tbv", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "SMOS TBH (at 40 deg incidence angle)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMOS_Tbh", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Stddev of TBV"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMOS_RMSE_v", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Stddev of TBH"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMOS_RMSE_h", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of data points in daily map"));
        variables.add(new VariableProxy("SMOS_nmp", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of invalid data points (potentially RFI contaminated)"));
        variables.add(new VariableProxy("SMOS_dataloss", DataType.SHORT, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 11;
    }

    @Override
    String getNamePrefix() {
        return "SMOS";
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("SMOS_latitude", parseFloat(tokens[offset]));
        section.add("SMOS_longitude", parseFloat(tokens[offset + 1]));
        section.add("SMOS_time", parseUtcTime(tokens[offset + 2]));
        section.add("SMOS_reference-id", parseString(tokens[offset + 3]));
        section.add("SMOS_upstreamfile", parseString(tokens[offset + 4]));
        section.add("SMOS_Tbv", parseFloat(tokens[offset + 5]));
        section.add("SMOS_Tbh", parseFloat(tokens[offset + 6]));
        section.add("SMOS_RMSE_v", parseFloat(tokens[offset + 7]));
        section.add("SMOS_RMSE_h", parseFloat(tokens[offset + 8]));
        section.add("SMOS_nmp", parseShort(tokens[offset + 9]));
        section.add("SMOS_dataloss", parseShort(tokens[offset + 10]));

        return section;
    }
}
