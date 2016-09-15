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

public class AtsrAngularScreeningPluginTest {

    private AtsrAngularScreeningPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AtsrAngularScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("atsr-angular", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<atsr-angular/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
        assertTrue(screening instanceof AtsrAngularScreening);
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final String XML = "<atsr-angular>" +
                "<angle-delta-nadir>10.0</angle-delta-nadir>" +
                "<angle-delta-fward>1.0</angle-delta-fward>" +
                "</atsr-angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        AtsrAngularScreening.Configuration configuration = AtsrAngularScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals(10.0, configuration.angleDeltaNadir, 1e-8);
        assertEquals(1.0, configuration.angleDeltaFward, 1e-8);
    }

    @Test
    public void testCreateConfiguration_noValueSet() throws JDOMException, IOException {
        final String XML = "<atsr-angular>" +
                "</atsr-angular>";
        final Element rootElement = TestUtil.createDomElement(XML);

        AtsrAngularScreening.Configuration configuration = AtsrAngularScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals(0.0, configuration.angleDeltaNadir, 1e-8);
        assertEquals(0.0, configuration.angleDeltaFward, 1e-8);
    }
}
