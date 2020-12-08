package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

import java.io.IOException;
import java.util.*;

import static com.bc.fiduceo.post.plugin.era5.VariableUtils.convertToEra5TimeStamp;

class MatchupFields {

    private Map<String, TemplateVariable> variables;

    void prepare(MatchupFieldsConfiguration matchupFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer) {
        matchupFieldsConfig.verify();

        final List<Dimension> dimensions = getDimensions(matchupFieldsConfig, writer, reader);

        variables = getVariables(matchupFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            VariableUtils.addAttributes(template, variable);
        }

        addTimeVariable(matchupFieldsConfig, dimensions, writer);
    }

    void compute(Configuration config, NetcdfFile reader) throws IOException, InvalidRangeException {
        final Era5Archive era5Archive = new Era5Archive(config.getNWPAuxDir());
        final MatchupFieldsConfiguration matchupConfig = config.getMatchupFields();

        // allocate cache large enough to hold the time-series for one Era-5 variable
        final int cacheSize = matchupConfig.get_time_steps_future() + matchupConfig.get_time_steps_past() + 1;
        final VariableCache variableCache = new VariableCache(era5Archive, cacheSize);

        try {
            // open input time variable
            // + read completely
            // + convert to ERA-5 time stamps
            // - calculate time past and future timestamps for each matchup
            // - write to MMD
            final Array timeArray = VariableUtils.readTimeArray(matchupConfig.get_time_variable_name(), reader);
            final Array era5TimeArray = convertToEra5TimeStamp(timeArray);

        }finally {
            variableCache.close();
        }
    }

    private void addTimeVariable(MatchupFieldsConfiguration matchupFieldsConfig, List<Dimension> dimensions, NetcdfFileWriter writer) {
        final String timeVariableName = matchupFieldsConfig.get_nwp_time_variable_name();
        final String escapedName = NetCDFUtils.escapeVariableName(timeVariableName);
        final Variable variable = writer.addVariable(escapedName, DataType.INT, dimensions);
        variable.addAttribute(new Attribute("description", "Timestamp of ERA-5 data"));
        variable.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        variable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));
    }

    // package access for testing purpose only tb 2020-12-02
    List<Dimension> getDimensions(MatchupFieldsConfiguration matchupFieldsConfig, NetcdfFileWriter writer, NetcdfFile reader) {
        final ArrayList<Dimension> dimensions = new ArrayList<>();

        final Dimension matchupDim = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);
        dimensions.add(matchupDim);

        final int time_steps_past = matchupFieldsConfig.get_time_steps_past();
        final int time_steps_future = matchupFieldsConfig.get_time_steps_future();
        final int time_dim_length = time_steps_past + time_steps_future + 1;
        final String time_dim_name = matchupFieldsConfig.get_time_dim_name();

        final Dimension timeDimension = writer.addDimension(time_dim_name, time_dim_length);
        dimensions.add(timeDimension);

        return dimensions;
    }

    // package access for testing purpose only tb 2020-12-03
    Map<String, TemplateVariable> getVariables(MatchupFieldsConfiguration configuration) {
        final HashMap<String, TemplateVariable> variablesMap = new HashMap<>();

        variablesMap.put("an_sfc_u10", new TemplateVariable(configuration.get_an_u10_name(), "m s**-1", "10 metre U wind component", null, false));
        variablesMap.put("an_sfc_v10", new TemplateVariable(configuration.get_an_v10_name(), "m s**-1", "10 metre V wind component", null, false));
        variablesMap.put("an_sfc_siconc", new TemplateVariable(configuration.get_an_siconc_name(), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
        variablesMap.put("an_sfc_sst", new TemplateVariable(configuration.get_an_sst_name(), "K", "Sea surface temperature", null, false));
        variablesMap.put("fc_sfc_metss", new TemplateVariable(configuration.get_fc_metss_name(), "N m**-2", "Mean eastward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mntss", new TemplateVariable(configuration.get_fc_mntss_name(), "N m**-2", "Mean northward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mslhf", new TemplateVariable(configuration.get_fc_mslhf_name(), "W m**-2", "Mean surface latent heat flux", null, false));
        variablesMap.put("fc_sfc_msnlwrf", new TemplateVariable(configuration.get_fc_msnlwrf_name(), "W m**-2", "Mean surface net long-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msnswrf", new TemplateVariable(configuration.get_fc_msnswrf_name(), "W m**-2", "Mean surface net short-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msshf", new TemplateVariable(configuration.get_fc_msshf_name(), "W m**-2", "Mean surface sensible heat flux", null, false));

        return variablesMap;
    }
}
