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

class TimeSeriesConfiguration {
    private int analysisSteps;
    private int forecastSteps;
    private String timeVariableName;
    private String longitudeVariableName;
    private String latitudeVariableName;
    private String an_CI_name;
    private String an_SSTK_name;
    private String fc_SSTK_name;
    private String an_U10_name;
    private String fc_U10_name;
    private String an_V10_name;
    private String fc_V10_name;
    private String fc_MSL_name;
    private String fc_T2_name;
    private String fc_D2_name;
    private String fc_TP_name;
    private String an_CLWC_name;
    private String fc_CLWC_name;
    private String an_TCWV_name;
    private String fc_TCWV_name;
    private String fc_SSHF_name;
    private String fc_SLHF_name;
    private String fc_BLH_name;
    private String fc_SSRD_name;
    private String fc_STRD_name;
    private String fc_SSR_name;
    private String fc_STR_name;
    private String fc_EWSS_name;
    private String fc_NSSS_name;
    private String fc_E_name;
    private String anCenterTimeName;
    private String fcCenterTimeName;

    TimeSeriesConfiguration() {
        analysisSteps = 17;
        forecastSteps = 33;

        an_CI_name = "matchup.nwp.an.sea_ice_fraction";
        an_SSTK_name = "matchup.nwp.an.sea_surface_temperature";
        fc_SSTK_name = "matchup.nwp.fc.sea_surface_temperature";
        an_U10_name = "matchup.nwp.an.10m_east_wind_component";
        fc_U10_name = "matchup.nwp.fc.10m_east_wind_component";
        an_V10_name = "matchup.nwp.an.10m_north_wind_component";
        fc_V10_name = "matchup.nwp.fc.10m_north_wind_component";
        fc_MSL_name = "matchup.nwp.fc.mean_sea_level_pressure";
        fc_T2_name = "matchup.nwp.fc.2m_temperature";
        fc_D2_name = "matchup.nwp.fc.2m_dew_point";
        fc_TP_name = "matchup.nwp.fc.total_precipitation";
        an_CLWC_name = "matchup.nwp.an.cloud_liquid_water_content";
        fc_CLWC_name = "matchup.nwp.fc.cloud_liquid_water_content";
        an_TCWV_name = "matchup.nwp.an.total_column_water_vapour";
        fc_TCWV_name = "matchup.nwp.fc.total_column_water_vapour";
        fc_SSHF_name = "matchup.nwp.fc.surface_sensible_heat_flux";
        fc_SLHF_name = "matchup.nwp.fc.surface_latent_heat_flux";
        fc_BLH_name = "matchup.nwp.fc.boundary_layer_height";
        fc_SSRD_name = "matchup.nwp.fc.downward_surface_solar_radiation";
        fc_STRD_name = "matchup.nwp.fc.downward_surface_thermal_radiation";
        fc_SSR_name = "matchup.nwp.fc.surface_solar_radiation";
        fc_STR_name = "matchup.nwp.fc.surface_thermal_radiation";
        fc_EWSS_name = "matchup.nwp.fc.turbulent_stress_east_component";
        fc_NSSS_name = "matchup.nwp.fc.turbulent_stress_north_component";
        fc_E_name = "matchup.nwp.fc.evaporation";

        anCenterTimeName = "matchup.nwp.an.t0";
        fcCenterTimeName = "matchup.nwp.fc.t0";
    }

    void setAnalysisSteps(int analysisSteps) {
        this.analysisSteps = analysisSteps;
    }

    int getAnalysisSteps() {
        return analysisSteps;
    }

    void setForecastSteps(int forecastSteps) {
        this.forecastSteps = forecastSteps;
    }

    int getForecastSteps() {
        return forecastSteps;
    }

    void setTimeVariableName(String timeVariableName) {
        this.timeVariableName = timeVariableName;
    }

    String getTimeVariableName() {
        return timeVariableName;
    }

    void setLongitudeVariableName(String longitudeVariableName) {
        this.longitudeVariableName = longitudeVariableName;
    }

    String getLongitudeVariableName() {
        return longitudeVariableName;
    }

    void setLatitudeVariableName(String latitudeVariableName) {
        this.latitudeVariableName = latitudeVariableName;
    }

    String getLatitudeVariableName() {
        return latitudeVariableName;
    }

    void setAn_CI_name(String an_CI_name) {
        this.an_CI_name = an_CI_name;
    }

    String getAn_CI_name() {
        return an_CI_name;
    }

    void setAn_SSTK_name(String an_SSTK_name) {
        this.an_SSTK_name = an_SSTK_name;
    }

    String getAn_SSTK_name() {
        return an_SSTK_name;
    }

    void setFc_SSTK_name(String fc_SSTK_name) {
        this.fc_SSTK_name = fc_SSTK_name;
    }

    String getFc_SSTK_name() {
        return fc_SSTK_name;
    }

    void setAn_U10_name(String an_U10_name) {
        this.an_U10_name = an_U10_name;
    }

    String getAn_U10_name() {
        return an_U10_name;
    }

    void setFc_U10_name(String fc_U10_name) {
        this.fc_U10_name = fc_U10_name;
    }

    String getFc_U10_name() {
        return fc_U10_name;
    }

    void setAn_V10_name(String an_V10_name) {
        this.an_V10_name = an_V10_name;
    }

    String getAn_V10_name() {
        return an_V10_name;
    }

    void setFc_V10_name(String fc_V10_name) {
        this.fc_V10_name = fc_V10_name;
    }

    String getFc_V10_name() {
        return fc_V10_name;
    }

    void setFc_MSL_name(String fc_MSL_name) {
        this.fc_MSL_name = fc_MSL_name;
    }

    String getFc_MSL_name() {
        return fc_MSL_name;
    }

    void setFc_T2_name(String fc_T2_name) {
        this.fc_T2_name = fc_T2_name;
    }

    String getFc_T2_name() {
        return fc_T2_name;
    }

    void setFc_D2_name(String fc_D2_name) {
        this.fc_D2_name = fc_D2_name;
    }

    String getFc_D2_name() {
        return fc_D2_name;
    }

    void setFc_TP_name(String fc_TP_name) {
        this.fc_TP_name = fc_TP_name;
    }

    String getFc_TP_name() {
        return fc_TP_name;
    }

    void setAn_CLWC_name(String an_CLWC_name) {
        this.an_CLWC_name = an_CLWC_name;
    }

    String getAn_CLWC_name() {
        return an_CLWC_name;
    }

    void setFc_CLWC_name(String fc_CLWC_name) {
        this.fc_CLWC_name = fc_CLWC_name;
    }

    String getFc_CLWC_name() {
        return fc_CLWC_name;
    }

    void setAn_TCWV_name(String an_TCWV_name) {
        this.an_TCWV_name = an_TCWV_name;
    }

    String getAn_TCWV_name() {
        return an_TCWV_name;
    }

    void setFc_TCWV_name(String fc_TCWV_name) {
        this.fc_TCWV_name = fc_TCWV_name;
    }

    String getFc_TCWV_name() {
        return fc_TCWV_name;
    }

    void setFc_SSHF_name(String fc_SSHF_name) {
        this.fc_SSHF_name = fc_SSHF_name;
    }

    String getFc_SSHF_name() {
        return fc_SSHF_name;
    }

    void setFc_SLHF_name(String fc_SLHF_name) {
        this.fc_SLHF_name = fc_SLHF_name;
    }

    String getFc_SLHF_name() {
        return fc_SLHF_name;
    }

    void setFc_BLH_name(String fc_BLH_name) {
        this.fc_BLH_name = fc_BLH_name;
    }

    String getFc_BLH_name() {
        return fc_BLH_name;
    }

    void setFc_SSRD_name(String fc_SSRD_name) {
        this.fc_SSRD_name = fc_SSRD_name;
    }

    String getFc_SSRD_name() {
        return fc_SSRD_name;
    }

    void setFc_STRD_name(String fc_STRD_name) {
        this.fc_STRD_name = fc_STRD_name;
    }

    String getFc_STRD_name() {
        return fc_STRD_name;
    }

    void setFc_SSR_name(String fc_SSR_name) {
        this.fc_SSR_name = fc_SSR_name;
    }

    String getFc_SSR_name() {
        return fc_SSR_name;
    }

    void setFc_STR_name(String fc_STR_name) {
        this.fc_STR_name = fc_STR_name;
    }

    String getFc_STR_name() {
        return fc_STR_name;
    }

    void setFc_EWSS_name(String fc_EWSS_name) {
        this.fc_EWSS_name = fc_EWSS_name;
    }

    String getFc_EWSS_name() {
        return fc_EWSS_name;
    }

    void setFc_NSSS_name(String fc_NSSS_name) {
        this.fc_NSSS_name = fc_NSSS_name;
    }

    String getFc_NSSS_name() {
        return fc_NSSS_name;
    }

    void setFc_E_name(String fc_E_name) {
        this.fc_E_name = fc_E_name;
    }

    String getFc_E_name() {
        return fc_E_name;
    }

    void setAnCenterTimeName(String anCenterTimeName) {
        this.anCenterTimeName = anCenterTimeName;
    }

    String getAnCenterTimeName() {
        return anCenterTimeName;
    }

    void setFcCenterTimeName(String fcCenterTimeName) {
        this.fcCenterTimeName = fcCenterTimeName;
    }

    String getFcCenterTimeName() {
        return fcCenterTimeName;
    }
}
