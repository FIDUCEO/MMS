package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.DataType;
import ucar.nc2.*;

import java.util.*;

class SatelliteFields {

    private List<Dimension> dimension2d;
    private List<Dimension> dimension3d;

    void prepare(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer) {
        setDimensions(satFieldsConfig, writer, reader);

        final Map<String, TemplateVariable> variables = getVariables(satFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            List<Dimension> dimensions;
            if (template.is3d()) {
                dimensions = dimension3d;
            } else {
                dimensions = dimension2d;
            }
            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            variable.addAttribute(new Attribute("units", template.getUnits()));
            variable.addAttribute(new Attribute("long_name", template.getLongName()));
            final String standardName = template.getStandardName();
            if (StringUtils.isNotNullAndNotEmpty(standardName)) {
                variable.addAttribute(new Attribute("standard_name", standardName));
            }
            variable.addAttribute(new Attribute("_FillValue", template.getFillValue()));
        }

        final Variable variable = writer.addVariable(satFieldsConfig.get_time_variable_name(), DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        variable.addAttribute(new Attribute("description", "Timestamp of ERA-5 data"));
        variable.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        variable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));

    }

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
        dimension2d.add(matchupDim);
        dimension3d.add(zDim);
        dimension3d.add(yDim);
        dimension3d.add(xDim);
    }

    void compute() {

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
