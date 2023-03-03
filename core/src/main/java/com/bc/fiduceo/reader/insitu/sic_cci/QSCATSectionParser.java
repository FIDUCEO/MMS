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

public class QSCATSectionParser extends AbstractSectionParser {

    @Override
    List<Variable> getVariables() {
        final List<Variable> variables = new ArrayList<>();

        createCommonVariables(variables, "QSCAT_");

        ArrayList<Attribute> attributes = new ArrayList<>();
        variables.add(new VariableProxy("QSCAT_upstreamfile", DataType.CHAR, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "QUIKSCAT backscatter (dB) (HH)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("QSCAT_sigma0_inner", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "QUIKSCAT backscatter (dB) - masked (HH)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("QSCAT_sigma0_mask_inner", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of QUIKSCAT samples (HH)"));
        variables.add(new VariableProxy("QSCAT_nb_inner", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Standard deviation of nb_inner datapoints in daily map (HH) - (not dB)"));
        variables.add(new VariableProxy("QSCAT_std_inner", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "QUIKSCAT backscatter (dB) (VV)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("QSCAT_sigma0_outer", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "QUIKSCAT backscatter (dB) - masked (VV)"));
        attributes.add(new Attribute(CF_UNITS_NAME, "db"));
        variables.add(new VariableProxy("QSCAT_sigma0_mask_outer", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Number of QUIKSCAT samples (VV)"));
        variables.add(new VariableProxy("QSCAT_nb_outer", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Standard deviation of nb_outer datapoints in daily map (VV) - (not dB)"));
        variables.add(new VariableProxy("QSCAT_std_outer", DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 13;
    }

    @Override
    String getNamePrefix() {
        return "QSCAT";
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("QSCAT_latitude", parseFloat(tokens[offset]));
        section.add("QSCAT_longitude", parseFloat(tokens[offset + 1]));
        section.add("QSCAT_time", parseUtcTime(tokens[offset + 2]));
        section.add("QSCAT_reference-id", parseString(tokens[offset + 3]));
        section.add("QSCAT_upstreamfile", parseString(tokens[offset + 4]));
        section.add("QSCAT_sigma0_inner", parseFloat(tokens[offset + 5]));
        section.add("QSCAT_sigma0_mask_inner", parseFloat(tokens[offset + 6]));
        section.add("QSCAT_nb_inner", parseShort(tokens[offset + 7]));
        section.add("QSCAT_std_inner", parseFloat(tokens[offset + 8]));
        section.add("QSCAT_sigma0_outer", parseFloat(tokens[offset + 9]));
        section.add("QSCAT_sigma0_mask_outer", parseFloat(tokens[offset + 10]));
        section.add("QSCAT_nb_outer", parseShort(tokens[offset + 11]));
        section.add("QSCAT_std_outer", parseFloat(tokens[offset + 12]));

        return section;
    }
}
