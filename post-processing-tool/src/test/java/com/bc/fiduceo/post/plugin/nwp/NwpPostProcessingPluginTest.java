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

import static org.junit.Assert.*;

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
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
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
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("/in/this/directory", configuration.getCDOHome());
    }

    @Test
    public void testCreateConfiguration_missing_cdoHome() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_analysisSteps() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <analysis-steps>19</analysis-steps>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals(19, configuration.getAnalysisSteps());
    }

    @Test
    public void testCreateConfiguration_forecastSteps() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <forecast-steps>27</forecast-steps>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>we need this, its mandatory</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals(27, configuration.getForecastSteps());
    }

    @Test
    public void testCreateConfiguration_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("/the/auxiliary/files", configuration.getNWPAuxDir());
    }

    @Test
    public void testCreateConfiguration_missing_nwpAuxDir() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_timeVariableName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <time-variable-name>big_ben</time-variable-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("big_ben", configuration.getTimeVariableName());
    }

    @Test
    public void testCreateConfiguration_anCenterTimeName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <analysis-center-time-variable-name>watch_me_now</analysis-center-time-variable-name>" +
                "" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("watch_me_now", configuration.getAnCenterTimeName());
    }

    @Test
    public void testCreateConfiguration_fcCenterTimeName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <forecast-center-time-variable-name>in_two_minutes</forecast-center-time-variable-name>" +
                "" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("in_two_minutes", configuration.getFcCenterTimeName());
    }

    @Test
    public void testCreateConfiguration_longitudeVariableName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <longitude-variable-name>lons_my_dear</longitude-variable-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("lons_my_dear", configuration.getLongitudeVariableName());
    }

    @Test
    public void testCreateConfiguration_longitudeVariableName_missing() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            NwpPostProcessingPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_latitudeVariableName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <latitude-variable-name>latida</latitude-variable-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("latida", configuration.getLatitudeVariableName());
    }

    @Test
    public void testCreateConfiguration_anSeaIceFractionName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <an-sea-ice-fraction-name>nogger</an-sea-ice-fraction-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("nogger", configuration.getAnSeaIceFractionName());
    }

    @Test
    public void testCreateConfiguration_anSSTName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <an-sst-name>quite_warm</an-sst-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("quite_warm", configuration.getAnSSTName());
    }

    @Test
    public void testCreateConfiguration_anEastWindName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <an-east-wind-name>breeze</an-east-wind-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("breeze", configuration.getAnEastWindName());
    }

    @Test
    public void testCreateConfiguration_anNorthWindName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <an-north-wind-name>from_ice_land</an-north-wind-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("from_ice_land", configuration.getAnNorthWindName());
    }

    @Test
    public void testCreateConfiguration_fcSSTName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-sst-name>temperature</fc-sst-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("temperature", configuration.getFcSSTName());
    }

    @Test
    public void testCreateConfiguration_fcSurfaceSensibleHeatFluxName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-surf-sensible-heat-flux-name>fluxi</fc-surf-sensible-heat-flux-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("fluxi", configuration.getFcSurfSensibleHeatFluxName());
    }

    @Test
    public void testCreateConfiguration_fcSurfaceLatentHeatFluxName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-surf-latent-heat-flux-name>lati-flux</fc-surf-latent-heat-flux-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("lati-flux", configuration.getFcSurfLatentHeatFluxName());
    }

    @Test
    public void testCreateConfiguration_fcBoundaryLayerHeightName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-boundary-layer-height-name>christine</fc-boundary-layer-height-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("christine", configuration.getFcBoundaryLayerHeightName());
    }

    @Test
    public void testCreateConfiguration_fc10mEastWindName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-10m-east-wind-name>out-of-russia</fc-10m-east-wind-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("out-of-russia", configuration.getFc10mEastWindName());
    }

    @Test
    public void testCreateConfiguration_fc10mNorthWindName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-10m-north-wind-name>from Sweden</fc-10m-north-wind-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("from Sweden", configuration.getFc10mNorthWindName());
    }

    @Test
    public void testCreateConfiguration_fc2mTemperatureName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-2m-temperature-name>Kevin</fc-2m-temperature-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Kevin", configuration.getFc2mTemperatureName());
    }

    @Test
    public void testCreateConfiguration_fc2mDewPointName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-2m-dew-point-name>pointy</fc-2m-dew-point-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("pointy", configuration.getFc2mDewPointName());
    }

    @Test
    public void testCreateConfiguration_fcDownSurfSolarRadiationName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-down-surf-solar-radiation-name>Hermann</fc-down-surf-solar-radiation-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Hermann", configuration.getFcDownSurfSolarRadiationName());
    }

    @Test
    public void testCreateConfiguration_fcDownSurfThermalRadiationName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-down-surf-thermal-radiation-name>Thekla</fc-down-surf-thermal-radiation-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Thekla", configuration.getFcDownSurfThermalRadiationName());
    }

    @Test
    public void testCreateConfiguration_fcSurfSolarRadiationName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-surf-solar-radiation-name>Winfried</fc-surf-solar-radiation-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Winfried", configuration.getFcSurfSolarRadiationName());
    }

    @Test
    public void testCreateConfiguration_fcSurfThermalRadiationName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-surf-thermal-radiation-name>Martin</fc-surf-thermal-radiation-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Martin", configuration.getFcSurfThermalRadiationName());
    }

    @Test
    public void testCreateConfiguration_fcTurbStressEastName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-turb-stress-east-name>whassup</fc-turb-stress-east-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("whassup", configuration.getFcTurbStressEastName());
    }

    @Test
    public void testCreateConfiguration_fcTurbStressNorthName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-turb-stress-north-name>northern_rubbish</fc-turb-stress-north-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("northern_rubbish", configuration.getFcTurbStressNorthName());
    }

    @Test
    public void testCreateConfiguration_fcEvaporationName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-evaporation-name>Eva</fc-evaporation-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Eva", configuration.getFcEvaporationName());
    }

    @Test
    public void testCreateConfiguration_fcTotalPrecipName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-total-precip-name>Toti</fc-total-precip-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("Toti", configuration.getFcTotalPrecipName());
    }

    @Test
    public void testCreateConfiguration_fcMeanSeaLevelPressureName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <fc-mean-pressure-name>press</fc-mean-pressure-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
                "    <longitude-variable-name>we need this, its mandatory</longitude-variable-name>" +
                "    <latitude-variable-name>we need this, its mandatory</latitude-variable-name>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("press", configuration.getFcMeanSeaLevelPressureName());
    }
}
