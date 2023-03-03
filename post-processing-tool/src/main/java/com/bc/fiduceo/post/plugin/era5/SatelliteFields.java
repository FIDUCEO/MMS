package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.Dimension;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.bc.fiduceo.post.plugin.era5.VariableUtils.*;

class SatelliteFields extends FieldsProcessor {

    private List<Dimension> dimension2d;
    private List<Dimension> dimension3d;
    private Map<String, TemplateVariable> variables;
    private Era5Collection collection;

    void prepare(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer, Era5Collection collection) {
        satFieldsConfig.verify();
        setDimensions(satFieldsConfig, writer, reader);

        this.collection = collection;

        variables = getVariables(satFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            final List<Dimension> dimensions = getDimensions(template);

            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            VariableUtils.addAttributes(template, variable);
        }

        addTimeVariable(satFieldsConfig, writer);
    }

    void compute(Configuration config, NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final SatelliteFieldsConfiguration satFieldsConfig = config.getSatelliteFields();
        final int numLayers = satFieldsConfig.get_z_dim();
        final Era5Archive era5Archive = new Era5Archive(config.getNWPAuxDir(), collection);
        final VariableCache variableCache = new VariableCache(era5Archive, 52); // 4 * 13 variables tb 2020-11-25

        try {
            // open input time variable
            // + read completely
            // + convert to ERA-5 time stamps
            // + write to MMD
            final Array timeArray = VariableUtils.readTimeArray(satFieldsConfig.get_time_variable_name(), reader);
            final Array era5TimeArray = convertToEra5TimeStamp(timeArray);
            final Variable targetTimeVariable = NetCDFUtils.getVariable(writer, satFieldsConfig.get_nwp_time_variable_name());
            writer.write(targetTimeVariable, era5TimeArray);

            // open longitude and latitude input variables
            // + read completely or specified x/y subset
            // + scale if necessary
            final com.bc.fiduceo.core.Dimension geoDimension = new com.bc.fiduceo.core.Dimension("geoloc", satFieldsConfig.get_x_dim(), satFieldsConfig.get_y_dim());
            final Array lonArray = readGeolocationVariable(geoDimension, reader, satFieldsConfig.get_longitude_variable_name());
            final Array latArray = readGeolocationVariable(geoDimension, reader, satFieldsConfig.get_latitude_variable_name());

            // prepare data
            // + calculate dimensions
            // + allocate target data arrays
            final int numMatches = NetCDFUtils.getDimensionLength(satFieldsConfig.getMatchupDimensionName(), reader);
            final int[] nwpShape = getNwpShape(geoDimension, lonArray.getShape());
            final int[] nwpOffset = getNwpOffset(lonArray.getShape(), nwpShape);
            final int[] nwpStride = {1, 1, 1};
            final HashMap<String, Array> targetArrays = allocateTargetData(writer, variables);

            // iterate over matchups
            //   + convert geo-region to era-5 extract
            //   + prepare interpolation context
            final Index timeIndex = era5TimeArray.getIndex();
            for (int m = 0; m < numMatches; m++) {
                nwpOffset[0] = m;
                nwpShape[0] = 1;

                // get a subset of one matchup layer and convert to 2D dataset
                final Array lonLayer = lonArray.sectionNoReduce(nwpOffset, nwpShape, nwpStride).reduce(0).copy();
                final Array latLayer = latArray.sectionNoReduce(nwpOffset, nwpShape, nwpStride).reduce(0).copy();

                final int[] shape = lonLayer.getShape();
                final int width = shape[1];
                final int height = shape[0];

                final InterpolationContext interpolationContext = Era5PostProcessing.getInterpolationContext(lonLayer, latLayer);
                final Rectangle layerRegion = interpolationContext.getEra5Region();

                timeIndex.set(m);
                final int era5Time = era5TimeArray.getInt(timeIndex);
                final boolean isTimeFill = VariableUtils.isTimeFill(era5Time);

                //   iterate over variables
                //     + assemble variable file name
                //     + read variable data extract
                //     + interpolate (2d, 3d per layer)
                //     - store to target raster
                final Set<String> variableKeys = variables.keySet();
                for (final String variableKey : variableKeys) {
                    VariableCache.CacheEntry cacheEntry = variableCache.get(variableKey, era5Time);
                    final Array subset = readSubset(numLayers, layerRegion, cacheEntry);
                    final Index subsetIndex = subset.getIndex();

                    final Array targetArray = targetArrays.get(variableKey);
                    final Index targetIndex = targetArray.getIndex();

                    final int rank = subset.getRank();
                    if (rank == 2) {
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                targetIndex.set(m, y, x);
                                if (isTimeFill) {
                                    targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                                    continue;
                                }

                                final BilinearInterpolator interpolator = interpolationContext.get(x, y);
                                if (interpolator == null) {
                                    targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                                    continue;
                                }

                                final int offsetX = interpolator.getXMin() - layerRegion.x;
                                final int offsetY = interpolator.getYMin() - layerRegion.y;

                                subsetIndex.set(offsetY, offsetX);
                                final float c00 = subset.getFloat(subsetIndex);

                                subsetIndex.set(offsetY, offsetX + 1);
                                final float c10 = subset.getFloat(subsetIndex);

                                subsetIndex.set(offsetY + 1, offsetX);
                                final float c01 = subset.getFloat(subsetIndex);

                                subsetIndex.set(offsetY + 1, offsetX + 1);
                                final float c11 = subset.getFloat(subsetIndex);

                                final double interpolate = interpolator.interpolate(c00, c10, c01, c11);

                                targetArray.setFloat(targetIndex, (float) interpolate);
                            }
                        }
                    } else if (rank == 3) {
                        for (int z = 0; z < numLayers; z++) {
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    targetIndex.set(m, z, y, x);

                                    if (isTimeFill) {
                                        targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                                        continue;
                                    }

                                    final BilinearInterpolator interpolator = interpolationContext.get(x, y);
                                    if (interpolator == null) {
                                        targetArray.setFloat(targetIndex, TemplateVariable.getFillValue());
                                        continue;
                                    }

                                    final int offsetX = interpolator.getXMin() - layerRegion.x;
                                    final int offsetY = interpolator.getYMin() - layerRegion.y;

                                    subsetIndex.set(z, offsetY, offsetX);
                                    final float c00 = subset.getFloat(subsetIndex);

                                    subsetIndex.set(z, offsetY, offsetX + 1);
                                    final float c10 = subset.getFloat(subsetIndex);

                                    subsetIndex.set(z, offsetY + 1, offsetX);
                                    final float c01 = subset.getFloat(subsetIndex);

                                    subsetIndex.set(z, offsetY + 1, offsetX + 1);
                                    final float c11 = subset.getFloat(subsetIndex);

                                    final double interpolate = interpolator.interpolate(c00, c01, c10, c11);

                                    targetArray.setFloat(targetIndex, (float) interpolate);
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException("Unexpected variable rank: " + rank + "  " + variableKey);
                    }
                }
            }

            final Set<String> variableKeys = variables.keySet();
            for (final String variableKey : variableKeys) {
                final TemplateVariable templateVariable = variables.get(variableKey);
                final Array targetArray = targetArrays.get(variableKey);
                final Variable targetVariable = writer.findVariable(NetCDFUtils.escapeVariableName(templateVariable.getName()));
                writer.write(targetVariable, targetArray);
            }

        } finally {
            variableCache.close();
        }
    }

    private void addTimeVariable(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer) {
        final String nwp_time_variable_name = satFieldsConfig.get_nwp_time_variable_name();
        final Variable variable = writer.addVariable(nwp_time_variable_name, DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        variable.addAttribute(new Attribute("description", "Timestamp of ERA-5 data"));
        variable.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        variable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));
    }

    // package access for testing purpose only tb 2020-12-02
    List<Dimension> getDimensions(TemplateVariable template) {
        List<Dimension> dimensions;
        if (template.is3d()) {
            dimensions = dimension3d;
        } else {
            dimensions = dimension2d;
        }
        return dimensions;
    }

    // package access for testing purpose only tb 2020-12-02
    void setDimensions(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer, NetcdfFile reader) {
        final Dimension xDim = writer.addDimension(satFieldsConfig.get_x_dim_name(), satFieldsConfig.get_x_dim());
        final Dimension yDim = writer.addDimension(satFieldsConfig.get_y_dim_name(), satFieldsConfig.get_y_dim());
        final Dimension zDim = writer.addDimension(satFieldsConfig.get_z_dim_name(), satFieldsConfig.get_z_dim());

        final Dimension matchupDim = reader.findDimension(satFieldsConfig.getMatchupDimensionName());

        dimension2d = new ArrayList<>();
        dimension2d.add(matchupDim);
        dimension2d.add(yDim);
        dimension2d.add(xDim);

        dimension3d = new ArrayList<>();
        dimension3d.add(matchupDim);
        dimension3d.add(zDim);
        dimension3d.add(yDim);
        dimension3d.add(xDim);
    }

    Map<String, TemplateVariable> getVariables(SatelliteFieldsConfiguration configuration) {
        final HashMap<String, TemplateVariable> variablesMap = new HashMap<>();

        variablesMap.put("an_ml_q", createTemplate(configuration.get_an_q_name(), "kg kg**-1", "Specific humidity", "specific_humidity", true));
        variablesMap.put("an_ml_t", createTemplate(configuration.get_an_t_name(), "K", "Temperature", "air_temperature", true));
        variablesMap.put("an_ml_o3", createTemplate(configuration.get_an_o3_name(), "kg kg**-1", "Ozone mass mixing ratio", null, true));
        variablesMap.put("an_ml_lnsp", createTemplate(configuration.get_an_lnsp_name(), "~", "Logarithm of surface pressure", null, false));
        variablesMap.put("an_sfc_t2m", createTemplate(configuration.get_an_t2m_name(), "K", "2 metre temperature", null, false));
        variablesMap.put("an_sfc_u10", createTemplate(configuration.get_an_u10_name(), "m s**-1", "10 metre U wind component", null, false));
        variablesMap.put("an_sfc_v10", createTemplate(configuration.get_an_v10_name(), "m s**-1", "10 metre V wind component", null, false));
        variablesMap.put("an_sfc_siconc", createTemplate(configuration.get_an_siconc_name(), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
        variablesMap.put("an_sfc_msl", createTemplate(configuration.get_an_msl_name(), "Pa", "Mean sea level pressure", "air_pressure_at_mean_sea_level", false));
        variablesMap.put("an_sfc_skt", createTemplate(configuration.get_an_skt_name(), "K", "Skin temperature", null, false));
        variablesMap.put("an_sfc_sst", createTemplate(configuration.get_an_sst_name(), "K", "Sea surface temperature", null, false));
        variablesMap.put("an_sfc_tcc", createTemplate(configuration.get_an_tcc_name(), "(0 - 1)", "Total cloud cover", "cloud_area_fraction", false));
        variablesMap.put("an_sfc_tcwv", createTemplate(configuration.get_an_tcwv_name(), "kg m**-2", "Total column water vapour", "lwe_thickness_of_atmosphere_mass_content_of_water_vapor", false));

        return variablesMap;
    }
}
