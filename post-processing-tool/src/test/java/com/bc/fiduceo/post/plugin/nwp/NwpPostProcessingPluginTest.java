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


import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NwpPostProcessingPluginTest {

    private NwpPostProcessingPlugin plugin;

    @Before
    public void setUp() {
        plugin = new NwpPostProcessingPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("nwp", plugin.getPostProcessingName());
    }

    @Test
    public void testCreateConfiguration_deleteOnExit() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <delete-on-exit>false</delete-on-exit>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertFalse(configuration.isDeleteOnExit());
    }

    @Test
    public void testCreateConfiguration_cdoHome() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <cdo-home>/in/this/directory</cdo-home>" +
                "" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("/in/this/directory", configuration.getCDOHome());
    }

    @Test
    public void testCreateConfiguration_missing_cdoHome() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_TimeSeriesExtract_allRelevantVariables() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <time-series-extraction>" +
                "        <analysis-steps>19</analysis-steps>" +
                "        <forecast-steps>27</forecast-steps>" +
                "        <analysis-center-time-name>watch_me_now</analysis-center-time-name>" +
                "        <forecast-center-time-name>watch_me_tomorrow</forecast-center-time-name>" +
                "        <time-variable-name>the_time</time-variable-name>" +
                "        <longitude-variable-name>long_John</longitude-variable-name>" +
                "        <latitude-variable-name>cafe_latte</latitude-variable-name>" +
                "" +
                "        <an-ci-name>nogger</an-ci-name>" +
                "        <an-sstk-name>sea_surface_T</an-sstk-name>" +
                "        <fc-sstk-name>the_sea_fore</fc-sstk-name>" +
                "        <an-u10-name>blow_to_russia</an-u10-name>" +
                "        <fc-u10-name>blow_eastern</fc-u10-name>" +
                "        <an-v10-name>to_iceland</an-v10-name>" +
                "        <fc-v10-name>to_greenland</fc-v10-name>" +
                "        <fc-msl-name>pressure</fc-msl-name>" +
                "        <fc-t2-name>hot_heat</fc-t2-name>" +
                "        <fc-d2-name>dewy</fc-d2-name>" +
                "        <fc-tp-name>precipate</fc-tp-name>" +
                "        <an-clwc-name>cloudy_water</an-clwc-name>" +
                "        <fc-clwc-name>fc_cloudy_water</fc-clwc-name>" +
                "        <an-tcwv-name>total_water</an-tcwv-name>" +
                "        <fc-tcwv-name>fc_total_water</fc-tcwv-name>" +
                "        <fc-sshf-name>sensible_flux</fc-sshf-name>" +
                "        <fc-slhf-name>latent_flux</fc-slhf-name>" +
                "        <fc-blh-name>lay_down</fc-blh-name>" +
                "        <fc-ssrd-name>surf_rad</fc-ssrd-name>" +
                "        <fc-strd-name>therm_rad</fc-strd-name>" +
                "        <fc-ssr-name>surf_rad</fc-ssr-name>" +
                "        <fc-str-name>surf_therm_rad</fc-str-name>" +
                "        <fc-ewss-name>east_stress</fc-ewss-name>" +
                "        <fc-nsss-name>north_stress</fc-nsss-name>" +
                "        <fc-e-name>vapoclean</fc-e-name>" +
                "    </time-series-extraction>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);

        assertTrue(configuration.isTimeSeriesExtraction());

        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();
        assertNotNull(timeSeriesConfiguration);
        assertEquals(19, timeSeriesConfiguration.getAnalysisSteps());
        assertEquals(27, timeSeriesConfiguration.getForecastSteps());

        assertEquals("the_time", timeSeriesConfiguration.getTimeVariableName());
        assertEquals("long_John", timeSeriesConfiguration.getLongitudeVariableName());
        assertEquals("cafe_latte", timeSeriesConfiguration.getLatitudeVariableName());

        assertEquals("watch_me_now", timeSeriesConfiguration.getAnCenterTimeName());
        assertEquals("watch_me_tomorrow", timeSeriesConfiguration.getFcCenterTimeName());

        assertEquals("nogger", timeSeriesConfiguration.getAn_CI_name());
        assertEquals("sea_surface_T", timeSeriesConfiguration.getAn_SSTK_name());
        assertEquals("the_sea_fore", timeSeriesConfiguration.getFc_SSTK_name());
        assertEquals("blow_to_russia", timeSeriesConfiguration.getAn_U10_name());
        assertEquals("blow_eastern", timeSeriesConfiguration.getFc_U10_name());
        assertEquals("to_iceland", timeSeriesConfiguration.getAn_V10_name());
        assertEquals("to_greenland", timeSeriesConfiguration.getFc_V10_name());
        assertEquals("pressure", timeSeriesConfiguration.getFc_MSL_name());
        assertEquals("hot_heat", timeSeriesConfiguration.getFc_T2_name());
        assertEquals("dewy", timeSeriesConfiguration.getFc_D2_name());
        assertEquals("precipate", timeSeriesConfiguration.getFc_TP_name());
        assertEquals("cloudy_water", timeSeriesConfiguration.getAn_CLWC_name());
        assertEquals("fc_cloudy_water", timeSeriesConfiguration.getFc_CLWC_name());
        assertEquals("total_water", timeSeriesConfiguration.getAn_TCWV_name());
        assertEquals("fc_total_water", timeSeriesConfiguration.getFc_TCWV_name());
        assertEquals("sensible_flux", timeSeriesConfiguration.getFc_SSHF_name());
        assertEquals("latent_flux", timeSeriesConfiguration.getFc_SLHF_name());
        assertEquals("lay_down", timeSeriesConfiguration.getFc_BLH_name());
        assertEquals("surf_rad", timeSeriesConfiguration.getFc_SSRD_name());
        assertEquals("therm_rad", timeSeriesConfiguration.getFc_STRD_name());
        assertEquals("surf_rad", timeSeriesConfiguration.getFc_SSR_name());
        assertEquals("surf_therm_rad", timeSeriesConfiguration.getFc_STR_name());
        assertEquals("east_stress", timeSeriesConfiguration.getFc_EWSS_name());
        assertEquals("north_stress", timeSeriesConfiguration.getFc_NSSS_name());
        assertEquals("vapoclean", timeSeriesConfiguration.getFc_E_name());
    }

    @Test
    public void testCreateConfiguration_TimeSeriesExtract_missingTimeVariable() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <time-series-extraction>" +
                "        <analysis-steps>19</analysis-steps>" +
                "        <forecast-steps>27</forecast-steps>" +
                "        <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "        <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "    </time-series-extraction>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_TimeSeriesExtract_missingLongitudeVariable() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <time-series-extraction>" +
                "        <analysis-steps>19</analysis-steps>" +
                "        <forecast-steps>27</forecast-steps>" +
                "        <time-variable-name>the_time</time-variable-name>" +
                "        <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "    </time-series-extraction>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_TimeSeriesExtract_missingLatitudeVariable() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <time-series-extraction>" +
                "        <analysis-steps>19</analysis-steps>" +
                "        <forecast-steps>27</forecast-steps>" +
                "        <time-variable-name>the_time</time-variable-name>" +
                "        <longitude-variable-name>long_John</longitude-variable-name>" +
                "    </time-series-extraction>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }


    @Test
    public void testCreateConfiguration_TimeSeriesExtract_switchedOff() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertFalse(configuration.isTimeSeriesExtraction());
    }

    @Test
    public void testCreateConfiguration_SensorExtract_allRelevantVariables() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <sensor-extraction>" +
                "        <an-ci-name>ice_cold</an-ci-name>" +
                "    </sensor-extraction>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        final SensorExtractConfiguration sensorConfig = configuration.getSensorExtractConfiguration();
        assertNotNull(sensorConfig);

        assertEquals("ice_cold", sensorConfig.getAn_CI_name());
    }

    @Test
    public void testCreateConfiguration_SensorExtract_switchedOff() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertFalse(configuration.isSensorExtraction());
    }

    @Test
    public void testCreateConfiguration_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("/the/auxiliary/files", configuration.getNWPAuxDir());
    }

    @Test
    public void testCreateConfiguration_missing_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
