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
}
