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

        return configuration;
    }
}
