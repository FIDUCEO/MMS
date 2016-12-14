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

package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AddAmsreSolarAnglesPluginTest {

    private static final String FULL_CONFIG = "<add-amsre-solar-angles>" +
            "    <sun-elevation-variable name = \"sun_up\" />" +
            "    <sun-azimuth-variable name = \"sun_angle\" />" +
            "    <earth-incidence-variable name = \"ground_angle\" />" +
            "    <earth-azimuth-variable name = \"ground_round\" />" +
            "    <sza-target-variable name = \"sun_zenith_angle\" />" +
            "    <saa-target-variable name = \"sun_azimuth_angle\" />" +
            "</add-amsre-solar-angles>";

    private AddAmsreSolarAnglesPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddAmsreSolarAnglesPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-amsre-solar-angles", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertNotNull(postProcessing);
        assertTrue(postProcessing instanceof AddAmsreSolarAngles);
    }

    @Test
    public void testCreateConfiguration_emptyConfig() throws JDOMException, IOException {
        final String XML = "<add-amsre-solar-angles>" +
                "</add-amsre-solar-angles>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            AddAmsreSolarAnglesPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_missingValues() throws JDOMException, IOException {
        String XML = "<add-amsre-solar-angles>" +
                "    <sun-elevation-variable name = \"bla\" />" +
                "<!-- missing sun azimuth -->" +
                "    <earth-incidence-variable name = \"ground_angle\" />" +
                "    <earth-azimuth-variable name = \"ground_round\" />" +
                "    <sza-target-variable name = \"sun_zenith_angle\" />" +
                "    <saa-target-variable name = \"sun_azimuth_angle\" />" +
                "</add-amsre-solar-angles>";
        Element rootElement = TestUtil.createDomElement(XML);

        try {
            AddAmsreSolarAnglesPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final AddAmsreSolarAngles.Configuration configuration = AddAmsreSolarAnglesPlugin.createConfiguration(rootElement);
        assertEquals("sun_up", configuration.sunElevationVariable);
        assertEquals("sun_angle", configuration.sunAzimuthVariable);
        assertEquals("ground_angle", configuration.earthIncidenceVariable);
        assertEquals("ground_round", configuration.earthAzimuthVariable);

        assertEquals("sun_zenith_angle", configuration.szaVariable);
        assertEquals("sun_azimuth_angle", configuration.saaVariable);
    }
}
