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

package com.bc.fiduceo.matchup.writer;


import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MmdWriterConfigTest {

    private MmdWriterConfig config;

    @Before
    public void setUp() {
        config = new MmdWriterConfig();
    }

    @Test
    public void testSetIsOverwrite() {
        config.setOverwrite(true);
        assertTrue(config.isOverwrite());

        config.setOverwrite(false);
        assertFalse(config.isOverwrite());
    }

    @Test
    public void testIsOverwriteDefaultValue() {
        assertFalse(config.isOverwrite());
    }

    @Test
    public void testLoad_overwrite() {
        final String configXml = "<mmd-writer-config>" +
                "    <overwrite>true</overwrite>" +
                "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertTrue(loadedConfig.isOverwrite());
    }
}
