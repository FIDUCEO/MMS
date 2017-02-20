/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;


import ucar.nc2.NetcdfFile;

import java.io.File;

class Configuration {

    private boolean deleteOnExit;
    private String CDOHome;
    private int analysisSteps;
    private int forecastSteps;
    private String NWPAuxDir;
    private String timeVariableName;
    private String anSeaIceFractionName;
    private String anSSTName;
    private String anEastWindName;
    private String anNorthWindName;
    private String fcSSTName;
    private String fcSurfSensibleHeatFluxName;
    private String fcSurfLatentHeatFluxName;
    private String fcMeanSeaLevelPressureName;
    private String fcBoundaryLayerHeightName;
    private String fc10mEastWindName;
    private String fc10mNorthWindName;
    private String fc2mTemperatureName;
    private String fc2mDewPointName;
    private String fcDownSurfSolarRadiationName;
    private String fcDownSurfThermalRadiationName;
    private String fcSurfSolarRadiationName;
    private String fcSurfThermalRadiationName;
    private String fcTurbStressEastName;
    private String fcTurbStressNorthName;
    private String fcEvaporationName;
    private String fcTotalPrecipName;
    private String longitudeVariableName;
    private String latitudeVariableName;

    Configuration() {
        deleteOnExit = true;
        analysisSteps = 17;
        forecastSteps = 33;

        anSeaIceFractionName = NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_ice_fraction");
        anSSTName = NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_surface_temperature");
        anEastWindName = NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_east_wind_component");
        anNorthWindName = NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_north_wind_component");
        fcSSTName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.sea_surface_temperature");
        fcSurfSensibleHeatFluxName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_sensible_heat_flux");
        fcSurfLatentHeatFluxName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_latent_heat_flux");
        fcMeanSeaLevelPressureName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.mean_sea_level_pressure");
        fcBoundaryLayerHeightName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.boundary_layer_height");
        fc10mEastWindName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_east_wind_component");
        fc10mNorthWindName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_north_wind_component");
        fc2mTemperatureName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_temperature");
        fc2mDewPointName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_dew_point");
        fcDownSurfSolarRadiationName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_solar_radiation");
        fcDownSurfThermalRadiationName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_thermal_radiation");
        fcSurfSolarRadiationName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_solar_radiation");
        fcSurfThermalRadiationName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_thermal_radiation");
        fcTurbStressEastName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_east_component");
        fcTurbStressNorthName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_north_component");
        fcEvaporationName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.evaporation");
        fcTotalPrecipName = NetcdfFile.makeValidCDLName("matchup.nwp.fc.total_precipitation");
    }

    void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    void setCDOHome(String CDOHome) {
        this.CDOHome = CDOHome;
    }

    String getCDOHome() {
        return CDOHome;
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

    void setNWPAuxDir(String NWPAuxDir) {
        this.NWPAuxDir = NWPAuxDir;
    }

    String getNWPAuxDir() {
        return NWPAuxDir;
    }

    void setTimeVariableName(String timeVariableName) {
        this.timeVariableName = NetcdfFile.makeValidCDLName(timeVariableName);
    }

    String getTimeVariableName() {
        return timeVariableName;
    }

    void setAnSeaIceFractionName(String anSeaIceFractionName) {
        this.anSeaIceFractionName = NetcdfFile.makeValidCDLName(anSeaIceFractionName);
    }

    String getAnSeaIceFractionName() {
        return anSeaIceFractionName;
    }

    void setAnSSTName(String anSSTName) {
        this.anSSTName = NetcdfFile.makeValidCDLName(anSSTName);
    }

    String getAnSSTName() {
        return anSSTName;
    }

    void setAnEastWindName(String anEastWindName) {
        this.anEastWindName = NetcdfFile.makeValidCDLName(anEastWindName);
    }

    String getAnEastWindName() {
        return anEastWindName;
    }

    void setAnNorthWindName(String anNorthWindName) {
        this.anNorthWindName = NetcdfFile.makeValidCDLName(anNorthWindName);
    }

    String getAnNorthWindName() {
        return anNorthWindName;
    }

    void setFcSSTName(String fcSSTName) {
        this.fcSSTName = NetcdfFile.makeValidCDLName(fcSSTName);
    }

    String getFcSSTName() {
        return fcSSTName;
    }

    void setFcSurfSensibleHeatFluxName(String fcSurfSensibleHeatFluxName) {
        this.fcSurfSensibleHeatFluxName = NetcdfFile.makeValidCDLName(fcSurfSensibleHeatFluxName);
    }

    String getFcSurfSensibleHeatFluxName() {
        return fcSurfSensibleHeatFluxName;
    }

    void setFcSurfLatentHeatFluxName(String fcSurfLatentHeatFluxName) {
        this.fcSurfLatentHeatFluxName = NetcdfFile.makeValidCDLName(fcSurfLatentHeatFluxName);
    }

    String getFcSurfLatentHeatFluxName() {
        return fcSurfLatentHeatFluxName;
    }

    void setFcMeanSeaLevelPressureName(String fcMeanSeaLevelPressureName) {
        this.fcMeanSeaLevelPressureName = NetcdfFile.makeValidCDLName(fcMeanSeaLevelPressureName);
    }

    String getFcMeanSeaLevelPressureName() {
        return fcMeanSeaLevelPressureName;
    }

    void setFcBoundaryLayerHeightName(String fcBoundaryLayerHeightName) {
        this.fcBoundaryLayerHeightName = NetcdfFile.makeValidCDLName(fcBoundaryLayerHeightName);
    }

    String getFcBoundaryLayerHeightName() {
        return fcBoundaryLayerHeightName;
    }

    void setFc10mEastWindName(String fc10mEastWindName) {
        this.fc10mEastWindName = NetcdfFile.makeValidCDLName(fc10mEastWindName);
    }

    String getFc10mEastWindName() {
        return fc10mEastWindName;
    }

    void setFc10mNorthWindName(String fc10mNorthWindName) {
        this.fc10mNorthWindName = NetcdfFile.makeValidCDLName(fc10mNorthWindName);
    }

    String getFc10mNorthWindName() {
        return fc10mNorthWindName;
    }

    void setFc2mTemperatureName(String fc2mTemperatureName) {
        this.fc2mTemperatureName = NetcdfFile.makeValidCDLName(fc2mTemperatureName);
    }

    String getFc2mTemperatureName() {
        return fc2mTemperatureName;
    }

    void setFc2mDewPointName(String fc2mDewPointName) {
        this.fc2mDewPointName = NetcdfFile.makeValidCDLName(fc2mDewPointName);
    }

    String getFc2mDewPointName() {
        return fc2mDewPointName;
    }

    void setFcDownSurfSolarRadiationName(String fcDownSurfSolarRadiationName) {
        this.fcDownSurfSolarRadiationName = NetcdfFile.makeValidCDLName(fcDownSurfSolarRadiationName);
    }

    String getFcDownSurfSolarRadiationName() {
        return fcDownSurfSolarRadiationName;
    }

    void setFcDownSurfThermalRadiationName(String fcDownSurfThermalRadiationName) {
        this.fcDownSurfThermalRadiationName = NetcdfFile.makeValidCDLName(fcDownSurfThermalRadiationName);
    }

    String getFcDownSurfThermalRadiationName() {
        return fcDownSurfThermalRadiationName;
    }

    void setFcSurfSolarRadiationName(String fcSurfSolarRadiationName) {
        this.fcSurfSolarRadiationName = NetcdfFile.makeValidCDLName(fcSurfSolarRadiationName);
    }

    String getFcSurfSolarRadiationName() {
        return fcSurfSolarRadiationName;
    }

    void setFcSurfThermalRadiationName(String fcSurfThermalRadiationName) {
        this.fcSurfThermalRadiationName = NetcdfFile.makeValidCDLName(fcSurfThermalRadiationName);
    }

    String getFcSurfThermalRadiationName() {
        return fcSurfThermalRadiationName;
    }

    void setFcTurbStressEastName(String fcTurbStressEastName) {
        this.fcTurbStressEastName = NetcdfFile.makeValidCDLName(fcTurbStressEastName);
    }

    String getFcTurbStressEastName() {
        return fcTurbStressEastName;
    }

    void setFcTurbStressNorthName(String fcTurbStressNorthName) {
        this.fcTurbStressNorthName = NetcdfFile.makeValidCDLName(fcTurbStressNorthName);
    }

    String getFcTurbStressNorthName() {
        return fcTurbStressNorthName;
    }

    void setFcEvaporationName(String fcEvaporationName) {
        this.fcEvaporationName = NetcdfFile.makeValidCDLName(fcEvaporationName);
    }

    String getFcEvaporationName() {
        return fcEvaporationName;
    }

    void setFcTotalPrecipName(String fcTotalPrecipName) {
        this.fcTotalPrecipName = NetcdfFile.makeValidCDLName(fcTotalPrecipName);
    }

    String getFcTotalPrecipName() {
        return fcTotalPrecipName;
    }

    void setLongitudeVariableName(String longitudeVariableName) {
        this.longitudeVariableName = NetcdfFile.makeValidCDLName(longitudeVariableName);
    }

    String getLongitudeVariableName() {
        return longitudeVariableName;
    }

    void setLatitudeVariableName(String latitudeVariableName) {
        this.latitudeVariableName = NetcdfFile.makeValidCDLName(latitudeVariableName);
    }

    String getLatitudeVariableName() {
        return latitudeVariableName;
    }

    boolean verify() {
        final File cdoDir = new File(CDOHome);
        if (!cdoDir.isDirectory()) {
            throw new RuntimeException("cdo executable directory does not exist");
        }

        final File nwpDir = new File(NWPAuxDir);
        if (!nwpDir.isDirectory()) {
            throw new RuntimeException("era interim aux data directory does not exist");
        }
        return true;
    }
}
