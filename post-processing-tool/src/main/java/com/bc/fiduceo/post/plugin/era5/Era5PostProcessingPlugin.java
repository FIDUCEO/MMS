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

            final Element humidityElement = JDomUtils.getMandatoryChild(satelliteFieldsElement, "an_ml_q");
            satelliteFieldsConfiguration.set_an_q_name(getElementValueTrimmed(humidityElement));

            final Element temperatureElement = JDomUtils.getMandatoryChild(satelliteFieldsElement, "an_ml_t");
            satelliteFieldsConfiguration.set_an_t_name(getElementValueTrimmed(temperatureElement));

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
