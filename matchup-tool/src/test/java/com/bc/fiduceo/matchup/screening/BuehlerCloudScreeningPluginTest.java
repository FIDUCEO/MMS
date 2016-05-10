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

package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.TestUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BuehlerCloudScreeningPluginTest {

    private BuehlerCloudScreeningPlugin plugin;

    @Before
    public void setUp(){
        plugin = new BuehlerCloudScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {

        assertEquals("buehler-cloud", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<buehler-cloud/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
        assertTrue(screening instanceof BuehlerCloudScreening);
    }

    @Test
    public void testCreateConfiguration_primaryNarrowChannel() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <primary-narrow-channel name=\"btemp_ch18\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("btemp_ch18", configuration.primaryNarrowChannelName);
    }

    @Test
    public void testCreateConfiguration_primaryWideChannel() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <primary-wide-channel name=\"btemp_ch20\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("btemp_ch20", configuration.primaryWideChannelName);
    }

    @Test
    public void testCreateConfiguration_primaryVZAVariable() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <primary-vza name=\"SZA\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("SZA", configuration.primaryVZAVariableName);
    }

    @Test
    public void testCreateConfiguration_secondaryNarrowChannel() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <secondary-narrow-channel name=\"btemp_ch3\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("btemp_ch3", configuration.secondaryNarrowChannelName);
    }

    @Test
    public void testCreateConfiguration_secondaryWideChannel() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <secondary-wide-channel name=\"btemp_ch4\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("btemp_ch4", configuration.secondaryWideChannelName);
    }

    @Test
    public void testCreateConfiguration_secondaryVZAVariable() throws JDOMException, IOException {
        final String XML = "<buehler-cloud>" +
                "  <secondary-vza name=\"2ndSZA\" />" +
                "</buehler-cloud>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final BuehlerCloudScreening.Configuration configuration = BuehlerCloudScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("2ndSZA", configuration.secondaryVZAVariableName);
    }
}
