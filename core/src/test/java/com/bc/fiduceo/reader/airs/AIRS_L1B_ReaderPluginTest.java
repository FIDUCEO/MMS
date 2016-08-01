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

package com.bc.fiduceo.reader.airs;

import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderPlugin;
import org.junit.*;

import static org.junit.Assert.*;

public class AIRS_L1B_ReaderPluginTest {

    private AIRS_L1B_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new AIRS_L1B_ReaderPlugin();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testImplementsReaderPluginInterface() {
        assertTrue(plugin instanceof ReaderPlugin);
    }

    @Test
    public void testGetSupportedSensorKeys() throws Exception {
        final String[] expected = new String[]{"airs-aq"};
        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testCreateReaderInstance() throws Exception {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof AIRS_L1B_Reader);
    }
}
