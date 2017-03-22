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
        // @todo 3 tb/** shouldn't we throw an exception here? Or at least log something meaningful? 2017-03-22
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
            final String deleteOnExitValue = getElementValueTrimmed(deleteOnExitElement);
            configuration.setDeleteOnExit(Boolean.parseBoolean(deleteOnExitValue));
        }

        final String cdoHomeValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "cdo-home");
        configuration.setCDOHome(cdoHomeValue);

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        parseTimeExtractionConfiguration(rootElement, configuration);
        parseSensorExtractionConfiguration(rootElement, configuration);

        return configuration;
    }

    private static void parseSensorExtractionConfiguration(Element rootElement, Configuration configuration) {
        final Element sensorExtractionElement = rootElement.getChild("sensor-extraction");
        if (sensorExtractionElement != null) {
            final SensorExtractConfiguration sensorExtractConfig = new SensorExtractConfiguration();

            final Element anSeaIceFractionElement = sensorExtractionElement.getChild("an-ci-name");
            if (anSeaIceFractionElement != null) {
                sensorExtractConfig.setAn_CI_name(getElementValueTrimmed(anSeaIceFractionElement));
            }

            configuration.setSensorExtractConfiguration(sensorExtractConfig);
        } else {
            configuration.setSensorExtractConfiguration(null);
        }
    }

    private static void parseTimeExtractionConfiguration(Element rootElement, Configuration configuration) {
        final Element timeSeriesExtractionElement = rootElement.getChild("time-series-extraction");
        if (timeSeriesExtractionElement != null) {
            final TimeSeriesConfiguration timeSeriesConfiguration = new TimeSeriesConfiguration();

            final Element analysisStepsElement = timeSeriesExtractionElement.getChild("analysis-steps");
            if (analysisStepsElement != null) {
                final String analysisStepsValue = getElementValueTrimmed(analysisStepsElement);
                timeSeriesConfiguration.setAnalysisSteps(Integer.parseInt(analysisStepsValue));
            }

            final Element forecastStepsElement = timeSeriesExtractionElement.getChild("forecast-steps");
            if (forecastStepsElement != null) {
                final String forecastStepsValue = getElementValueTrimmed(forecastStepsElement);
                timeSeriesConfiguration.setForecastSteps(Integer.parseInt(forecastStepsValue));
            }

            final Element anCenterTimeVariableName = timeSeriesExtractionElement.getChild("analysis-center-time-name");
            if (anCenterTimeVariableName != null) {
                timeSeriesConfiguration.setAnCenterTimeName(getElementValueTrimmed(anCenterTimeVariableName));
            }

            final Element fcCenterTimeVariableName = timeSeriesExtractionElement.getChild("forecast-center-time-name");
            if (fcCenterTimeVariableName != null) {
                timeSeriesConfiguration.setFcCenterTimeName(getElementValueTrimmed(fcCenterTimeVariableName));
            }

            final String timeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "time-variable-name");
            timeSeriesConfiguration.setTimeVariableName(timeVariableName);

            final String longitudeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "longitude-variable-name");
            timeSeriesConfiguration.setLongitudeVariableName(longitudeVariableName);

            final String latitudeVariableName = JDomUtils.getMandatoryChildTextTrim(timeSeriesExtractionElement, "latitude-variable-name");
            timeSeriesConfiguration.setLatitudeVariableName(latitudeVariableName);

            final Element anSeaIceFractionElement = timeSeriesExtractionElement.getChild("an-ci-name");
            if (anSeaIceFractionElement != null) {
                timeSeriesConfiguration.setAn_CI_name(getElementValueTrimmed(anSeaIceFractionElement));
            }

            final Element anSSTElement = timeSeriesExtractionElement.getChild("an-sstk-name");
            if (anSSTElement != null) {
                timeSeriesConfiguration.setAn_SSTK_name(getElementValueTrimmed(anSSTElement));
            }

            final Element fcSSTElement = timeSeriesExtractionElement.getChild("fc-sstk-name");
            if (fcSSTElement != null) {
                timeSeriesConfiguration.setFc_SSTK_name(getElementValueTrimmed(fcSSTElement));
            }

            final Element anEastWindElement = timeSeriesExtractionElement.getChild("an-u10-name");
            if (anEastWindElement != null) {
                timeSeriesConfiguration.setAn_U10_name(getElementValueTrimmed(anEastWindElement));
            }

            final Element fc10mEastWindElement = timeSeriesExtractionElement.getChild("fc-u10-name");
            if (fc10mEastWindElement != null) {
                timeSeriesConfiguration.setFc_U10_name(getElementValueTrimmed(fc10mEastWindElement));
            }

            final Element anNorthWindElement = timeSeriesExtractionElement.getChild("an-v10-name");
            if (anNorthWindElement != null) {
                timeSeriesConfiguration.setAn_V10_name(getElementValueTrimmed(anNorthWindElement));
            }

            final Element fc10mNorthWindElement = timeSeriesExtractionElement.getChild("fc-v10-name");
            if (fc10mNorthWindElement != null) {
                timeSeriesConfiguration.setFc_V10_name(getElementValueTrimmed(fc10mNorthWindElement));
            }

            final Element fcMeanPressureElement = timeSeriesExtractionElement.getChild("fc-msl-name");
            if (fcMeanPressureElement != null) {
                timeSeriesConfiguration.setFc_MSL_name(getElementValueTrimmed(fcMeanPressureElement));
            }

            final Element fc2mTemperatureElement = timeSeriesExtractionElement.getChild("fc-t2-name");
            if (fc2mTemperatureElement != null) {
                timeSeriesConfiguration.setFc_T2_name(getElementValueTrimmed(fc2mTemperatureElement));
            }

            final Element fc2mDewPointElement = timeSeriesExtractionElement.getChild("fc-d2-name");
            if (fc2mDewPointElement != null) {
                timeSeriesConfiguration.setFc_D2_name(getElementValueTrimmed(fc2mDewPointElement));
            }

            final Element fcTotalPrecipElement = timeSeriesExtractionElement.getChild("fc-tp-name");
            if (fcTotalPrecipElement != null) {
                timeSeriesConfiguration.setFc_TP_name(getElementValueTrimmed(fcTotalPrecipElement));
            }

            final Element anCloudLiquidWaterContentElement = timeSeriesExtractionElement.getChild("an-clwc-name");
            if (anCloudLiquidWaterContentElement != null) {
                timeSeriesConfiguration.setAn_CLWC_name(getElementValueTrimmed(anCloudLiquidWaterContentElement));
            }

            final Element fcCloudLiquidWaterContentElement = timeSeriesExtractionElement.getChild("fc-clwc-name");
            if (fcCloudLiquidWaterContentElement != null) {
                timeSeriesConfiguration.setFc_CLWC_name(getElementValueTrimmed(fcCloudLiquidWaterContentElement));
            }

            final Element anTotalColumnWaterVapourElement = timeSeriesExtractionElement.getChild("an-tcwv-name");
            if (anTotalColumnWaterVapourElement != null) {
                timeSeriesConfiguration.setAn_TCWV_name(getElementValueTrimmed(anTotalColumnWaterVapourElement));
            }

            final Element fcTotalColumnWaterVapour = timeSeriesExtractionElement.getChild("fc-tcwv-name");
            if (fcTotalColumnWaterVapour != null) {
                timeSeriesConfiguration.setFc_TCWV_name(getElementValueTrimmed(fcTotalColumnWaterVapour));
            }

            final Element fcSurfSensibleHeatFluxElement = timeSeriesExtractionElement.getChild("fc-sshf-name");
            if (fcSurfSensibleHeatFluxElement != null) {
                timeSeriesConfiguration.setFc_SSHF_name(getElementValueTrimmed(fcSurfSensibleHeatFluxElement));
            }

            final Element fcSurfLatentHeatFluxElement = timeSeriesExtractionElement.getChild("fc-slhf-name");
            if (fcSurfLatentHeatFluxElement != null) {
                timeSeriesConfiguration.setFc_SLHF_name(getElementValueTrimmed(fcSurfLatentHeatFluxElement));
            }

            final Element fcBoundaryLayerHeightElement = timeSeriesExtractionElement.getChild("fc-blh-name");
            if (fcBoundaryLayerHeightElement != null) {
                timeSeriesConfiguration.setFc_BLH_name(getElementValueTrimmed(fcBoundaryLayerHeightElement));
            }

            final Element fcDownSurfSolarRadiationElement = timeSeriesExtractionElement.getChild("fc-ssrd-name");
            if (fcDownSurfSolarRadiationElement != null) {
                timeSeriesConfiguration.setFc_SSRD_name(getElementValueTrimmed(fcDownSurfSolarRadiationElement));
            }

            final Element fcDownSurfThermalRadiationElement = timeSeriesExtractionElement.getChild("fc-strd-name");
            if (fcDownSurfThermalRadiationElement != null) {
                timeSeriesConfiguration.setFc_STRD_name(getElementValueTrimmed(fcDownSurfThermalRadiationElement));
            }

            final Element fcSurfSolarRadiationElement = timeSeriesExtractionElement.getChild("fc-ssr-name");
            if (fcSurfSolarRadiationElement != null) {
                timeSeriesConfiguration.setFc_SSR_name(getElementValueTrimmed(fcSurfSolarRadiationElement));
            }

            final Element fcSurfThermalRadiationElement = timeSeriesExtractionElement.getChild("fc-str-name");
            if (fcSurfThermalRadiationElement != null) {
                timeSeriesConfiguration.setFc_STR_name(getElementValueTrimmed(fcSurfThermalRadiationElement));
            }

            final Element fcTurbStressEastElement = timeSeriesExtractionElement.getChild("fc-ewss-name");
            if (fcTurbStressEastElement != null) {
                timeSeriesConfiguration.setFc_EWSS_name(getElementValueTrimmed(fcTurbStressEastElement));
            }

            final Element fcTurbStressNorthElement = timeSeriesExtractionElement.getChild("fc-nsss-name");
            if (fcTurbStressNorthElement != null) {
                timeSeriesConfiguration.setFc_NSSS_name(getElementValueTrimmed(fcTurbStressNorthElement));
            }

            final Element fcEvaporationElement = timeSeriesExtractionElement.getChild("fc-e-name");
            if (fcEvaporationElement != null) {
                timeSeriesConfiguration.setFc_E_name(getElementValueTrimmed(fcEvaporationElement));
            }

            configuration.setTimeSeriesConfiguration(timeSeriesConfiguration);
        } else {
            configuration.setTimeSeriesConfiguration(null);
        }
    }

    private static String getElementValueTrimmed(Element element) {
        return element.getValue().trim();
    }
}
