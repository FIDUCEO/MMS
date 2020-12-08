package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom.Attribute;
import org.jdom.Element;

public class Era5PostProcessingPlugin implements PostProcessingPlugin {

    static Configuration createConfiguration(Element rootElement) {
        final Configuration configuration = new Configuration();

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        parseSatelliteFields(rootElement, configuration);
        parseMatchupFields(rootElement, configuration);

        return configuration;
    }

    private static void parseSatelliteFields(Element rootElement, Configuration configuration) {
        final Element satelliteFieldsElement = rootElement.getChild("satellite-fields");
        if (satelliteFieldsElement != null) {
            final SatelliteFieldsConfiguration satelliteFieldsConfiguration = new SatelliteFieldsConfiguration();

            final Element xDimElement = satelliteFieldsElement.getChild("x_dim");
            if (xDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(xDimElement, "name");
                satelliteFieldsConfiguration.set_x_dim_name(nameElement.getValue());
                final Attribute lengthElement = JDomUtils.getMandatoryAttribute(xDimElement, "length");
                satelliteFieldsConfiguration.set_x_dim(Integer.parseInt(lengthElement.getValue()));
            }

            final Element yDimElement = satelliteFieldsElement.getChild("y_dim");
            if (yDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(yDimElement, "name");
                satelliteFieldsConfiguration.set_y_dim_name(nameElement.getValue());
                final Attribute lengthElement = JDomUtils.getMandatoryAttribute(yDimElement, "length");
                satelliteFieldsConfiguration.set_y_dim(Integer.parseInt(lengthElement.getValue()));
            }

            final Element zDimElement = satelliteFieldsElement.getChild("z_dim");
            if (zDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(zDimElement, "name");
                satelliteFieldsConfiguration.set_z_dim_name(nameElement.getValue());
                final Attribute lengthElement = zDimElement.getAttribute("length");
                if (lengthElement != null) {
                    satelliteFieldsConfiguration.set_z_dim(Integer.parseInt(lengthElement.getValue()));
                }
            }

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

            final Element era5TimeElement = satelliteFieldsElement.getChild("era5_time_variable");
            if (era5TimeElement != null) {
                satelliteFieldsConfiguration.set_nwp_time_variable_name(getElementValueTrimmed(era5TimeElement));
            }

            final Element lonElement = satelliteFieldsElement.getChild("longitude_variable");
            if (lonElement != null) {
                satelliteFieldsConfiguration.set_longitude_variable_name(getElementValueTrimmed(lonElement));
            }

            final Element latElement = satelliteFieldsElement.getChild("latitude_variable");
            if (latElement != null) {
                satelliteFieldsConfiguration.set_latitude_variable_name(getElementValueTrimmed(latElement));
            }

            final Element timeElement = satelliteFieldsElement.getChild("time_variable");
            if (timeElement != null) {
                satelliteFieldsConfiguration.set_time_variable_name(getElementValueTrimmed(timeElement));
            }

            configuration.setSatelliteFields(satelliteFieldsConfiguration);
        }
    }

    private static void parseMatchupFields(Element rootElement, Configuration configuration) {
        final Element matchupFieldsElements = rootElement.getChild("matchup-fields");
        if (matchupFieldsElements != null) {
            final MatchupFieldsConfiguration matchupFieldsConfiguration = new MatchupFieldsConfiguration();

            final Element windUElement = matchupFieldsElements.getChild("an_sfc_u10");
            if (windUElement != null) {
                matchupFieldsConfiguration.set_an_u10_name(getElementValueTrimmed(windUElement));
            }

            final Element windVElement = matchupFieldsElements.getChild("an_sfc_v10");
            if (windVElement != null) {
                matchupFieldsConfiguration.set_an_v10_name(getElementValueTrimmed(windVElement));
            }

            final Element siconcElement = matchupFieldsElements.getChild("an_sfc_siconc");
            if (siconcElement != null) {
                matchupFieldsConfiguration.set_an_siconc_name(getElementValueTrimmed(siconcElement));
            }

            final Element sstElement = matchupFieldsElements.getChild("an_sfc_sst");
            if (sstElement != null) {
                matchupFieldsConfiguration.set_an_sst_name(getElementValueTrimmed(sstElement));
            }

            final Element metssElement = matchupFieldsElements.getChild("fc_sfc_metss");
            if (metssElement != null) {
                matchupFieldsConfiguration.set_fc_metss_name(getElementValueTrimmed(metssElement));
            }

            final Element mntssElement = matchupFieldsElements.getChild("fc_sfc_mntss");
            if (mntssElement != null) {
                matchupFieldsConfiguration.set_fc_mntss_name(getElementValueTrimmed(mntssElement));
            }

            final Element mslhfElement = matchupFieldsElements.getChild("fc_sfc_mslhf");
            if (mslhfElement != null) {
                matchupFieldsConfiguration.set_fc_mslhf_name(getElementValueTrimmed(mslhfElement));
            }

            final Element msnlwrfElement = matchupFieldsElements.getChild("fc_sfc_msnlwrf");
            if (msnlwrfElement != null) {
                matchupFieldsConfiguration.set_fc_msnlwrf_name(getElementValueTrimmed(msnlwrfElement));
            }

            final Element msnswrfElement = matchupFieldsElements.getChild("fc_sfc_msnswrf");
            if (msnswrfElement != null) {
                matchupFieldsConfiguration.set_fc_msnswrf_name(getElementValueTrimmed(msnswrfElement));
            }

            final Element msshfElement = matchupFieldsElements.getChild("fc_sfc_msshf");
            if (msshfElement != null) {
                matchupFieldsConfiguration.set_fc_msshf_name(getElementValueTrimmed(msshfElement));
            }

            final Element timeStepsPastElement = matchupFieldsElements.getChild("time_steps_past");
            if (timeStepsPastElement != null) {
                final String value = timeStepsPastElement.getValue();
                matchupFieldsConfiguration.set_time_steps_past(Integer.parseInt(value));
            }

            final Element timeStepsFutureElement = matchupFieldsElements.getChild("time_steps_future");
            if (timeStepsFutureElement != null) {
                final String value = timeStepsFutureElement.getValue();
                matchupFieldsConfiguration.set_time_steps_future(Integer.parseInt(value));
            }

            final Element timeDimNameElement = matchupFieldsElements.getChild("time_dim_name");
            if (timeDimNameElement != null) {
                matchupFieldsConfiguration.set_time_dim_name(getElementValueTrimmed(timeDimNameElement));
            }

            final Element timeVarNameElement = matchupFieldsElements.getChild("time_variable");
            if (timeVarNameElement != null) {
                matchupFieldsConfiguration.set_time_variable_name(getElementValueTrimmed(timeVarNameElement));
            }

            final Element nwpTimeVarNameElement = matchupFieldsElements.getChild("era5_time_variable");
            if (nwpTimeVarNameElement != null) {
                matchupFieldsConfiguration.set_nwp_time_variable_name(getElementValueTrimmed(nwpTimeVarNameElement));
            }

            configuration.setMatchupFields(matchupFieldsConfiguration);
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
