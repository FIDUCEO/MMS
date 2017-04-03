
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
        seaIceFractionVariable.addAttribute("long_name", "Sea-ice cover");
        seaIceFractionVariable.addAttribute("_FillValue", 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(timeSeriesConfiguration.getAn_SSTK_name(), "SSTK", DataType.FLOAT, anDimensions);
        sstVariable.addAttribute("long_name", "Sea surface temperature");
        sstVariable.addAttribute("units", "K");
        sstVariable.addAttribute("_FillValue", 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable eastWindVariable = new TemplateVariable(timeSeriesConfiguration.getAn_U10_name(), "U10", DataType.FLOAT, anDimensions);
        eastWindVariable.addAttribute("standard_name", "eastward_wind");
        eastWindVariable.addAttribute("long_name", "10 metre U wind component");
        eastWindVariable.addAttribute("units", "m s**-1");
        eastWindVariable.addAttribute("_FillValue", 2.0E20);
        eastWindVariable.addAttribute("source", "GRIB data");
        variables.add(eastWindVariable);

        final TemplateVariable northWindVariable = new TemplateVariable(timeSeriesConfiguration.getAn_V10_name(), "V10", DataType.FLOAT, anDimensions);
        northWindVariable.addAttribute("standard_name", "northward_wind");
        northWindVariable.addAttribute("long_name", "10 metre V wind component");
        northWindVariable.addAttribute("units", "m s**-1");
        northWindVariable.addAttribute("_FillValue", 2.0E20);
        northWindVariable.addAttribute("source", "GRIB data");
        variables.add(northWindVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(timeSeriesConfiguration.getAn_TCWV_name(), "TCWV", DataType.FLOAT, anDimensions);
        totalColumnWaterVapourVariable.addAttribute("standard_name", "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute("long_name", "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute("units", "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute("_FillValue", 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(timeSeriesConfiguration.getAn_CLWC_name(), Constants.CLWC_NAME, DataType.FLOAT, anDimensions);
        clwcVariable.addAttribute("standard_name", "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute("long_name", "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute("units", "kg kg**-1");
        clwcVariable.addAttribute("_FillValue", -9.0e33);
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
        evaporationVariable.addAttribute("standard_name", "lwe_thickness_of_water_evaporation_amount");
        evaporationVariable.addAttribute("long_name", "Evaporation");
        evaporationVariable.addAttribute("units", "m of water");
        evaporationVariable.addAttribute("_FillValue", 2.0E20);
        evaporationVariable.addAttribute("source", "GRIB data");
        variables.add(evaporationVariable);

        final TemplateVariable sshfVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSHF_name(), "SSHF", DataType.FLOAT, fcDimensions);
        sshfVariable.addAttribute("standard_name", "surface_upward_sensible_heat_flux");
        sshfVariable.addAttribute("long_name", "Surface sensible heat flux");
        sshfVariable.addAttribute("units", "W m**-2 s");
        sshfVariable.addAttribute("_FillValue", 2.0E20);
        sshfVariable.addAttribute("source", "GRIB data");
        variables.add(sshfVariable);

        final TemplateVariable slhfVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SLHF_name(), "SLHF", DataType.FLOAT, fcDimensions);
        slhfVariable.addAttribute("standard_name", "surface_upward_latent_heat_flux");
        slhfVariable.addAttribute("long_name", "Surface latent heat flux");
        slhfVariable.addAttribute("units", "W m**-2 s");
        slhfVariable.addAttribute("_FillValue", 2.0E20);
        slhfVariable.addAttribute("source", "GRIB data");
        variables.add(slhfVariable);

        final TemplateVariable ssrdVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSRD_name(), "SSRD", DataType.FLOAT, fcDimensions);
        ssrdVariable.addAttribute("standard_name", "surface_downwelling_shortwave_flux_in_air");
        ssrdVariable.addAttribute("long_name", "Surface solar radiation downwards");
        ssrdVariable.addAttribute("units", "W m**-2 s");
        ssrdVariable.addAttribute("_FillValue", 2.0E20);
        ssrdVariable.addAttribute("source", "GRIB data");
        variables.add(ssrdVariable);

        final TemplateVariable strdVariable = new TemplateVariable(timeSeriesConfiguration.getFc_STRD_name(), "STRD", DataType.FLOAT, fcDimensions);
        strdVariable.addAttribute("long_name", "Surface thermal radiation downwards");
        strdVariable.addAttribute("units", "W m**-2 s");
        strdVariable.addAttribute("_FillValue", 2.0E20);
        strdVariable.addAttribute("source", "GRIB data");
        variables.add(strdVariable);

        final TemplateVariable ssrVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSR_name(), "SSR", DataType.FLOAT, fcDimensions);
        ssrVariable.addAttribute("standard_name", "surface_net_upward_longwave_flux");
        ssrVariable.addAttribute("long_name", "Surface solar radiation");
        ssrVariable.addAttribute("units", "W m**-2 s");
        ssrVariable.addAttribute("_FillValue", 2.0E20);
        ssrVariable.addAttribute("source", "GRIB data");
        variables.add(ssrVariable);

        final TemplateVariable strVariable = new TemplateVariable(timeSeriesConfiguration.getFc_STR_name(), "STR", DataType.FLOAT, fcDimensions);
        strVariable.addAttribute("standard_name", "surface_net_upward_shortwave_flux");
        strVariable.addAttribute("long_name", "Surface thermal radiation");
        strVariable.addAttribute("units", "W m**-2 s");
        strVariable.addAttribute("_FillValue", 2.0E20);
        strVariable.addAttribute("source", "GRIB data");
        variables.add(strVariable);

        final TemplateVariable ewssVariable = new TemplateVariable(timeSeriesConfiguration.getFc_EWSS_name(), "EWSS", DataType.FLOAT, fcDimensions);
        ewssVariable.addAttribute("standard_name", "surface_downward_eastward_stress");
        ewssVariable.addAttribute("long_name", "East-west surface stress");
        ewssVariable.addAttribute("units", "N m**-2 s");
        ewssVariable.addAttribute("_FillValue", 2.0E20);
        ewssVariable.addAttribute("source", "GRIB data");
        variables.add(ewssVariable);

        final TemplateVariable nsssVariable = new TemplateVariable(timeSeriesConfiguration.getFc_NSSS_name(), "NSSS", DataType.FLOAT, fcDimensions);
        nsssVariable.addAttribute("standard_name", "surface_downward_northward_stress");
        nsssVariable.addAttribute("long_name", "North-south surface stress");
        nsssVariable.addAttribute("units", "N m**-2 s");
        nsssVariable.addAttribute("_FillValue", 2.0E20);
        nsssVariable.addAttribute("source", "GRIB data");
        variables.add(nsssVariable);

        final TemplateVariable tpVariable = new TemplateVariable(timeSeriesConfiguration.getFc_TP_name(), "TP", DataType.FLOAT, fcDimensions);
        tpVariable.addAttribute("long_name", "Total precipitation");
        tpVariable.addAttribute("units", "m");
        tpVariable.addAttribute("_FillValue", 2.0E20);
        tpVariable.addAttribute("source", "GRIB data");
        variables.add(tpVariable);

        final TemplateVariable u10Variable = new TemplateVariable(timeSeriesConfiguration.getFc_U10_name(), "U10", DataType.FLOAT, fcDimensions);
        u10Variable.addAttribute("standard_name", "eastward_wind");
        u10Variable.addAttribute("long_name", "10 metre U wind component");
        u10Variable.addAttribute("units", "m s**-1");
        u10Variable.addAttribute("_FillValue", 2.0E20);
        u10Variable.addAttribute("source", "GRIB data");
        variables.add(u10Variable);

        final TemplateVariable v10Variable = new TemplateVariable(timeSeriesConfiguration.getFc_V10_name(), "V10", DataType.FLOAT, fcDimensions);
        v10Variable.addAttribute("standard_name", "northward_wind");
        v10Variable.addAttribute("long_name", "10 metre V wind component");
        v10Variable.addAttribute("units", "m s**-1");
        v10Variable.addAttribute("_FillValue", 2.0E20);
        v10Variable.addAttribute("source", "GRIB data");
        variables.add(v10Variable);

        final TemplateVariable d2Variable = new TemplateVariable(timeSeriesConfiguration.getFc_D2_name(), "D2", DataType.FLOAT, fcDimensions);
        d2Variable.addAttribute("standard_name", "dew_point_temperature");
        d2Variable.addAttribute("long_name", "2 metre dewpoint temperature");
        d2Variable.addAttribute("units", "K");
        d2Variable.addAttribute("_FillValue", 2.0E20);
        d2Variable.addAttribute("source", "GRIB data");
        variables.add(d2Variable);

        final TemplateVariable t2Variable = new TemplateVariable(timeSeriesConfiguration.getFc_T2_name(), "T2", DataType.FLOAT, fcDimensions);
        t2Variable.addAttribute("standard_name", "air_temperature");
        t2Variable.addAttribute("long_name", "2 metre temperature");
        t2Variable.addAttribute("units", "K");
        t2Variable.addAttribute("_FillValue", 2.0E20);
        t2Variable.addAttribute("source", "GRIB data");
        variables.add(t2Variable);

        final TemplateVariable blhVariable = new TemplateVariable(timeSeriesConfiguration.getFc_BLH_name(), "BLH", DataType.FLOAT, fcDimensions);
        blhVariable.addAttribute("long_name", "Boundary layer height");
        blhVariable.addAttribute("units", "m");
        blhVariable.addAttribute("_FillValue", 2.0E20);
        blhVariable.addAttribute("source", "GRIB data");
        variables.add(blhVariable);

        final TemplateVariable mslVariable = new TemplateVariable(timeSeriesConfiguration.getFc_MSL_name(), "MSL", DataType.FLOAT, fcDimensions);
        mslVariable.addAttribute("standard_name", "air_pressure_at_sea_level");
        mslVariable.addAttribute("long_name", "Mean sea-level pressure");
        mslVariable.addAttribute("units", "Pa");
        mslVariable.addAttribute("_FillValue", 2.0E20);
        mslVariable.addAttribute("source", "GRIB data");
        variables.add(mslVariable);

        final TemplateVariable sstkVariable = new TemplateVariable(timeSeriesConfiguration.getFc_SSTK_name(), "SSTK", DataType.FLOAT, fcDimensions);
        sstkVariable.addAttribute("long_name", "Sea surface temperature");
        sstkVariable.addAttribute("units", "K");
        sstkVariable.addAttribute("_FillValue", 2.0E20);
        sstkVariable.addAttribute("source", "GRIB data");
        variables.add(sstkVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(timeSeriesConfiguration.getFc_TCWV_name(), "TCWV", DataType.FLOAT, fcDimensions);
        totalColumnWaterVapourVariable.addAttribute("standard_name", "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute("long_name", "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute("units", "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute("_FillValue", 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(timeSeriesConfiguration.getFc_CLWC_name(), Constants.CLWC_NAME, DataType.FLOAT, fcDimensions);
        clwcVariable.addAttribute("standard_name", "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute("long_name", "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute("units", "kg kg**-1");
        clwcVariable.addAttribute("_FillValue", -9.0e33);
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
        seaIceFractionVariable.addAttribute("long_name", "Sea-ice cover");
        seaIceFractionVariable.addAttribute("_FillValue", 2.0E20);
        seaIceFractionVariable.addAttribute("source", "GRIB data");
        variables.add(seaIceFractionVariable);

        final TemplateVariable sstVariable = new TemplateVariable(sensorExtractConfiguration.getAn_SSTK_name(), "SSTK", DataType.FLOAT, extractDimensions);
        sstVariable.addAttribute("long_name", "Sea surface temperature");
        sstVariable.addAttribute("units", "K");
        sstVariable.addAttribute("_FillValue", 2.0E20);
        sstVariable.addAttribute("source", "GRIB data");
        variables.add(sstVariable);

        final TemplateVariable u10Variable = new TemplateVariable(sensorExtractConfiguration.getAn_U10_name(), "U10", DataType.FLOAT, extractDimensions);
        u10Variable.addAttribute("standard_name", "eastward_wind");
        u10Variable.addAttribute("long_name", "10 metre U wind component");
        u10Variable.addAttribute("units", "m s**-1");
        u10Variable.addAttribute("_FillValue", 2.0E20);
        u10Variable.addAttribute("source", "GRIB data");
        variables.add(u10Variable);

        final TemplateVariable v10Variable = new TemplateVariable(sensorExtractConfiguration.getAn_V10_name(), "V10", DataType.FLOAT, extractDimensions);
        v10Variable.addAttribute("standard_name", "northward_wind");
        v10Variable.addAttribute("long_name", "10 metre V wind component");
        v10Variable.addAttribute("units", "m s**-1");
        v10Variable.addAttribute("_FillValue", 2.0E20);
        v10Variable.addAttribute("source", "GRIB data");
        variables.add(v10Variable);

        final TemplateVariable mslVariable = new TemplateVariable(sensorExtractConfiguration.getAn_MSL_name(), "MSL", DataType.FLOAT, extractDimensions);
        mslVariable.addAttribute("standard_name", "air_pressure_at_sea_level");
        mslVariable.addAttribute("long_name", "Mean sea-level pressure");
        mslVariable.addAttribute("units", "Pa");
        mslVariable.addAttribute("_FillValue", 2.0E20);
        mslVariable.addAttribute("source", "GRIB data");
        variables.add(mslVariable);

        final TemplateVariable t2Variable = new TemplateVariable(sensorExtractConfiguration.getAn_T2_name(), "T2", DataType.FLOAT, extractDimensions);
        t2Variable.addAttribute("standard_name", "air_temperature");
        t2Variable.addAttribute("long_name", "2 metre temperature");
        t2Variable.addAttribute("units", "K");
        t2Variable.addAttribute("_FillValue", 2.0E20);
        t2Variable.addAttribute("source", "GRIB data");
        variables.add(t2Variable);

        final TemplateVariable d2Variable = new TemplateVariable(sensorExtractConfiguration.getAn_D2_name(), "D2", DataType.FLOAT, extractDimensions);
        d2Variable.addAttribute("standard_name", "dew_point_temperature");
        d2Variable.addAttribute("long_name", "2 metre dewpoint temperature");
        d2Variable.addAttribute("units", "K");
        d2Variable.addAttribute("_FillValue", 2.0E20);
        d2Variable.addAttribute("source", "GRIB data");
        variables.add(d2Variable);

        final TemplateVariable tpVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TP_name(), "TP", DataType.FLOAT, extractDimensions);
        tpVariable.addAttribute("long_name", "Total precipitation");
        tpVariable.addAttribute("units", "m");
        tpVariable.addAttribute("_FillValue", 2.0E20);
        tpVariable.addAttribute("source", "GRIB data");
        variables.add(tpVariable);

        final TemplateVariable clwcVariable = new TemplateVariable(sensorExtractConfiguration.getAn_CLWC_name(), Constants.CLWC_NAME, DataType.FLOAT, profileDimensions);
        clwcVariable.addAttribute("standard_name", "specific_cloud_liquid_water_content");
        clwcVariable.addAttribute("long_name", "Grid-box mean specific cloud liquid water content (mass of condensate / mass of moist air)");
        clwcVariable.addAttribute("units", "kg kg**-1");
        clwcVariable.addAttribute("_FillValue", -9.0e33);
        clwcVariable.addAttribute("source", "GRIB data");
        variables.add(clwcVariable);

        final TemplateVariable totalColumnWaterVapourVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TCWV_name(), "TCWV", DataType.FLOAT, extractDimensions);
        totalColumnWaterVapourVariable.addAttribute("standard_name", "lwe_thickness_of_atmosphere_water_vapour_content");
        totalColumnWaterVapourVariable.addAttribute("long_name", "Total column water vapour");
        totalColumnWaterVapourVariable.addAttribute("units", "kg m**-2");
        totalColumnWaterVapourVariable.addAttribute("_FillValue", 2.0E20);
        totalColumnWaterVapourVariable.addAttribute("source", "GRIB data");
        variables.add(totalColumnWaterVapourVariable);

        final TemplateVariable asnVariable = new TemplateVariable(sensorExtractConfiguration.getAn_ASN_name(), "ASN", DataType.FLOAT, extractDimensions);
        asnVariable.addAttribute("long_name", "Snow albedo");
        asnVariable.addAttribute("units", "(0 - 1)");
        asnVariable.addAttribute("_FillValue", 2.0E20);
        asnVariable.addAttribute("source", "GRIB data");
        variables.add(asnVariable);

        final TemplateVariable tccVariable = new TemplateVariable(sensorExtractConfiguration.getAn_TCC_name(), "TCC", DataType.FLOAT, extractDimensions);
        tccVariable.addAttribute("standard_name", "cloud_area_fraction");
        tccVariable.addAttribute("long_name", "Total cloud cover");
        tccVariable.addAttribute("units", "(0 - 1)");
        tccVariable.addAttribute("_FillValue", 2.0E20);
        tccVariable.addAttribute("source", "GRIB data");
        variables.add(tccVariable);

        final TemplateVariable alVariable = new TemplateVariable(sensorExtractConfiguration.getAn_AL_name(), "AL", DataType.FLOAT, extractDimensions);
        alVariable.addAttribute("standard_name", "surface_albedo");
        alVariable.addAttribute("long_name", "Albedo");
        alVariable.addAttribute("units", "(0 - 1)");
        alVariable.addAttribute("_FillValue", 2.0E20);
        alVariable.addAttribute("source", "GRIB data");
        variables.add(alVariable);

        final TemplateVariable sktVariable = new TemplateVariable(sensorExtractConfiguration.getAn_SKT_name(), "SKT", DataType.FLOAT, extractDimensions);
        sktVariable.addAttribute("long_name", "Skin temperature");
        sktVariable.addAttribute("units", "K");
        sktVariable.addAttribute("_FillValue", 2.0E20);
        sktVariable.addAttribute("source", "GRIB data");
        variables.add(sktVariable);

        final TemplateVariable lnspVariable = new TemplateVariable(sensorExtractConfiguration.getAn_LNSP_name(), "LNSP", DataType.FLOAT, extractDimensions);
        lnspVariable.addAttribute("long_name", "Logarithm of surface pressure");
        lnspVariable.addAttribute("_FillValue", -9.0E33);
        lnspVariable.addAttribute("source", "GRIB data");
        variables.add(lnspVariable);

        final TemplateVariable temperatureProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_T_name(), "T", DataType.FLOAT, profileDimensions);
        temperatureProfileVariable.addAttribute("long_name", "Temperature");
        temperatureProfileVariable.addAttribute("_FillValue", -9.0E33);
        temperatureProfileVariable.addAttribute("units", "K");
        temperatureProfileVariable.addAttribute("source", "GRIB data");
        variables.add(temperatureProfileVariable);

        final TemplateVariable wvProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_Q_name(), "Q", DataType.FLOAT, profileDimensions);
        wvProfileVariable.addAttribute("long_name", "Specific humidity");
        wvProfileVariable.addAttribute("_FillValue", -9.0E33);
        wvProfileVariable.addAttribute("units", "kg kg**-1");
        wvProfileVariable.addAttribute("source", "GRIB data");
        variables.add(wvProfileVariable);

        final TemplateVariable o3ProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_O3_name(), "O3", DataType.FLOAT, profileDimensions);
        o3ProfileVariable.addAttribute("long_name", "Ozone mass mixing ratio");
        o3ProfileVariable.addAttribute("_FillValue", -9.0E33);
        o3ProfileVariable.addAttribute("units", "kg kg**-1");
        o3ProfileVariable.addAttribute("source", "GRIB data");
        variables.add(o3ProfileVariable);

        final TemplateVariable ciwcProfileVariable = new TemplateVariable(sensorExtractConfiguration.getAn_CIWC_name(), "CIWC", DataType.FLOAT, profileDimensions);
        ciwcProfileVariable.addAttribute("long_name", "Cloud ice water content");
        ciwcProfileVariable.addAttribute("_FillValue", -9.0E33);
        ciwcProfileVariable.addAttribute("units", "kg kg**-1");
        ciwcProfileVariable.addAttribute("source", "GRIB data");
        variables.add(ciwcProfileVariable);

        return variables;
    }
}
