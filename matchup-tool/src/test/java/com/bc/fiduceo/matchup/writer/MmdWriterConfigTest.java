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

import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N3;
import static com.bc.fiduceo.matchup.writer.MmdWriterFactory.NetcdfType.N4;
import static org.junit.Assert.*;

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
    public void testSetGetCacheSize() {
        final int size_1 = 125;
        final int size_2 = 1098;

        config.setCacheSize(size_1);
        assertEquals(size_1, config.getCacheSize());

        config.setCacheSize(size_2);
        assertEquals(size_2, config.getCacheSize());
    }

    @Test
    public void testSetGetNetcdfFormat() {
        config.setNetcdfFormat("N3");
        assertEquals(N3, config.getNetcdfFormat());

        config.setNetcdfFormat("N4");
        assertEquals(N4, config.getNetcdfFormat());
    }

    @Test
    public void setNetcdfFormat_illegalString() {
        try {
            config.setNetcdfFormat("nasenmann");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testDefaultValues() {
        assertFalse(config.isOverwrite());
        assertEquals(2048, config.getCacheSize());
        assertEquals(N4, config.getNetcdfFormat());
    }

    @Test
    public void testLoad_overwrite() {
        final String configXml = "<mmd-writer-config>" +
                "    <overwrite>true</overwrite>" +
                "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertTrue(loadedConfig.isOverwrite());

        assertEquals(2048, loadedConfig.getCacheSize());
        assertEquals(N4, loadedConfig.getNetcdfFormat());
    }

    @Test
    public void testLoad_cacheSize() {
        final String configXml = "<mmd-writer-config>" +
                "    <cache-size>119</cache-size>" +
                "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertEquals(119, loadedConfig.getCacheSize());

        assertFalse(loadedConfig.isOverwrite());
        assertEquals(N4, loadedConfig.getNetcdfFormat());
    }

    @Test
    public void testLoad_netcdfFormat() {
        final String configXml = "<mmd-writer-config>" +
                "    <netcdf-format>N3</netcdf-format>" +
                "</mmd-writer-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(configXml.getBytes());

        final MmdWriterConfig loadedConfig = MmdWriterConfig.load(inputStream);
        assertEquals(N3, loadedConfig.getNetcdfFormat());
    }
}
