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

import static org.junit.Assert.*;

public class HIRS_LZADeltaScreeningPluginTest {

    private HIRS_LZADeltaScreeningPlugin plugin;

    @Before
    public void setUp() {
        plugin = new HIRS_LZADeltaScreeningPlugin();
    }

    @Test
    public void testGetScreeningName() {
        assertEquals("hirs-lza-delta", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<hirs-lza-delta/>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
        assertTrue(screening instanceof HIRS_LZADeltaScreening);
    }

    @Test
    public void testCreateConfiguration() throws JDOMException, IOException {
        final String XML = "<hirs-lza-delta>" +
                "<max-lza-delta>10.0</max-lza-delta>" +
                "</hirs-lza-delta>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final HIRS_LZADeltaScreening.Configuration configuration = HIRS_LZADeltaScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals(10.0, configuration.maxLzaDelta, 1e-8);
    }

    @Test
    public void testCreateConfiguration_noDeltaValue() throws JDOMException, IOException {
        final String XML = "<hirs-lza-delta/>";

        final Element rootElement = TestUtil.createDomElement(XML);

        final HIRS_LZADeltaScreening.Configuration configuration = HIRS_LZADeltaScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals(0.0, configuration.maxLzaDelta, 1e-8);
    }
}
