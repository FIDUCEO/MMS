/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.*;

import java.io.IOException;

public class AddAmsr2ScanDataQualityPluginTest {

    private static final String FULL_CONFIG = "<add-amsr2-scan-data-quality>" +
            "    <filename-variable name = \"amsr2-gcw1_file_name\" />" +
            "    <processing-version-variable name = \"amsr2-gcw1_processing_version\" />" +
            "    <y-variable name = \"amsr2-gcw1_y\" />" +
            "    <target-variable name = \"amsr2-gcw1_Scan_Data_Quality\" />" +
            "</add-amsr2-scan-data-quality>";

    private AddAmsr2ScanDataQualityPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddAmsr2ScanDataQualityPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-amsr2-scan-data-quality", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertNotNull(postProcessing);
        assertTrue(postProcessing instanceof AddAmsr2ScanDataQuality);
    }

    @Test
    public void testCreateConfiguration_emptyConfig() throws JDOMException, IOException {
        final String XML = "<add-amsr2-scan-data-quality>" +
                "</add-amsr2-scan-data-quality>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            AddAmsr2ScanDataQualityPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), containsString("target-variable"));
        }
    }

    @Test
    public void testCreateConfiguration_missingValue() throws JDOMException, IOException {
        final String XML = "<add-amsr2-scan-data-quality>" +
                "    <target-variable/>" +
                "</add-amsr2-scan-data-quality>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            AddAmsr2ScanDataQualityPlugin.createConfiguration(rootElement);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            String lowerMessage = expected.getMessage().toLowerCase();
            assertThat(lowerMessage, containsString("'name'"));
            assertThat(lowerMessage, containsString("attribute"));
            assertThat(lowerMessage, containsString("target-variable"));
        }
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(FULL_CONFIG);

        final AddAmsr2ScanDataQuality.Configuration configuration = AddAmsr2ScanDataQualityPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("amsr2-gcw1_Scan_Data_Quality", configuration.targetVariableName);
        assertEquals("amsr2-gcw1_file_name", configuration.filenameVariableName);
        assertEquals("amsr2-gcw1_processing_version", configuration.processingVersionVariableName);
        assertEquals("amsr2-gcw1_y", configuration.yCoordinateVariableName);
    }
}
