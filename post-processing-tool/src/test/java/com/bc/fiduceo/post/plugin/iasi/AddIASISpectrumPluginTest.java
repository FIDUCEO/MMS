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

package com.bc.fiduceo.post.plugin.iasi;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessing;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AddIASISpectrumPluginTest {

    private AddIASISpectrumPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddIASISpectrumPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-iasi-spectrum", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = createFullConfigElement();

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement);
        assertTrue(postProcessing instanceof AddIASISpectrum);
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final Element rootElement = createFullConfigElement();

        final AddIASISpectrum.Configuration configuration = AddIASISpectrum.createConfiguration(rootElement);
        assertEquals("schnecktrum", configuration.targetVariableName);
        assertEquals("reffi", configuration.referenceVariableName);
        assertEquals("exxi", configuration.xCoordinateName);
        assertEquals("yppsi", configuration.yCoordinateName);
        assertEquals("fileName", configuration.filenameVariableName);
        assertEquals("proc-ver", configuration.processingVersionVariableName);
    }

    private Element createFullConfigElement() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        return TestUtil.createDomElement(configXML);
    }

    @Test
    public void testCreateConfiguration_missingTargetVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingTargetVariableNameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable  />" +
                "    <reference-variable name=\"reffi\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingReferenceVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingReferenceVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable  />" +
                "    <x-variable name=\"exxi\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingXVariable_nameAtribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable  />" +
                "    <y-variable name=\"yppsi\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingYVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingYVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  />" +
                "    <file-name-variable name=\"fileName\" />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingFileNameVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingFileNameVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable />" +
                "    <processing-version-variable name=\"proc-ver\" />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingProcessingVersionVariable() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable name=\"filius\"/>" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testCreateConfiguration_missingProcessingVersionVariable_nameAttribute() throws JDOMException, IOException {
        final String configXML = "<add-iasi-spectrum>" +
                "    <target-variable name=\"schnecktrum\" />" +
                "    <reference-variable name=\"ref\" />" +
                "    <x-variable name=\"echs\" />" +
                "    <y-variable  name=\"ypps\"/>" +
                "    <file-name-variable name=\"filius\"/>" +
                "    <processing-version-variable  />" +
                "</add-iasi-spectrum>";

        final Element rootElement = TestUtil.createDomElement(configXML);

        try {
            AddIASISpectrum.createConfiguration(rootElement);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }
}
