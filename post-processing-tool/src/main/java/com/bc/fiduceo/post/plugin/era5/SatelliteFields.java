package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.*;
import ucar.nc2.*;
import ucar.nc2.Dimension;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

class SatelliteFields {

    private List<Dimension> dimension2d;
    private List<Dimension> dimension3d;
    private Map<String, TemplateVariable> variables;

    static int toEra5TimeStamp(int utc1970Seconds) {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(new Date(utc1970Seconds * 1000L));

        final int minutes = utcCalendar.get(Calendar.MINUTE);
        if (minutes >= 30) {
            utcCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        return (int) (utcCalendar.getTimeInMillis() / 1000L);
    }

    static int[] getNwpOffset(int[] shape, int[] nwpShape) {
        final int yOffset = shape[1] / 2 - nwpShape[1] / 2;
        final int xOffset = shape[2] / 2 - nwpShape[2] / 2;
        return new int[]{0, yOffset, xOffset};
    }

    static int[] getNwpShape(SatelliteFieldsConfiguration satFieldsConfig, int[] shape) {
        int xExtract = satFieldsConfig.get_x_dim();
        int yExtract = satFieldsConfig.get_y_dim();
        if (yExtract >= shape[1]) {
            yExtract = shape[1];
        }
        if (xExtract >= shape[2]) {
            xExtract = shape[2];
        }
        return new int[]{shape[0], yExtract, xExtract};
    }

    static Array convertToEra5TimeStamp(Array timeArray) {
        final Array era5TimeArray = Array.factory(timeArray.getDataType(), timeArray.getShape());
        final IndexIterator era5Iterator = era5TimeArray.getIndexIterator();
        final IndexIterator indexIterator = timeArray.getIndexIterator();
        while (indexIterator.hasNext() && era5Iterator.hasNext()) {
            final int satelliteTime = indexIterator.getIntNext();
            final int era5Time = toEra5TimeStamp(satelliteTime);
            era5Iterator.setIntNext(era5Time);
        }
        return era5TimeArray;
    }

    void prepare(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer) {
        setDimensions(satFieldsConfig, writer, reader);

        variables = getVariables(satFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            final List<Dimension> dimensions = getDimensions(template);

            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            addAttributes(template, variable);
        }

        addTimeVariable(satFieldsConfig, writer);
    }

    void compute(Configuration config, NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final SatelliteFieldsConfiguration satFieldsConfig = config.getSatelliteFields();
        // @todo 2 tb/tb ensure valid range 2020-11-25
        final int numLayers = satFieldsConfig.get_z_dim();
        final Era5Archive era5Archive = new Era5Archive(config.getNWPAuxDir());
        final VariableCache variableCache = new VariableCache(era5Archive, 52); // 4 * 13 variables tb 2020-11-25

        try {
            // open input time variable
            // + read completely
            // + convert to ERA-5 time stamps
            // + write to MMD
            final Array timeArray = readTimeArray(satFieldsConfig, reader);
            final Array era5TimeArray = convertToEra5TimeStamp(timeArray);
            writer.write(satFieldsConfig.get_nwp_time_variable_name(), era5TimeArray);

            // open longitude and latitude input variables
            // + read completely or specified x/y subset
            // + scale if necessary
            final Array lonArray = readGeolocationVariable(satFieldsConfig, reader, satFieldsConfig.get_longitude_variable_name());
            final Array latArray = readGeolocationVariable(satFieldsConfig, reader, satFieldsConfig.get_latitude_variable_name());

            // prepare data
            // + calculate dimensions
            // - allocate target data arrays
            final int numMatches = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, reader);
            final int[] nwpShape = getNwpShape(satFieldsConfig, lonArray.getShape());
            final int[] nwpOffset = getNwpOffset(lonArray.getShape(), nwpShape);
            final HashMap<String, Array> targetArrays = allocateTargetData(writer);

            // iterate over matchups
            //   + convert geo-region to era-5 extract
            //   + prepare interpolation context
            final Index timeIndex = era5TimeArray.getIndex();
            for (int m = 0; m < numMatches; m++) {
                nwpOffset[0] = m;
                nwpShape[0] = 1; // we read matchups layer by layer

                final Array lonLayer = lonArray.section(nwpOffset, nwpShape).reduce();
                final Array latLayer = latArray.section(nwpOffset, nwpShape).reduce();

                final int[] shape = lonLayer.getShape();
                final int width = shape[1];
                final int height = shape[0];

                //final Rectangle era5RasterPosition = Era5PostProcessing.getEra5RasterPosition(geoRegion);
                final InterpolationContext interpolationContext = Era5PostProcessing.getInterpolationContext(lonLayer, latLayer);
                final Rectangle layerRegion = interpolationContext.getEra5Region();

                timeIndex.set(m);
                final int era5Time = era5TimeArray.getInt(timeIndex);

                //   iterate over variables
                //     + assemble variable file name
                //     + read variable data extract
                //     + interpolate (2d, 3d per layer)
                //     - store to target raster
                final Set<String> variableKeys = variables.keySet();
                for (final String variableKey : variableKeys) {
                    final Variable variable = variableCache.get(variableKey, era5Time);
                    final Array subset = readSubset(numLayers, layerRegion, variableKey, variable);
                    final Index subsetIndex = subset.getIndex();

                    final Array targetArray = targetArrays.get(variableKey);
                    final Index targetIndex = targetArray.getIndex();

                    final int rank = subset.getRank();
                    if (rank == 2) {
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                final BilinearInterpolator interpolator = interpolationContext.get(x, y);
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

                                final double interpolate = interpolator.interpolate(c00, c01, c10, c11);
                                targetIndex.set(m, y, x);
                                targetArray.setFloat(targetIndex, (float) interpolate);
                            }
                        }
                    } else if (rank == 3) {
                        for (int z = 0; z < numLayers; z++) {
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    final BilinearInterpolator interpolator = interpolationContext.get(x, y);
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
                                    targetIndex.set(m, z, y, x);
                                    targetArray.setFloat(targetIndex, (float) interpolate);
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException("Unexpected variable rank: " + rank + "  " + variableKey);
                    }
                    
                    final TemplateVariable templateVariable = variables.get(variableKey);
                    final Variable targetVariable = writer.findVariable(templateVariable.getName());
                    writer.write(targetVariable, targetArray);
                }
            }
        } finally {
            variableCache.close();
        }
    }

    private HashMap<String, Array> allocateTargetData(NetcdfFileWriter writer) {
        final HashMap<String, Array> targetArrays = new HashMap<>();
        final Set<Map.Entry<String, TemplateVariable>> entries = variables.entrySet();
        for (final Map.Entry<String, TemplateVariable> entry : entries) {
            final TemplateVariable templateVariable = entry.getValue();
            final Variable variable = writer.findVariable(templateVariable.getName());
            final Array targetArray = Array.factory(DataType.FLOAT, variable.getShape());
            targetArrays.put(entry.getKey(), targetArray);
        }

        return targetArrays;
    }

    private Array readSubset(int numLayers, Rectangle era5RasterPosition, String variableKey, Variable variable) throws IOException, InvalidRangeException {
        final int rank = variable.getRank();
        Array subset;
        if (rank == 3) {
            final int[] origin = new int[]{0, era5RasterPosition.y, era5RasterPosition.x};
            final int[] shape = new int[]{1, era5RasterPosition.height, era5RasterPosition.width};
            subset = variable.read(origin, shape);
        } else if (rank == 4) {
            final int[] origin = new int[]{0, 0, era5RasterPosition.y, era5RasterPosition.x};
            final int[] shape = new int[]{1, numLayers, era5RasterPosition.height, era5RasterPosition.width};
            subset = variable.read(origin, shape);
        } else {
            throw new IOException("variable rank invalid: " + variableKey);
        }

        subset = subset.reduce();
        subset = NetCDFUtils.scaleIfNecessary(variable, subset);
        return subset;
    }

    private Array readGeolocationVariable(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader, String lonVarName) throws IOException, InvalidRangeException {
        final Variable geoVariable = NetCDFUtils.getVariable(reader, lonVarName);

        final int[] shape = geoVariable.getShape();

        final int[] nwpShape = getNwpShape(satFieldsConfig, shape);
        final int[] offset = getNwpOffset(shape, nwpShape);

        Array rawData = geoVariable.read(offset, nwpShape);

        final double scaleFactor = NetCDFUtils.getScaleFactor(geoVariable);
        final double addOffset = NetCDFUtils.getOffset(geoVariable);
        if (ReaderUtils.mustScale(scaleFactor, addOffset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, addOffset);
            rawData = MAMath.convert2Unpacked(rawData, scaleOffset);
        }
        return rawData;
    }

    private Array readTimeArray(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader) throws IOException, InvalidRangeException {
        final String timeVariableName = satFieldsConfig.get_time_variable_name();
        final Variable timeVariable = NetCDFUtils.getVariable(reader, timeVariableName);

        final Array timeArray;
        final int rank = timeVariable.getRank();

        // @todo 2 tb/tb this block might be of general interest, extract and test 2020-11-17
        if (rank == 1) {
            timeArray = timeVariable.read();
        } else if (rank == 2) {
            final int[] shape = timeVariable.getShape();
            final int shapeOffset = shape[1] / 2;
            final int[] offset = {0, shapeOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1});
        } else if (rank == 3) {
            final int[] shape = timeVariable.getShape();
            final int yOffset = shape[1] / 2;
            final int xOffset = shape[2] / 2;
            final int[] offset = {0, yOffset, xOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1, 1});
        } else {
            throw new IllegalArgumentException("Rank of time-variable not supported");
        }
        return timeArray;
    }

    private void addTimeVariable(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer) {
        final Variable variable = writer.addVariable(satFieldsConfig.get_nwp_time_variable_name(), DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        variable.addAttribute(new Attribute("description", "Timestamp of ERA-5 data"));
        variable.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        variable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));
    }

    private void addAttributes(TemplateVariable template, Variable variable) {
        variable.addAttribute(new Attribute("units", template.getUnits()));
        variable.addAttribute(new Attribute("long_name", template.getLongName()));
        final String standardName = template.getStandardName();
        if (StringUtils.isNotNullAndNotEmpty(standardName)) {
            variable.addAttribute(new Attribute("standard_name", standardName));
        }
        variable.addAttribute(new Attribute("_FillValue", template.getFillValue()));
    }

    private List<Dimension> getDimensions(TemplateVariable template) {
        List<Dimension> dimensions;
        if (template.is3d()) {
            dimensions = dimension3d;
        } else {
            dimensions = dimension2d;
        }
        return dimensions;
    }

    // @todo 2 tb/tb write tests for this pair of methods 2020-11-17
    private void setDimensions(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer, NetcdfFile reader) {
        satFieldsConfig.verify();
        final Dimension xDim = writer.addDimension(satFieldsConfig.get_x_dim_name(), satFieldsConfig.get_x_dim());
        final Dimension yDim = writer.addDimension(satFieldsConfig.get_y_dim_name(), satFieldsConfig.get_y_dim());

        int z_dim = satFieldsConfig.get_z_dim();
        if (z_dim < 1) {
            z_dim = 137; // the we take all levels tb 2020-11-16
        }
        final Dimension zDim = writer.addDimension(satFieldsConfig.get_z_dim_name(), z_dim);

        final Dimension matchupDim = reader.findDimension(FiduceoConstants.MATCHUP_COUNT);

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

        variablesMap.put("an_ml_q", new TemplateVariable(configuration.get_an_q_name(), "kg kg**-1", "Specific humidity", "specific_humidity", true));
        variablesMap.put("an_ml_t", new TemplateVariable(configuration.get_an_t_name(), "K", "Temperature", "air_temperature", true));
        variablesMap.put("an_ml_o3", new TemplateVariable(configuration.get_an_o3_name(), "kg kg**-1", "Ozone mass mixing ratio", null, true));
        variablesMap.put("an_ml_lnsp", new TemplateVariable(configuration.get_an_lnsp_name(), "~", "Logarithm of surface pressure", null, false));
        variablesMap.put("an_sfc_t2m", new TemplateVariable(configuration.get_an_t2m_name(), "K", "2 metre temperature", null, false));
        variablesMap.put("an_sfc_u10", new TemplateVariable(configuration.get_an_u10_name(), "m s**-1", "10 metre U wind component", null, false));
        variablesMap.put("an_sfc_v10", new TemplateVariable(configuration.get_an_v10_name(), "m s**-1", "10 metre V wind component", null, false));
        variablesMap.put("an_sfc_siconc", new TemplateVariable(configuration.get_an_siconc_name(), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
        variablesMap.put("an_sfc_msl", new TemplateVariable(configuration.get_an_msl_name(), "Pa", "Mean sea level pressure", "air_pressure_at_mean_sea_level", false));
        variablesMap.put("an_sfc_skt", new TemplateVariable(configuration.get_an_skt_name(), "K", "Skin temperature", null, false));
        variablesMap.put("an_sfc_sst", new TemplateVariable(configuration.get_an_sst_name(), "K", "Sea surface temperature", null, false));
        variablesMap.put("an_sfc_tcc", new TemplateVariable(configuration.get_an_tcc_name(), "(0 - 1)", "Total cloud cover", "cloud_area_fraction", false));
        variablesMap.put("an_sfc_tcwv", new TemplateVariable(configuration.get_an_tcwv_name(), "kg m**-2", "Total column water vapour", "lwe_thickness_of_atmosphere_mass_content_of_water_vapor", false));
        return variablesMap;
    }
}
