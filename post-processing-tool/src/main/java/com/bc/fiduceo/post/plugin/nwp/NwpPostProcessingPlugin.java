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

// @todo 2 tb/tb add example XML config as comment here 2017-03-20

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
            configuration.setDeleteOnExit(Boolean.parseBoolean(deleteOnExitValue));
        }

        final String cdoHomeValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "cdo-home");
        configuration.setCDOHome(cdoHomeValue);

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        parseTimeExtractionConfiguration(rootElement, configuration);

        final Element anCenterTimeVariableName = rootElement.getChild("analysis-center-time-variable-name");
        if (anCenterTimeVariableName != null) {
            configuration.setAnCenterTimeName(anCenterTimeVariableName.getValue().trim());
        }

        final Element fcCenterTimeVariableName = rootElement.getChild("forecast-center-time-variable-name");
        if (fcCenterTimeVariableName != null) {
            configuration.setFcCenterTimeName(fcCenterTimeVariableName.getValue().trim());
        }

        final Element anTotalColumnWaterVapourElement = rootElement.getChild("an-total-column-water-vapour-name");
        if (anTotalColumnWaterVapourElement != null) {
            configuration.setAnTotalColumnWaterVapourName(anTotalColumnWaterVapourElement.getValue().trim());
        }

        final Element anCloudLiquidWaterContentElement = rootElement.getChild("an-cloud-liquid-water-content-name");
        if (anCloudLiquidWaterContentElement != null) {
            configuration.setAnCloudLiquidWaterContentName(anCloudLiquidWaterContentElement.getValue().trim());
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

        final Element fcTotalColumnWaterVapour = rootElement.getChild("fc-total-column-water-vapour-name");
        if (fcTotalColumnWaterVapour != null) {
            configuration.setFcTotalColumnWaterVapourName(fcTotalColumnWaterVapour.getValue().trim());
        }

        final Element fcCloudLiquidWaterContentElement = rootElement.getChild("fc-cloud-liquid-water-content-name");
        if (fcCloudLiquidWaterContentElement != null) {
            configuration.setFcCloudLiquidWaterContentName(fcCloudLiquidWaterContentElement.getValue().trim());
        }

        return configuration;
    }

    private static void parseTimeExtractionConfiguration(Element rootElement, Configuration configuration) {
        final Element timeSeriesExtractionElement = rootElement.getChild("time-series-extraction");
        if (timeSeriesExtractionElement != null) {
            final TimeSeriesConfiguration timeSeriesConfiguration = new TimeSeriesConfiguration();

            final Element analysisStepsElement = timeSeriesExtractionElement.getChild("analysis-steps");
            if (analysisStepsElement != null) {
                final String analysisStepsValue = analysisStepsElement.getValue().trim();
                timeSeriesConfiguration.setAnalysisSteps(Integer.parseInt(analysisStepsValue));
            }

            final Element forecastStepsElement = timeSeriesExtractionElement.getChild("forecast-steps");
            if (forecastStepsElement != null) {
                final String forecastStepsValue = forecastStepsElement.getValue().trim();
                timeSeriesConfiguration.setForecastSteps(Integer.parseInt(forecastStepsValue));
            }

            final String timeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "time-variable-name");
            timeSeriesConfiguration.setTimeVariableName(timeVariableName);

            final String longitudeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "longitude-variable-name");
            timeSeriesConfiguration.setLongitudeVariableName(longitudeVariableName);

            final String latitudeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "latitude-variable-name");
            timeSeriesConfiguration.setLatitudeVariableName(latitudeVariableName);

            final Element anSeaIceFractionElement = timeSeriesExtractionElement.getChild("an-ci-name");
            if (anSeaIceFractionElement != null) {
                timeSeriesConfiguration.setAn_CI_name(anSeaIceFractionElement.getValue().trim());
            }

            final Element anSSTElement = timeSeriesExtractionElement.getChild("an-sstk-name");
            if (anSSTElement != null) {
                timeSeriesConfiguration.setAn_SSTK_name(anSSTElement.getValue().trim());
            }

            final Element fcSSTElement = timeSeriesExtractionElement.getChild("fc-sstk-name");
            if (fcSSTElement != null) {
                timeSeriesConfiguration.setFc_SSTK_name(fcSSTElement.getValue().trim());
            }

            final Element anEastWindElement = timeSeriesExtractionElement.getChild("an-u10-name");
            if (anEastWindElement != null) {
                timeSeriesConfiguration.setAn_U10_name(anEastWindElement.getValue().trim());
            }

            final Element fc10mEastWindElement = timeSeriesExtractionElement.getChild("fc-u10-name");
            if (fc10mEastWindElement != null) {
                timeSeriesConfiguration.setFc_U10_name(fc10mEastWindElement.getValue().trim());
            }

            final Element anNorthWindElement = timeSeriesExtractionElement.getChild("an-v10-name");
            if (anNorthWindElement != null) {
                timeSeriesConfiguration.setAn_V10_name(anNorthWindElement.getValue().trim());
            }

            final Element fc10mNorthWindElement = timeSeriesExtractionElement.getChild("fc-v10-name");
            if (fc10mNorthWindElement != null) {
                timeSeriesConfiguration.setFc_V10_name(fc10mNorthWindElement.getValue().trim());
            }

            final Element fcMeanPressureElement = timeSeriesExtractionElement.getChild("fc-msl-name");
            if (fcMeanPressureElement != null) {
                timeSeriesConfiguration.setFc_MSL_name(fcMeanPressureElement.getValue().trim());
            }

            configuration.setTimeSeriesConfiguration(timeSeriesConfiguration);
        }  else {
            configuration.setTimeSeriesConfiguration(null);
        }
    }
}
