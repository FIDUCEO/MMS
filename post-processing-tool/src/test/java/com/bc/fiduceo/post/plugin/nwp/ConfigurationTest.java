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


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationTest {

    // @todo 1 tb/tb add IO Test class for confguration checking 2016-12-22

    private Configuration config;

    @Before
    public void setUp() {
        config = new Configuration();
    }

    @Test
    public void testSetGetDeleteOnExit() {
        config.setDeleteOnExit(false);
        assertFalse(config.isDeleteOnExit());

        config.setDeleteOnExit(true);
        assertTrue(config.isDeleteOnExit());
    }

    @Test
    public void testSetGetCDOHome() {
        final String cdoHome = "/here/is/it";

        config.setCDOHome(cdoHome);
        assertEquals(cdoHome, config.getCDOHome());
    }

    @Test
    public void testSetGetNWPAuxDir() {
        final String nwpAuxDir = "/here/are/the/files";

        config.setNWPAuxDir(nwpAuxDir);
        assertEquals(nwpAuxDir, config.getNWPAuxDir());
    }

    @Test
    public void testSetGetAnalysisSteps() {
        config.setAnalysisSteps(19);
        assertEquals(19, config.getAnalysisSteps());
    }

    @Test
    public void testSetGetForecastSteps() {
        config.setForecastSteps(31);
        assertEquals(31, config.getForecastSteps());
    }

    @Test
    public void testDefaultValues() {
        assertTrue(config.isDeleteOnExit());
        assertNull(config.getCDOHome());
        assertEquals(17, config.getAnalysisSteps());
        assertEquals(33, config.getForecastSteps());
    }
}
