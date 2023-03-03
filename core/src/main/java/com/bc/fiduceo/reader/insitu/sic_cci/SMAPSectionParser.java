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

public class SMAPSectionParser extends AbstractSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables, "SMAP_");

        ArrayList<Attribute> attributes = new ArrayList<>();
        variables.add(new StringVariable(new VariableProxy("SMAP_upstreamfile", DataType.STRING, attributes), 32));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "SMAP TBV (at 40 deg incidence angle)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMAP_Tbv", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "SMAP TBH (at 40 deg incidence angle)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMAP_Tbh", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Stddev of TBV"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMAP_RMSE_v", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Stddev of TBH"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy("SMAP_RMSE_h", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of data points in daily map"));
        variables.add(new VariableProxy("SMAP_nmp", DataType.SHORT, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 10;
    }

    @Override
    String getNamePrefix() {
        return "SMAP";
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("SMAP_latitude", parseFloat(tokens[offset]));
        section.add("SMAP_longitude", parseFloat(tokens[offset + 1]));
        section.add("SMAP_time", parseUtcTime(tokens[offset + 2]));
        section.add("SMAP_reference-id", parseString(tokens[offset + 3]));
        section.add("SMAP_upstreamfile", parseString(tokens[offset + 4]));
        section.add("SMAP_Tbv", parseFloat(tokens[offset + 5]));
        section.add("SMAP_Tbh", parseFloat(tokens[offset + 6]));
        section.add("SMAP_RMSE_v", parseFloat(tokens[offset + 7]));
        section.add("SMAP_RMSE_h", parseFloat(tokens[offset + 8]));
        section.add("SMAP_nmp", parseShort(tokens[offset + 9]));

        return section;
    }
}
