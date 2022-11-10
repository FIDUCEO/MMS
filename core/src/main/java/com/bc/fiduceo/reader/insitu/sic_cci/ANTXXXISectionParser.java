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

        createCommonVariables(variables);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-total", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-primary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-primary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-primary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-primary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-secondary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-secondary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-secondary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-secondary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_area_fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "percent"));
        variables.add(new VariableProxy("SIC-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Ice-type-tertiary ASPeCt code"));
        variables.add(new VariableProxy("Ice-type-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_ice_thickness"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("SIT-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        variables.add(new VariableProxy("Ridged-ice-fraction-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Ridge-height-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snow-cover-type-tertiary ASPeCt code"));
        variables.add(new VariableProxy("Snow-cover-type-tertiary", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "thickness_of_snowfall_amount"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy("Snow-depth-tertiary", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "sea_water_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_C"));
        variables.add(new VariableProxy("Sea-water-temperature", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "air_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_C"));
        variables.add(new VariableProxy("Air-temperature", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "wind_speed"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m s-1"));
        variables.add(new VariableProxy("Wind-speed", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(short.class)));
        attributes.add(new Attribute(CF_UNITS_NAME, "degree"));
        variables.add(new VariableProxy("Wind-direction", DataType.SHORT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Visibility", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Cloud-cover", DataType.BYTE, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(byte.class)));
        variables.add(new VariableProxy("Weather", DataType.BYTE, attributes));

        return variables;
    }

    @Override
    int getNumVariables() {
        return 33;
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();

        section.add("latitude", parseFloat(tokens[offset]));
        section.add("longitude", parseFloat(tokens[offset + 1]));
        section.add("time", parseUtcTime(tokens[offset + 2]));
        section.add("reference-id", parseString(tokens[offset + 3]));
        section.add("SIC-total", parseByte(tokens[offset + 4]));
        section.add("SIC-primary", parseByte(tokens[offset + 5]));
        section.add("Ice-type-primary", parseByte(tokens[offset + 6]));
        section.add("SIT-primary", parseFloat(tokens[offset + 7]));
        section.add("Ridged-ice-fraction-primary", parseFloat(tokens[offset + 8]));
        section.add("Ridged-height-primary", parseFloat(tokens[offset + 9]));
        section.add("Snow-cover-type-primary", parseByte(tokens[offset + 10]));
        section.add("Snow-depth-primary", parseFloat(tokens[offset + 11]));
        section.add("SIC-secondary", parseByte(tokens[offset + 12]));
        section.add("Ice-type-secondary", parseByte(tokens[offset + 13]));
        section.add("SIT-secondary", parseFloat(tokens[offset + 14]));
        section.add("Ridged-ice-fraction-secondary", parseFloat(tokens[offset + 15]));
        section.add("Ridged-height-secondary", parseFloat(tokens[offset + 16]));
        section.add("Snow-cover-type-secondary", parseByte(tokens[offset + 17]));
        section.add("Snow-depth-secondary", parseFloat(tokens[offset + 18]));
        section.add("SIC-tertiary", parseByte(tokens[offset + 19]));
        section.add("Ice-type-tertiary", parseByte(tokens[offset + 20]));
        section.add("SIT-tertiary", parseFloat(tokens[offset + 21]));
        section.add("Ridged-ice-fraction-tertiary", parseFloat(tokens[offset + 22]));
        section.add("Ridged-height-tertiary", parseFloat(tokens[offset + 23]));
        section.add("Snow-cover-type-tertiary", parseByte(tokens[offset + 24]));
        section.add("Snow-depth-tertiary", parseFloat(tokens[offset + 25]));
        section.add("Sea-water-temperature", parseFloat(tokens[offset + 26]));
        section.add("Air-temperature", parseFloat(tokens[offset + 27]));
        section.add("Wind-speed", parseFloat(tokens[offset + 28]));
        section.add("Wind-direction", parseShort(tokens[offset + 29]));
        section.add("Visibility", parseByte(tokens[offset + 30]));
        section.add("Cloud-cover", parseByte(tokens[offset + 31]));
        section.add("Weather", parseByte(tokens[offset + 32]));

        return section;
    }
}
