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
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("big_ben", configuration.getTimeVariableName());
    }

    @Test
    public void testCreateConfiguration_anSeaIceFractionName() throws JDOMException, IOException {
        final String XML = "<nwp>" +
                "    <an-sea-ice-fraction-name>nogger</an-sea-ice-fraction-name>" +
                "" +
                "    <cdo-home>we need this, its mandatory</cdo-home>" +
                "    <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>" +
                "    <time-variable-name>we need this, its mandatory</time-variable-name>" +
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
                "</nwp>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Configuration configuration = NwpPostProcessingPlugin.createConfiguration(rootElement);
        assertEquals("temperature", configuration.getFcSSTName());
    }
}
