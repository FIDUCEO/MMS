package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Element;

public class Era5PostProcessingPlugin implements PostProcessingPlugin {

    static Configuration createConfiguration(Element rootElement) {
        final Configuration configuration = new Configuration();

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        parseSatelliteFields(rootElement, configuration);

        return configuration;
    }

    private static void parseSatelliteFields(Element rootElement, Configuration configuration) {
        final Element satelliteFieldsElement = rootElement.getChild("satellite-fields");
        if (satelliteFieldsElement != null) {
            final SatelliteFieldsConfiguration satelliteFieldsConfiguration = new SatelliteFieldsConfiguration();

            final Element humidityElement = satelliteFieldsElement.getChild("an_ml_q");
            if (humidityElement != null) {
                satelliteFieldsConfiguration.set_an_q_name(getElementValueTrimmed(humidityElement));
            }

            final Element temperatureElement = satelliteFieldsElement.getChild("an_ml_t");
            if (temperatureElement != null) {
                satelliteFieldsConfiguration.set_an_t_name(getElementValueTrimmed(temperatureElement));
            }

            final Element ozoneElement = satelliteFieldsElement.getChild("an_ml_o3");
            if (ozoneElement != null) {
                satelliteFieldsConfiguration.set_an_o3_name(getElementValueTrimmed(ozoneElement));
            }

            final Element pressureElement = satelliteFieldsElement.getChild("an_ml_lnsp");
            if (pressureElement != null) {
                satelliteFieldsConfiguration.set_an_lnsp_name(getElementValueTrimmed(pressureElement));
            }

            final Element temp2metersElement = satelliteFieldsElement.getChild("an_sfc_t2m");
            if (temp2metersElement != null) {
                satelliteFieldsConfiguration.set_an_t2m_name(getElementValueTrimmed(temp2metersElement));
            }

            final Element windUElement = satelliteFieldsElement.getChild("an_sfc_u10");
            if (windUElement != null) {
                satelliteFieldsConfiguration.set_an_u10_name(getElementValueTrimmed(windUElement));
            }

            final Element windVElement = satelliteFieldsElement.getChild("an_sfc_v10");
            if (windVElement != null) {
                satelliteFieldsConfiguration.set_an_v10_name(getElementValueTrimmed(windVElement));
            }

            final Element seaIceElement = satelliteFieldsElement.getChild("an_sfc_siconc");
            if (seaIceElement != null) {
                satelliteFieldsConfiguration.set_an_siconc_name(getElementValueTrimmed(seaIceElement));
            }

            final Element surfPressElement = satelliteFieldsElement.getChild("an_sfc_msl");
            if (surfPressElement != null) {
                satelliteFieldsConfiguration.set_an_msl_name(getElementValueTrimmed(surfPressElement));
            }

            final Element skinTempElement = satelliteFieldsElement.getChild("an_sfc_skt");
            if (skinTempElement != null) {
                satelliteFieldsConfiguration.set_an_skt_name(getElementValueTrimmed(skinTempElement));
            }

            final Element sstElement = satelliteFieldsElement.getChild("an_sfc_sst");
            if (sstElement != null) {
                satelliteFieldsConfiguration.set_an_sst_name(getElementValueTrimmed(sstElement));
            }

            final Element cloudElement = satelliteFieldsElement.getChild("an_sfc_tcc");
            if (cloudElement != null) {
                satelliteFieldsConfiguration.set_an_tcc_name(getElementValueTrimmed(cloudElement));
            }

            final Element waterVaporElement = satelliteFieldsElement.getChild("an_sfc_tcwv");
            if (waterVaporElement != null) {
                satelliteFieldsConfiguration.set_an_tcwv_name(getElementValueTrimmed(waterVaporElement));
            }

            configuration.setSatelliteFields(satelliteFieldsConfiguration);
        }
    }

    private static String getElementValueTrimmed(Element element) {
        return element.getValue().trim();
    }

    @Override
    public PostProcessing createPostProcessing(Element element) {
        final Configuration configuration = createConfiguration(element);
        return new Era5PostProcessing(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return "era5";
    }
}
