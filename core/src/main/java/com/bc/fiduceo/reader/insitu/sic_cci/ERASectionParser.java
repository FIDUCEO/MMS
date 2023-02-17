package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.netcdf.StringVariable;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
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
        variables.add(new StringVariable(new VariableProxy(prefix + "upstreamfile", DataType.STRING, attributes), 16));

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

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "2 metre dewpoint temperature"));
        attributes.add(new Attribute(CF_STANDARD_NAME, "dew_point_temperature"));
        attributes.add(new Attribute(CF_UNITS_NAME, "K"));
        variables.add(new VariableProxy(prefix + "d2m", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Total column vertically-integrated water vapour"));
        attributes.add(new Attribute(CF_UNITS_NAME, "kg m**-2"));
        variables.add(new VariableProxy(prefix + "tcwv", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Total column cloud liquid water"));
        attributes.add(new Attribute(CF_UNITS_NAME, "kg m**-2"));
        variables.add(new VariableProxy(prefix + "tclw", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Total column cloud ice water"));
        attributes.add(new Attribute(CF_UNITS_NAME, "kg m**-2"));
        variables.add(new VariableProxy(prefix + "tciw", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Surface solar radiation downwards"));
        attributes.add(new Attribute(CF_UNITS_NAME, "J m**-2"));
        variables.add(new VariableProxy(prefix + "ssrd", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Surface thermal radiation downwards"));
        attributes.add(new Attribute(CF_UNITS_NAME, "J m**-2"));
        variables.add(new VariableProxy(prefix + "strd", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Evaporation"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m of water equivalent"));
        variables.add(new VariableProxy(prefix + "e", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Total precipitation"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        variables.add(new VariableProxy(prefix + "tp", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Snowfall"));
        attributes.add(new Attribute(CF_UNITS_NAME, "m of water equivalent"));
        variables.add(new VariableProxy(prefix + "sf", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Forest albedo"));
        attributes.add(new Attribute(CF_UNITS_NAME, "(0 - 1)"));
        variables.add(new VariableProxy(prefix + "fal", DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_LONG_NAME, "Sea ice area fraction"));
        attributes.add(new Attribute(CF_UNITS_NAME, "(0 - 1)"));
        if (type.equals("ERA5")) {
            variables.add(new VariableProxy(prefix + "ci", DataType.FLOAT, attributes));
        } else {
            variables.add(new VariableProxy(prefix + "siconc", DataType.FLOAT, attributes));
        }

        return variables;
    }

    @Override
    int getNumVariables() {
        return 27;
    }

    @Override
    String getNamePrefix() {
        return type;
    }

    @Override
    Section parse(String[] tokens, int offset) throws ParseException {
        final Section section = new Section();
        final String prefix = type + "_";

        section.add(prefix + "latitude", parseFloat(tokens[offset]));
        section.add(prefix + "longitude", parseFloat(tokens[offset + 1]));
        section.add(prefix + "time", parseUtcTime(tokens[offset + 2]));
        section.add(prefix + "reference-id", parseString(tokens[offset + 3]));
        section.add(prefix + "upstreamfile", parseString(tokens[offset + 4]));
        section.add(prefix + "msl", parseFloat(tokens[offset + 5]));
        section.add(prefix + "u10", parseFloat(tokens[offset + 6]));
        section.add(prefix + "v10", parseFloat(tokens[offset + 7]));
        section.add(prefix + "ws", parseFloat(tokens[offset + 8]));
        section.add(prefix + "t2m", parseFloat(tokens[offset + 9]));
        section.add(prefix + "skt", parseFloat(tokens[offset + 10]));
        section.add(prefix + "istl1", parseFloat(tokens[offset + 11]));
        section.add(prefix + "istl2", parseFloat(tokens[offset + 12]));
        section.add(prefix + "istl3", parseFloat(tokens[offset + 13]));
        section.add(prefix + "istl4", parseFloat(tokens[offset + 14]));
        section.add(prefix + "sst", parseFloat(tokens[offset + 15]));
        section.add(prefix + "d2m", parseFloat(tokens[offset + 16]));
        section.add(prefix + "tcwv", parseFloat(tokens[offset + 17]));
        section.add(prefix + "tclw", parseFloat(tokens[offset + 18]));
        section.add(prefix + "tciw", parseFloat(tokens[offset + 19]));
        section.add(prefix + "ssrd", parseFloat(tokens[offset + 20]));
        section.add(prefix + "strd", parseFloat(tokens[offset + 21]));
        section.add(prefix + "e", parseFloat(tokens[offset + 22]));
        section.add(prefix + "tp", parseFloat(tokens[offset + 23]));
        section.add(prefix + "sf", parseFloat(tokens[offset + 24]));
        section.add(prefix + "fal", parseFloat(tokens[offset + 25]));

        final Array sic = parseFloat(tokens[offset + 26]);
        if (type.equals("ERA5")) {
            section.add(prefix + "ci", sic);
        } else {
            section.add(prefix + "siconc", sic);
        }

        return section;
    }
}
