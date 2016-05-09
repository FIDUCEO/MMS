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
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AngularScreeningPluginTest {

    @Test
    public void testGetScreeningName() {
        final AngularScreeningPlugin plugin = new AngularScreeningPlugin();

        assertEquals("angular", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<angular/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreeningPlugin plugin = new AngularScreeningPlugin();
        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
    }

    @Test
    public void testCreateConfiguration_primaryName() throws JDOMException, IOException {
        final String XML = "<angular>" +
                "  <primaryVZAVariable name=\"zenith_angle\" />" +
                "</angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreening.Configuration configuration = AngularScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("zenith_angle", configuration.primaryVariableName);
    }

    @Test
    public void testCreateConfiguration_secondaryName() throws JDOMException, IOException {
        final String XML = "<angular>" +
                "  <secondaryVZAVariable name=\"2nd_angle\" />" +
                "</angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreening.Configuration configuration = AngularScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("2nd_angle", configuration.secondaryVariableName);
    }

    @Test
    public void testCreateConfiguration_primaryThreshold() throws JDOMException, IOException {
        final String XML = "<angular>" +
                "  <maxPrimaryVZA>" +
                "    11.3" +
                "  </maxPrimaryVZA>" +
                "</angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreening.Configuration configuration = AngularScreeningPlugin.createConfiguration(rootElement);
        assertEquals(11.3, configuration.maxPrimaryVZA, 1e-8);
        assertTrue(configuration.usePrimary);
    }

    @Test
    public void testCreateConfiguration_secondaryThreshold() throws JDOMException, IOException {
        final String XML = "<angular>" +
                "  <maxSecondaryVZA>" +
                "    12.4" +
                "  </maxSecondaryVZA>" +
                "</angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreening.Configuration configuration = AngularScreeningPlugin.createConfiguration(rootElement);
        assertEquals(12.4, configuration.maxSecondaryVZA, 1e-8);
        assertTrue(configuration.useSecondary);
    }

    @Test
    public void testCreateConfiguration_angularDifference() throws JDOMException, IOException {
        final String XML = "<angular>" +
                "  <maxAngleDelta>" +
                "    13.5" +
                "  </maxAngleDelta>" +
                "</angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final AngularScreening.Configuration configuration = AngularScreeningPlugin.createConfiguration(rootElement);
        assertEquals(13.5, configuration.maxAngleDelta, 1e-8);
        assertTrue(configuration.useDelta);
    }
}
