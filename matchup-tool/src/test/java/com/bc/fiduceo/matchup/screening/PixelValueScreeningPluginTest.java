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
import static org.junit.Assert.assertNull;

public class PixelValueScreeningPluginTest {

    private PixelValueScreeningPlugin plugin;

    @Before
    public void setUp() {
        plugin = new PixelValueScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("pixel-value", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<pixel-value/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final String XML = "<pixel-value>" +
                "<primary_expression>radiance_10 > 13.678</primary_expression>" +
                "<secondary_expression>flags != 26</secondary_expression>" +
                "</pixel-value>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final PixelValueScreening.Configuration configuration = PixelValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("radiance_10 > 13.678", configuration.primaryExpression);
        assertEquals("flags != 26", configuration.secondaryExpression);
    }

    @Test
    public void testCreateConfiguration_missingPrimaryExpression() throws JDOMException, IOException {
        final String XML = "<pixel-value>" +
                "<secondary_expression>flags != 26</secondary_expression>" +
                "</pixel-value>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final PixelValueScreening.Configuration configuration = PixelValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryExpression);
        assertEquals("flags != 26", configuration.secondaryExpression);
    }

    @Test
    public void testCreateConfiguration_missingSecondaryExpression() throws JDOMException, IOException {
        final String XML = "<pixel-value>" +
                "<primary_expression>radiance_10 > 13.678</primary_expression>" +
                "</pixel-value>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final PixelValueScreening.Configuration configuration = PixelValueScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("radiance_10 > 13.678", configuration.primaryExpression);
        assertNull(configuration.secondaryExpression);
    }
}