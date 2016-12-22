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

    </nwp>
 */

public class NwpPostProcessingPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element) {
       return new NwpPostProcessing();
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

        return configuration;
    }
}
