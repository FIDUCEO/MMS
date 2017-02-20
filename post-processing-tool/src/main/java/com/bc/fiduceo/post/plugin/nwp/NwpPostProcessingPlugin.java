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


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

/* The XML template for this post processing class looks like:

    <nwp>
        <!-- Directory hosting the CDO executables
        -->
        <cdo-home>/usr/local/bin/cdo</cdo-home>

        <!-- Defines the directory where the ERAInterim auxiliary files are located
        -->
        <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>

        <!-- Set whether to delete all temporary files after processing or not.
             Default value is: true
         -->
        <delete-on-exit>true</delete-on-exit>

        <!-- Defines the number of time steps around the matchup time for
             NWP analysis data (6 hr time resolution). Default is: 17.
        -->
        <analysis-steps>19</analysis-steps>

         <!-- Defines the number of time steps around the matchup time for
             NWP forecast data (3 hr time resolution). Default is: 33
        -->
        <forecast-steps>33</forecast-steps>

        <!-- Defines the name of the time variable to use. Time variables are expected to store data in
             seconds since 1970 format.
        -->
        <time-variable-name>acquisition-time</time-variable-name>

        <!-- Defines the name of the longitude variable to use. Data is expected in decimal degrees east.
        -->
        <longitude-variable-name>animal-sst_insitu.lon</longitude-variable-name>

        <!-- Defines the name of the latitude variable to use. Data is expected in decimal degrees east.
        -->
        <latitude-variable-name>animal-sst_insitu.lat</latitude-variable-name>

        <!-- Defines the name of the target variable for analysis sea-ice-fraction.
             Default: matchup.nwp.an.sea_ice_fraction
        -->
        <an-sea-ice-fraction-name>an_sea-ice-fraction</an-sea-ice-fraction-name>

        <!-- Defines the name of the target variable for analysis sea surface temperature.
             Default: matchup.nwp.an.sea_surface_temperature
        -->
        <an-sst-name>an_sea-surface-temperature</an-sst-name>

        <!-- Defines the name of the target variable for analysis 10m east wind component.
             Default: matchup.nwp.an.10m_east_wind_component
        -->
        <an-east-wind-name>an_sea-surface-temperature</an-east-wind-name>

        <!-- Defines the name of the target variable for analysis 10m north wind component.
             Default: matchup.nwp.an.10m_north_wind_component
        -->
        <an-north-wind-name>an_sea-surface-temperature</an-north-wind-name>

         <!-- Defines the name of the target variable for forecast sea surface temperature.
             Default: matchup.nwp.fc.sea_surface_temperature
        -->
        <fc-sst-name>fc_sea-surface-temperature</fc-sst-name>

        <!-- Defines the name of the target variable for forecast surface sensible heat flux.
             Default: matchup.nwp.fc.surface_sensible_heat_flux
        -->
        <fc-surf-sensible-heat-flux-name>matchup.nwp.fc.surface_sensible_heat_flux</fc-surf-sensible-heat-flux-name>

        <!-- Defines the name of the target variable for forecast latent sensible heat flux.
             Default: matchup.nwp.fc.surface_latent_heat_flux
        -->
        <fc-surf-latent-heat-flux-name>matchup.nwp.fc.surface_latent_heat_flux</fc-surf-latent-heat-flux-name>

        <!-- Defines the name of the target variable for forecast boundary layer height.
             Default: matchup.nwp.fc.boundary_layer_height
        -->
        <fc-surf-boundary-layer-height-name>matchup.nwp.fc.boundary_layer_height</fc-surf-boundary-layer-height-name>

        <!-- Defines the name of the target variable for forecast 10m east wind component.
             Default: matchup.nwp.fc.10m_east_wind_component
        -->
        <fc-10m-east-wind-name>matchup.nwp.fc.10m_east_wind_component</fc-10m-east-wind-name>

        <!-- Defines the name of the target variable for forecast 10m north wind component.
             Default: matchup.nwp.fc.10m_north_wind_component
        -->
        <fc-10m-north-wind-name>matchup.nwp.fc.10m_north_wind_component</fc-10m-north-wind-name>

        <!-- Defines the name of the target variable for forecast 2m temperature.
             Default: matchup.nwp.fc.2m_temperature
        -->
        <fc-2m-temperature-name>matchup.nwp.fc.2m-temperature</fc-2m-temperature-name>

        <!-- Defines the name of the target variable for forecast 2m dew point.
             Default: matchup.nwp.fc.2m_dew_point
        -->
        <fc-2m-dew-point-name>matchup.nwp.fc.2m_dew_point</fc-2m-dew-point-name>

        <!-- Defines the name of the target variable for forecast downward surface solar radiation.
             Default: matchup.nwp.fc.downward_surface_solar_radiation
        -->
        <fc-down-surf-solar-radiation-name>matchup.nwp.fc.downward_surface_solar_radiation</fc-down-surf-solar-radiation-name>

        <!-- Defines the name of the target variable for forecast downward surface thermal radiation.
             Default: matchup.nwp.fc.downward_surface_thermal_radiation
        -->
        <fc-down-surf-thermal-radiation-name>matchup.nwp.fc.downward_surface_thermal_radiation</fc-down-surf-thermal-radiation-name>

        <!-- Defines the name of the target variable for forecast surface solar radiation.
             Default: matchup.nwp.fc.surface_solar_radiation
        -->
        <fc-surf-solar-radiation-name>matchup.nwp.fc.surface_solar_radiation</fc-surf-solar-radiation-name>

        <!-- Defines the name of the target variable for forecast surface thermal radiation.
             Default: matchup.nwp.fc.surface_thermal_radiation
        -->
        <fc-surf-thermal-radiation-name>matchup.nwp.fc.surface_thermal_radiation</fc-surf-thermal-radiation-name>

        <!-- Defines the name of the target variable for forecast turbulent stress east component.
             Default: matchup.nwp.fc.turbulent_stress_east_component
        -->
        <fc-turb-stress-east-name>matchup.nwp.fc.turbulent_stress_east_component</fc-turb-stress-east-name>

        <!-- Defines the name of the target variable for forecast turbulent stress north component.
             Default: matchup.nwp.fc.turbulent_stress_north_component
        -->
        <fc-turb-stress-north-name>matchup.nwp.fc.turbulent_stress_north_component</fc-turb-stress-north-name>

        <!-- Defines the name of the target variable for forecast evaporation.
             Default: matchup.nwp.fc.evaporation
        -->
        <fc-evaporation-name>matchup.nwp.fc.evaporation</fc-evaporation-name>

        <!-- Defines the name of the target variable for forecast total precipitation.
             Default: matchup.nwp.fc.total_precipitation
        -->
        <fc-total-precip-name>matchup.nwp.fc.total_precipitation</fc-total-precip-name>

    </nwp>
 */

public class NwpPostProcessingPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final Configuration configuration = createConfiguration(element);
        if (configuration.verify()) {
            return new NwpPostProcessing(configuration);
        }
        return null;
    }

    @Override
    public String getPostProcessingName() {
        return "nwp";
    }

    static Configuration createConfiguration(Element rootElement) {
        final Configuration configuration = new Configuration();

        final Element deleteOnExitElement = rootElement.getChild("delete-on-exit");
        if (deleteOnExitElement != null) {
            final String deleteOnExitValue = deleteOnExitElement.getValue().trim();
            configuration.setDeleteOnExit(Boolean.getBoolean(deleteOnExitValue));
        }

        final String cdoHomeValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "cdo-home");
        configuration.setCDOHome(cdoHomeValue);

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        final Element analysisStepsElement = rootElement.getChild("analysis-steps");
        if (analysisStepsElement != null) {
            final String analysisStepsValue = analysisStepsElement.getValue().trim();
            configuration.setAnalysisSteps(Integer.parseInt(analysisStepsValue));
        }

        final Element forecastStepsElement = rootElement.getChild("forecast-steps");
        if (forecastStepsElement != null) {
            final String forecastStepsValue = forecastStepsElement.getValue().trim();
            configuration.setForecastSteps(Integer.parseInt(forecastStepsValue));
        }

        final String timeVariableName = JDomUtils.getMandatoryChildTextTrim(rootElement, "time-variable-name");
        configuration.setTimeVariableName(timeVariableName);

        final String longitudeVariableName = JDomUtils.getMandatoryChildTextTrim(rootElement, "longitude-variable-name");
        configuration.setLongitudeVariableName(longitudeVariableName);

        final String latitudeVariableName = JDomUtils.getMandatoryChildTextTrim(rootElement, "latitude-variable-name");
        configuration.setLatitudeVariableName(latitudeVariableName);

        final Element anSeaIceFractionElement = rootElement.getChild("an-sea-ice-fraction-name");
        if (anSeaIceFractionElement != null) {
            configuration.setAnSeaIceFractionName(anSeaIceFractionElement.getValue().trim());
        }

        final Element anSSTElement = rootElement.getChild("an-sst-name");
        if (anSSTElement != null) {
            configuration.setAnSSTName(anSSTElement.getValue().trim());
        }

        final Element anEastWindElement = rootElement.getChild("an-east-wind-name");
        if (anEastWindElement != null) {
            configuration.setAnEastWindName(anEastWindElement.getValue().trim());
        }

        final Element anNorthWindElement = rootElement.getChild("an-north-wind-name");
        if (anNorthWindElement != null) {
            configuration.setAnNorthWindName(anNorthWindElement.getValue().trim());
        }

        final Element fcSSTElement = rootElement.getChild("fc-sst-name");
        if (fcSSTElement != null) {
            configuration.setFcSSTName(fcSSTElement.getValue().trim());
        }

        final Element fcSurfSensibleHeatFluxElement = rootElement.getChild("fc-surf-sensible-heat-flux-name");
        if (fcSurfSensibleHeatFluxElement != null) {
            configuration.setFcSurfSensibleHeatFluxName(fcSurfSensibleHeatFluxElement.getValue().trim());
        }

        final Element fcSurfLatentHeatFluxElement = rootElement.getChild("fc-surf-latent-heat-flux-name");
        if (fcSurfLatentHeatFluxElement != null) {
            configuration.setFcSurfLatentHeatFluxName(fcSurfLatentHeatFluxElement.getValue().trim());
        }

        final Element fcBoundaryLayerHeightElement = rootElement.getChild("fc-boundary-layer-height-name");
        if (fcBoundaryLayerHeightElement != null) {
            configuration.setFcBoundaryLayerHeightName(fcBoundaryLayerHeightElement.getValue().trim());
        }

        final Element fc10mEastWindElement = rootElement.getChild("fc-10m-east-wind-name");
        if (fc10mEastWindElement != null) {
            configuration.setFc10mEastWindName(fc10mEastWindElement.getValue().trim());
        }

        final Element fc10mNorthWindElement = rootElement.getChild("fc-10m-north-wind-name");
        if (fc10mNorthWindElement != null) {
            configuration.setFc10mNorthWindName(fc10mNorthWindElement.getValue().trim());
        }

        final Element fc2mTemperatureElement = rootElement.getChild("fc-2m-temperature-name");
        if (fc2mTemperatureElement != null) {
            configuration.setFc2mTemperatureName(fc2mTemperatureElement.getValue().trim());
        }

        final Element fc2mDewPointElement = rootElement.getChild("fc-2m-dew-point-name");
        if (fc2mDewPointElement != null) {
            configuration.setFc2mDewPointName(fc2mDewPointElement.getValue().trim());
        }

        final Element fcDownSurfSolarRadiationElement = rootElement.getChild("fc-down-surf-solar-radiation-name");
        if (fcDownSurfSolarRadiationElement != null) {
            configuration.setFcDownSurfSolarRadiationName(fcDownSurfSolarRadiationElement.getValue().trim());
        }

        final Element fcDownSurfThermalRadiationElement = rootElement.getChild("fc-down-surf-thermal-radiation-name");
        if (fcDownSurfThermalRadiationElement != null) {
            configuration.setFcDownSurfThermalRadiationName(fcDownSurfThermalRadiationElement.getValue().trim());
        }

        final Element fcSurfSolarRadiationElement = rootElement.getChild("fc-surf-solar-radiation-name");
        if (fcSurfSolarRadiationElement != null) {
            configuration.setFcSurfSolarRadiationName(fcSurfSolarRadiationElement.getValue().trim());
        }

        final Element fcSurfThermalRadiationElement = rootElement.getChild("fc-surf-thermal-radiation-name");
        if (fcSurfThermalRadiationElement != null) {
            configuration.setFcSurfThermalRadiationName(fcSurfThermalRadiationElement.getValue().trim());
        }

        final Element fcTurbStressEastElement = rootElement.getChild("fc-turb-stress-east-name");
        if (fcTurbStressEastElement != null) {
            configuration.setFcTurbStressEastName(fcTurbStressEastElement.getValue().trim());
        }

        final Element fcTurbStressNorthElement = rootElement.getChild("fc-turb-stress-north-name");
        if (fcTurbStressNorthElement != null) {
            configuration.setFcTurbStressNorthName(fcTurbStressNorthElement.getValue().trim());
        }

        final Element fcEvaporationElement = rootElement.getChild("fc-evaporation-name");
        if (fcEvaporationElement != null) {
            configuration.setFcEvaporationName(fcEvaporationElement.getValue().trim());
        }

        final Element fcTotalPrecipElement = rootElement.getChild("fc-total-precip-name");
        if (fcTotalPrecipElement != null) {
            configuration.setFcTotalPrecipName(fcTotalPrecipElement.getValue().trim());
        }

        return configuration;
    }
}
