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

public class AngularCosineProportionScreeningPluginTest {

    private AngularCosineProportionScreeningPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AngularCosineProportionScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("angular-cosine-proportion", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<angular-cosine-proportion/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
    }

    @Test
    public void testCreateConfiguration_primaryName() throws JDOMException, IOException {
        final String XML = "<angular-cosine-proportion>" +
                "  <primary-variable name=\"the_angle\" />" +
                "</angular-cosine-proportion>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularCosineProportionScreening.Configuration configuration = AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("the_angle", configuration.primaryVariableName);
    }

    @Test
    public void testCreateConfiguration_secondaryName() throws JDOMException, IOException {
        final String XML = "<angular-cosine-proportion>" +
                "  <secondary-variable name=\"other_angle\" />" +
                "</angular-cosine-proportion>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularCosineProportionScreening.Configuration configuration = AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("other_angle", configuration.secondaryVariableName);
    }

    @Test
    public void testCreateConfiguration_threshold() throws JDOMException, IOException {
        final String XML = "<angular-cosine-proportion>" +
                "  <threshold>0.028</threshold>" +
                "</angular-cosine-proportion>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularCosineProportionScreening.Configuration configuration = AngularCosineProportionScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals(0.028, configuration.threshold, 1e-8);
    }
}
