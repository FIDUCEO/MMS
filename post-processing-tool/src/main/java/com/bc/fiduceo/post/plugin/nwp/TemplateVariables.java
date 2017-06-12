
/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;

import ucar.ma2.DataType;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_LONG_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_STANDARD_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_UNITS_NAME;

class TemplateVariables {

    private final List<TemplateVariable> timeSeriesAnalysisVariables;
    private final List<TemplateVariable> timeSeriesForecastVariables;
    private final List<TemplateVariable> sensorExtractVariables;

    TemplateVariables(Configuration configuration) {
        timeSeriesAnalysisVariables = createAnalysisVariables(configuration);
        timeSeriesForecastVariables = createForecastVariables(configuration);
        sensorExtractVariables = createSensorExtractVariables(configuration);
    }

    List<TemplateVariable> getTimeSeriesAnalysisVariables() {
        return timeSeriesAnalysisVariables;
    }

    List<TemplateVariable> getTimeSeriesForecastVariables() {
        return timeSeriesForecastVariables;
    }

    List<TemplateVariable> getAllTimeSeriesVariables() {
        final ArrayList<TemplateVariable> allVariables = new ArrayList<>();

        allVariables.addAll(timeSeriesAnalysisVariables);
        allVariables.addAll(timeSeriesForecastVariables);

        return allVariables;
    }

    List<TemplateVariable> getSensorExtractVariables() {
        return sensorExtractVariables;
    }

    private ArrayList<TemplateVariable> createAnalysisVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        if (!configuration.isTimeSeriesExtraction()) {
            return variables;
        }

        final String anDimensions = "matchup_count matchup.nwp.an.time";

        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final TemplateVariable seaIceFractionVariable = new TemplateVariable(timeSeriesConfiguration.getAn_CI_name(), "CI", DataType.FLOAT, anDimensions);
        seaIceFractionVariable.addAttribute(CF_LONG_NAME, "Sea-ice cover");
        seaIceFractionVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(timeSeriesConfiguration.getAn_SSTK_name(), "SSTK", DataType.FLOAT, anDimensions);
        sstVariable.addAttribute(CF_LONG_NAME, "Sea surface temperature");
        sstVariable.addAttribute(CF_UNITS_NAME, "K");
        sstVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable eastWindVariable = new TemplateVariable(timeSeriesConfiguration.getAn_U10_name(), "U10", DataType.FLOAT, anDimensions);
        eastWindVariable.addAttribute(CF_STANDARD_NAME, "eastward_wind");
        eastWindVariable.addAttribute(CF_LONG_NAME, "10 metre U wind component");
        eastWindVariable.addAttribute(CF_UNITS_NAME, "m s**-1");
        eastWindVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        eastWindVariable.addAttribute("source", "GRIB data");
        variables.add(eastWindVariable);

        final TemplateVariable northWindVariable = new TemplateVariable(timeSeriesConfiguration.getAn_V10_name(), "V10", DataType.FLOAT, anDimensions);
        northWindVariable.addAttribute(CF_STANDARD_NAME, "northward_wind");
        northWindVariable.addAttribute(CF_LONG_NAME, "10 metre V wind component");
        northWindVariable.addAttribute(CF_UNITS_NAME, "m s**-1");
        northWindVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        northWindVariable.addAttribute("source", "GRIB data");
        variables.add(northWindVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(timeSeriesConfiguration.getAn_TCWV_name(), "TCWV", DataType.FLOAT, anDimensions);
        totalColumnWaterVapourVariable.addAttribute(CF_STANDARD_NAME, "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute(CF_LONG_NAME, "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute(CF_UNITS_NAME, "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(timeSeriesConfiguration.getAn_CLWC_name(), Constants.CLWC_NAME, DataType.FLOAT, anDimensions);
        clwcVariable.addAttribute(CF_STANDARD_NAME, "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute(CF_LONG_NAME, "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        clwcVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0e33);
        clwcVariable.addAttribute("source", "GRIB data");
        variables.add(clwcVariable);

        return variables;
    }

    private ArrayList<TemplateVariable> createForecastVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        if (!configuration.isTimeSeriesExtraction()) {
            return variables;
        }

        final String fcDimensions = "matchup_count matchup.nwp.fc.time";

        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final TemplateVariable evaporationVariable = new TemplateVariable(timeSeriesConfiguration.getFc_E_name(), "E", DataType.FLOAT, fcDimensions);
        evaporationVariable.addAttribute(CF_STANDARD_NAME, "lwe_thickness_of_water_evaporation_amount");
        evaporationVariable.addAttribute(CF_LONG_NAME, "Evaporation");
        evaporationVariable.addAttribute(CF_UNITS_NAME, "m of water");
        evaporationVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        evaporationVariable.addAttribute("source", "GRIB data");
        variables.add(evaporationVariable);

        final TemplateVariable sshfVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSHF_name(), "SSHF", DataType.FLOAT, fcDimensions);
        sshfVariable.addAttribute(CF_STANDARD_NAME, "surface_upward_sensible_heat_flux");
        sshfVariable.addAttribute(CF_LONG_NAME, "Surface sensible heat flux");
        sshfVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        sshfVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        sshfVariable.addAttribute("source", "GRIB data");
        variables.add(sshfVariable);

        final TemplateVariable slhfVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SLHF_name(), "SLHF", DataType.FLOAT, fcDimensions);
        slhfVariable.addAttribute(CF_STANDARD_NAME, "surface_upward_latent_heat_flux");
        slhfVariable.addAttribute(CF_LONG_NAME, "Surface latent heat flux");
        slhfVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        slhfVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        slhfVariable.addAttribute("source", "GRIB data");
        variables.add(slhfVariable);

        final TemplateVariable ssrdVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSRD_name(), "SSRD", DataType.FLOAT, fcDimensions);
        ssrdVariable.addAttribute(CF_STANDARD_NAME, "surface_downwelling_shortwave_flux_in_air");
        ssrdVariable.addAttribute(CF_LONG_NAME, "Surface solar radiation downwards");
        ssrdVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        ssrdVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        ssrdVariable.addAttribute("source", "GRIB data");
        variables.add(ssrdVariable);

        final TemplateVariable strdVariable = new TemplateVariable(timeSeriesConfiguration.getFc_STRD_name(), "STRD", DataType.FLOAT, fcDimensions);
        strdVariable.addAttribute(CF_LONG_NAME, "Surface thermal radiation downwards");
        strdVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        strdVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        strdVariable.addAttribute("source", "GRIB data");
        variables.add(strdVariable);

        final TemplateVariable ssrVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSR_name(), "SSR", DataType.FLOAT, fcDimensions);
        ssrVariable.addAttribute(CF_STANDARD_NAME, "surface_net_upward_longwave_flux");
        ssrVariable.addAttribute(CF_LONG_NAME, "Surface solar radiation");
        ssrVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        ssrVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        ssrVariable.addAttribute("source", "GRIB data");
        variables.add(ssrVariable);

        final TemplateVariable strVariable = new TemplateVariable(timeSeriesConfiguration.getFc_STR_name(), "STR", DataType.FLOAT, fcDimensions);
        strVariable.addAttribute(CF_STANDARD_NAME, "surface_net_upward_shortwave_flux");
        strVariable.addAttribute(CF_LONG_NAME, "Surface thermal radiation");
        strVariable.addAttribute(CF_UNITS_NAME, "W m**-2 s");
        strVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        strVariable.addAttribute("source", "GRIB data");
        variables.add(strVariable);

        final TemplateVariable ewssVariable = new TemplateVariable(timeSeriesConfiguration.getFc_EWSS_name(), "EWSS", DataType.FLOAT, fcDimensions);
        ewssVariable.addAttribute(CF_STANDARD_NAME, "surface_downward_eastward_stress");
        ewssVariable.addAttribute(CF_LONG_NAME, "East-west surface stress");
        ewssVariable.addAttribute(CF_UNITS_NAME, "N m**-2 s");
        ewssVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        ewssVariable.addAttribute("source", "GRIB data");
        variables.add(ewssVariable);

        final TemplateVariable nsssVariable = new TemplateVariable(timeSeriesConfiguration.getFc_NSSS_name(), "NSSS", DataType.FLOAT, fcDimensions);
        nsssVariable.addAttribute(CF_STANDARD_NAME, "surface_downward_northward_stress");
        nsssVariable.addAttribute(CF_LONG_NAME, "North-south surface stress");
        nsssVariable.addAttribute(CF_UNITS_NAME, "N m**-2 s");
        nsssVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        nsssVariable.addAttribute("source", "GRIB data");
        variables.add(nsssVariable);

        final TemplateVariable tpVariable = new TemplateVariable(timeSeriesConfiguration.getFc_TP_name(), "TP", DataType.FLOAT, fcDimensions);
        tpVariable.addAttribute(CF_LONG_NAME, "Total precipitation");
        tpVariable.addAttribute(CF_UNITS_NAME, "m");
        tpVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        tpVariable.addAttribute("source", "GRIB data");
        variables.add(tpVariable);

        final TemplateVariable u10Variable = new TemplateVariable(timeSeriesConfiguration.getFc_U10_name(), "U10", DataType.FLOAT, fcDimensions);
        u10Variable.addAttribute(CF_STANDARD_NAME, "eastward_wind");
        u10Variable.addAttribute(CF_LONG_NAME, "10 metre U wind component");
        u10Variable.addAttribute(CF_UNITS_NAME, "m s**-1");
        u10Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        u10Variable.addAttribute("source", "GRIB data");
        variables.add(u10Variable);

        final TemplateVariable v10Variable = new TemplateVariable(timeSeriesConfiguration.getFc_V10_name(), "V10", DataType.FLOAT, fcDimensions);
        v10Variable.addAttribute(CF_STANDARD_NAME, "northward_wind");
        v10Variable.addAttribute(CF_LONG_NAME, "10 metre V wind component");
        v10Variable.addAttribute(CF_UNITS_NAME, "m s**-1");
        v10Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        v10Variable.addAttribute("source", "GRIB data");
        variables.add(v10Variable);

        final TemplateVariable d2Variable = new TemplateVariable(timeSeriesConfiguration.getFc_D2_name(), "D2", DataType.FLOAT, fcDimensions);
        d2Variable.addAttribute(CF_STANDARD_NAME, "dew_point_temperature");
        d2Variable.addAttribute(CF_LONG_NAME, "2 metre dewpoint temperature");
        d2Variable.addAttribute(CF_UNITS_NAME, "K");
        d2Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        d2Variable.addAttribute("source", "GRIB data");
        variables.add(d2Variable);

        final TemplateVariable t2Variable = new TemplateVariable(timeSeriesConfiguration.getFc_T2_name(), "T2", DataType.FLOAT, fcDimensions);
        t2Variable.addAttribute(CF_STANDARD_NAME, "air_temperature");
        t2Variable.addAttribute(CF_LONG_NAME, "2 metre temperature");
        t2Variable.addAttribute(CF_UNITS_NAME, "K");
        t2Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        t2Variable.addAttribute("source", "GRIB data");
        variables.add(t2Variable);

        final TemplateVariable blhVariable = new TemplateVariable(timeSeriesConfiguration.getFc_BLH_name(), "BLH", DataType.FLOAT, fcDimensions);
        blhVariable.addAttribute(CF_LONG_NAME, "Boundary layer height");
        blhVariable.addAttribute(CF_UNITS_NAME, "m");
        blhVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        blhVariable.addAttribute("source", "GRIB data");
        variables.add(blhVariable);

        final TemplateVariable mslVariable = new TemplateVariable(timeSeriesConfiguration.getFc_MSL_name(), "MSL", DataType.FLOAT, fcDimensions);
        mslVariable.addAttribute(CF_STANDARD_NAME, "air_pressure_at_sea_level");
        mslVariable.addAttribute(CF_LONG_NAME, "Mean sea-level pressure");
        mslVariable.addAttribute(CF_UNITS_NAME, "Pa");
        mslVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        mslVariable.addAttribute("source", "GRIB data");
        variables.add(mslVariable);

        final TemplateVariable sstkVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSTK_name(), "SSTK", DataType.FLOAT, fcDimensions);
        sstkVariable.addAttribute(CF_LONG_NAME, "Sea surface temperature");
        sstkVariable.addAttribute(CF_UNITS_NAME, "K");
        sstkVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        sstkVariable.addAttribute("source", "GRIB data");
        variables.add(sstkVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(timeSeriesConfiguration.getFc_TCWV_name(), "TCWV", DataType.FLOAT, fcDimensions);
        totalColumnWaterVapourVariable.addAttribute(CF_STANDARD_NAME, "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute(CF_LONG_NAME, "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute(CF_UNITS_NAME, "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(timeSeriesConfiguration.getFc_CLWC_name(), Constants.CLWC_NAME, DataType.FLOAT, fcDimensions);
        clwcVariable.addAttribute(CF_STANDARD_NAME, "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute(CF_LONG_NAME, "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        clwcVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0e33);
        clwcVariable.addAttribute("source", "GRIB data");
        variables.add(clwcVariable);

        return variables;
    }

    private List<TemplateVariable> createSensorExtractVariables(Configuration configuration) {
        final ArrayList<TemplateVariable> variables = new ArrayList<>();
        if (!configuration.isSensorExtraction()) {
            return variables;
        }

        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();
        final String x_dimensionName = sensorExtractConfiguration.getX_DimensionName();
        final String y_dimensionName = sensorExtractConfiguration.getY_DimensionName();
        final String z_dimensionName = sensorExtractConfiguration.getZ_DimensionName();
        final String extractDimensions = "matchup_count " + y_dimensionName + " " + x_dimensionName;
        final String profileDimensions = "matchup_count " + z_dimensionName + " " + y_dimensionName + " " + x_dimensionName;

        final TemplateVariable seaIceFractionVariable = new TemplateVariable(sensorExtractConfiguration.getAn_CI_name(), "CI", DataType.FLOAT, extractDimensions);
        seaIceFractionVariable.addAttribute(CF_LONG_NAME, "Sea-ice cover");
        seaIceFractionVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(sensorExtractConfiguration.getAn_SSTK_name(), "SSTK", DataType.FLOAT, extractDimensions);
        sstVariable.addAttribute(CF_LONG_NAME, "Sea surface temperature");
        sstVariable.addAttribute(CF_UNITS_NAME, "K");
        sstVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable u10Variable = new TemplateVariable(sensorExtractConfiguration.getAn_U10_name(), "U10", DataType.FLOAT, extractDimensions);
        u10Variable.addAttribute(CF_STANDARD_NAME, "eastward_wind");
        u10Variable.addAttribute(CF_LONG_NAME, "10 metre U wind component");
        u10Variable.addAttribute(CF_UNITS_NAME, "m s**-1");
        u10Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        u10Variable.addAttribute("source", "GRIB data");
        variables.add(u10Variable);

        final TemplateVariable v10Variable = new TemplateVariable(sensorExtractConfiguration.getAn_V10_name(), "V10", DataType.FLOAT, extractDimensions);
        v10Variable.addAttribute(CF_STANDARD_NAME, "northward_wind");
        v10Variable.addAttribute(CF_LONG_NAME, "10 metre V wind component");
        v10Variable.addAttribute(CF_UNITS_NAME, "m s**-1");
        v10Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        v10Variable.addAttribute("source", "GRIB data");
        variables.add(v10Variable);

        final TemplateVariable mslVariable = new TemplateVariable(sensorExtractConfiguration.getAn_MSL_name(), "MSL", DataType.FLOAT, extractDimensions);
        mslVariable.addAttribute(CF_STANDARD_NAME, "air_pressure_at_sea_level");
        mslVariable.addAttribute(CF_LONG_NAME, "Mean sea-level pressure");
        mslVariable.addAttribute(CF_UNITS_NAME, "Pa");
        mslVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        mslVariable.addAttribute("source", "GRIB data");
        variables.add(mslVariable);

        final TemplateVariable t2Variable = new TemplateVariable(sensorExtractConfiguration.getAn_T2_name(), "T2", DataType.FLOAT, extractDimensions);
        t2Variable.addAttribute(CF_STANDARD_NAME, "air_temperature");
        t2Variable.addAttribute(CF_LONG_NAME, "2 metre temperature");
        t2Variable.addAttribute(CF_UNITS_NAME, "K");
        t2Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        t2Variable.addAttribute("source", "GRIB data");
        variables.add(t2Variable);

        final TemplateVariable d2Variable = new TemplateVariable(sensorExtractConfiguration.getAn_D2_name(), "D2", DataType.FLOAT, extractDimensions);
        d2Variable.addAttribute(CF_STANDARD_NAME, "dew_point_temperature");
        d2Variable.addAttribute(CF_LONG_NAME, "2 metre dewpoint temperature");
        d2Variable.addAttribute(CF_UNITS_NAME, "K");
        d2Variable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        d2Variable.addAttribute("source", "GRIB data");
        variables.add(d2Variable);

        final TemplateVariable tpVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TP_name(), "TP", DataType.FLOAT, extractDimensions);
        tpVariable.addAttribute(CF_LONG_NAME, "Total precipitation");
        tpVariable.addAttribute(CF_UNITS_NAME, "m");
        tpVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        tpVariable.addAttribute("source", "GRIB data");
        variables.add(tpVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(sensorExtractConfiguration.getAn_CLWC_name(), "CLWC", DataType.FLOAT, profileDimensions);
        clwcVariable.addAttribute(CF_STANDARD_NAME, "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute(CF_LONG_NAME, "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        clwcVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0e33);
        clwcVariable.addAttribute("source", "GRIB data");
        variables.add(clwcVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TCWV_name(), "TCWV", DataType.FLOAT, extractDimensions);
        totalColumnWaterVapourVariable.addAttribute(CF_STANDARD_NAME, "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute(CF_LONG_NAME, "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute(CF_UNITS_NAME, "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable asnVariable = new TemplateVariable(sensorExtractConfiguration.getAn_ASN_name(), "ASN", DataType.FLOAT, extractDimensions);
        asnVariable.addAttribute(CF_LONG_NAME, "Snow albedo");
        asnVariable.addAttribute(CF_UNITS_NAME, "(0 - 1)");
        asnVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        asnVariable.addAttribute("source", "GRIB data");
        variables.add(asnVariable);

        final TemplateVariable tccVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TCC_name(), "TCC", DataType.FLOAT, extractDimensions);
        tccVariable.addAttribute(CF_STANDARD_NAME, "cloud_area_fraction");
        tccVariable.addAttribute(CF_LONG_NAME, "Total cloud cover");
        tccVariable.addAttribute(CF_UNITS_NAME, "(0 - 1)");
        tccVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        tccVariable.addAttribute("source", "GRIB data");
        variables.add(tccVariable);

        final TemplateVariable alVariable = new TemplateVariable(sensorExtractConfiguration.getAn_AL_name(), "AL", DataType.FLOAT, extractDimensions);
        alVariable.addAttribute(CF_STANDARD_NAME, "surface_albedo");
        alVariable.addAttribute(CF_LONG_NAME, "Albedo");
        alVariable.addAttribute(CF_UNITS_NAME, "(0 - 1)");
        alVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        alVariable.addAttribute("source", "GRIB data");
        variables.add(alVariable);

        final TemplateVariable sktVariable = new TemplateVariable(sensorExtractConfiguration.getAn_SKT_name(), "SKT", DataType.FLOAT, extractDimensions);
        sktVariable.addAttribute(CF_LONG_NAME, "Skin temperature");
        sktVariable.addAttribute(CF_UNITS_NAME, "K");
        sktVariable.addAttribute(CF_FILL_VALUE_NAME, 2.0E20);
        sktVariable.addAttribute("source", "GRIB data");
        variables.add(sktVariable);

        final TemplateVariable lnspVariable = new TemplateVariable(sensorExtractConfiguration.getAn_LNSP_name(), "LNSP", DataType.FLOAT, extractDimensions);
        lnspVariable.addAttribute(CF_LONG_NAME, "Logarithm of surface pressure");
        lnspVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0E33);
        lnspVariable.addAttribute("source", "GRIB data");
        variables.add(lnspVariable);

        final TemplateVariable temperatureProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_T_name(), "T", DataType.FLOAT, profileDimensions);
        temperatureProfileVariable.addAttribute(CF_LONG_NAME, "Temperature");
        temperatureProfileVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0E33);
        temperatureProfileVariable.addAttribute(CF_UNITS_NAME, "K");
        temperatureProfileVariable.addAttribute("source", "GRIB data");
        variables.add(temperatureProfileVariable);

        final TemplateVariable wvProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_Q_name(), "Q", DataType.FLOAT, profileDimensions);
        wvProfileVariable.addAttribute(CF_LONG_NAME, "Specific humidity");
        wvProfileVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0E33);
        wvProfileVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        wvProfileVariable.addAttribute("source", "GRIB data");
        variables.add(wvProfileVariable);

        final TemplateVariable o3ProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_O3_name(), "O3", DataType.FLOAT, profileDimensions);
        o3ProfileVariable.addAttribute(CF_LONG_NAME, "Ozone mass mixing ratio");
        o3ProfileVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0E33);
        o3ProfileVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        o3ProfileVariable.addAttribute("source", "GRIB data");
        variables.add(o3ProfileVariable);

        final TemplateVariable ciwcProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_CIWC_name(), "CIWC", DataType.FLOAT, profileDimensions);
        ciwcProfileVariable.addAttribute(CF_LONG_NAME, "Cloud ice water content");
        ciwcProfileVariable.addAttribute(CF_FILL_VALUE_NAME, -9.0E33);
        ciwcProfileVariable.addAttribute(CF_UNITS_NAME, "kg kg**-1");
        ciwcProfileVariable.addAttribute("source", "GRIB data");
        variables.add(ciwcProfileVariable);

        return variables;
    }
}
