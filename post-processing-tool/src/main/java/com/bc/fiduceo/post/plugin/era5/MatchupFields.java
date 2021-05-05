package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static com.bc.fiduceo.post.plugin.era5.VariableUtils.*;

class MatchupFields extends FieldsProcessor {

    private static final int SECS_PER_HOUR = 3600;

    private Map<String, TemplateVariable> variables;

    private static Array createTimeArray(NetcdfFile reader, MatchupFieldsConfiguration matchupConfig, int numTimeSteps, Variable nwpTimeVariable) throws IOException, InvalidRangeException {
        final Array timeArray = VariableUtils.readTimeArray(matchupConfig.get_time_variable_name(), reader);
        final Array era5TimeArray = convertToEra5TimeStamp(timeArray);
        final int numMatchups = era5TimeArray.getShape()[0];

        final Array targetTimeArray = Array.factory(DataType.INT, nwpTimeVariable.getShape());
        final Index targetIndex = targetTimeArray.getIndex();

        final int offset = -matchupConfig.get_time_steps_past();
        final Index index = era5TimeArray.getIndex();
        for (int i = 0; i < numMatchups; i++) {
            index.set(i);
            final int timeStamp = era5TimeArray.getInt(index);
            final boolean isTimeFill = VariableUtils.isTimeFill(timeStamp);
            for (int k = 0; k < numTimeSteps; k++) {
                targetIndex.set(i, k);
                if (isTimeFill) {
                    targetTimeArray.setInt(targetIndex, TIME_FILL);
                } else {
                    final int timeStep = timeStamp + (offset + k) * SECS_PER_HOUR;
                    targetTimeArray.setInt(targetIndex, timeStep);
                }
            }
        }
        return targetTimeArray;
    }

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

    void compute(Configuration config, NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final Era5Archive era5Archive = new Era5Archive(config.getNWPAuxDir());
        final MatchupFieldsConfiguration matchupConfig = config.getMatchupFields();

        // allocate cache large enough to hold the time-series for one Era-5 variable
        final int numTimeSteps = matchupConfig.get_time_steps_future() + matchupConfig.get_time_steps_past() + 1;
        final VariableCache variableCache = new VariableCache(era5Archive, numTimeSteps);

        try {
            // open input time variable
            // + read completely
            // + convert to ERA-5 time stamps
            // + calculate time past and future timestamps for each matchup
            // + write to MMD
            final Variable nwpTimeVariable = NetCDFUtils.getVariable(writer, matchupConfig.get_nwp_time_variable_name());
            final Array targetTimeArray = createTimeArray(reader, matchupConfig, numTimeSteps, nwpTimeVariable);
            writer.write(nwpTimeVariable, targetTimeArray);

            // open longitude and latitude input variables
            // + read 1/1 subset
            // + scale if necessary
            final com.bc.fiduceo.core.Dimension geoDimension = new com.bc.fiduceo.core.Dimension("geoloc", 1, 1);
            final Array lonArray = readGeolocationVariable(geoDimension, reader, matchupConfig.get_longitude_variable_name());
            final Array latArray = readGeolocationVariable(geoDimension, reader, matchupConfig.get_latitude_variable_name());

            // for all nwp variables
            // - get variable
            // - get data array
            final int numMatches = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);
            final int[] nwpShape = new int[]{1, 1, 1};
            final int[] nwpOffset = new int[]{0, 0, 0};
            final Index timeIndex = targetTimeArray.getIndex();
            final Set<String> variableKeys = variables.keySet();
            final HashMap<String, Array> targetArrays = allocateTargetData(writer, variables);

            // iterate over matchups
            for (final String variableKey : variableKeys) {
                final Array targetArray = targetArrays.get(variableKey);
                final Index targetIndex = targetArray.getIndex();

                for (int m = 0; m < numMatches; m++) {
                    nwpOffset[0] = m;

                    final Array lonLayer = lonArray.section(nwpOffset, nwpShape);
                    final Array latLayer = latArray.section(nwpOffset, nwpShape);

                    final InterpolationContext interpolationContext = Era5PostProcessing.getInterpolationContext(lonLayer, latLayer);
                    final Rectangle layerRegion = interpolationContext.getEra5Region();
                    final int[] offset = new int[]{layerRegion.y, layerRegion.x};
                    final int[] shape = new int[]{layerRegion.height, layerRegion.width};

                    // iterate over time stamps
                    for (int t = 0; t < numTimeSteps; t++) {
                        timeIndex.set(m, t);
                        targetIndex.set(m, t);

                        final int timeStamp = targetTimeArray.getInt(timeIndex);
                        if (VariableUtils.isTimeFill(timeStamp)) {
                            targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                            continue;
                        }

                        VariableCache.CacheEntry cacheEntry = variableCache.get(variableKey, timeStamp);

                        // read and get rid of fake z-dimension
                        Array subset = cacheEntry.array.section(offset, shape);
                        subset = NetCDFUtils.scaleIfNecessary(cacheEntry.variable, subset);
                        final Index subsetIndex = subset.getIndex();
                        final BilinearInterpolator bilinearInterpolator = interpolationContext.get(0, 0);
                        if (bilinearInterpolator == null) {
                            targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                            continue;
                        }

                        subsetIndex.set(0, 0);
                        final float c00 = subset.getFloat(subsetIndex);

                        subsetIndex.set(0, 1);
                        final float c10 = subset.getFloat(subsetIndex);

                        subsetIndex.set(1, 0);
                        final float c01 = subset.getFloat(subsetIndex);

                        subsetIndex.set(1, 1);
                        final float c11 = subset.getFloat(subsetIndex);
                        final double interpolated = bilinearInterpolator.interpolate(c00, c10, c01, c11);

                        targetArray.setFloat(targetIndex, (float) interpolated);
                    }
                }
            }

            for (final String variableKey : variableKeys) {
                final TemplateVariable templateVariable = variables.get(variableKey);
                final Variable targetVariable = writer.findVariable(NetCDFUtils.escapeVariableName(templateVariable.getName()));
                final Array targetArray = targetArrays.get(variableKey);
                writer.write(targetVariable, targetArray);
            }
        } finally {
            variableCache.close();
        }
    }

    private void addTimeVariable(MatchupFieldsConfiguration matchupFieldsConfig, List<Dimension> dimensions, NetcdfFileWriter writer) {
        final String timeVariableName = matchupFieldsConfig.get_nwp_time_variable_name();
//        final String escapedName = NetCDFUtils.escapeVariableName(timeVariableName);
        final Variable variable = writer.addVariable(timeVariableName, DataType.INT, dimensions);
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
        final String time_dim_name = NetCDFUtils.escapeVariableName(matchupFieldsConfig.get_time_dim_name());

        final Dimension timeDimension = writer.addDimension(time_dim_name, time_dim_length);
        dimensions.add(timeDimension);

        return dimensions;
    }

    // package access for testing purpose only tb 2020-12-03
    Map<String, TemplateVariable> getVariables(MatchupFieldsConfiguration configuration) {
        final HashMap<String, TemplateVariable> variablesMap = new HashMap<>();

        variablesMap.put("an_sfc_u10", createTemplate(configuration.get_an_u10_name(), "m s**-1", "10 metre U wind component", null, false));
        variablesMap.put("an_sfc_v10", createTemplate(configuration.get_an_v10_name(), "m s**-1", "10 metre V wind component", null, false));
        variablesMap.put("an_sfc_siconc", createTemplate(configuration.get_an_siconc_name(), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
        variablesMap.put("an_sfc_sst", createTemplate(configuration.get_an_sst_name(), "K", "Sea surface temperature", null, false));
        variablesMap.put("fc_sfc_metss", createTemplate(configuration.get_fc_metss_name(), "N m**-2", "Mean eastward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mntss", createTemplate(configuration.get_fc_mntss_name(), "N m**-2", "Mean northward turbulent surface stress", null, false));
        variablesMap.put("fc_sfc_mslhf", createTemplate(configuration.get_fc_mslhf_name(), "W m**-2", "Mean surface latent heat flux", null, false));
        variablesMap.put("fc_sfc_msnlwrf", createTemplate(configuration.get_fc_msnlwrf_name(), "W m**-2", "Mean surface net long-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msnswrf", createTemplate(configuration.get_fc_msnswrf_name(), "W m**-2", "Mean surface net short-wave radiation flux", null, false));
        variablesMap.put("fc_sfc_msshf", createTemplate(configuration.get_fc_msshf_name(), "W m**-2", "Mean surface sensible heat flux", null, false));

        return variablesMap;
    }
}
