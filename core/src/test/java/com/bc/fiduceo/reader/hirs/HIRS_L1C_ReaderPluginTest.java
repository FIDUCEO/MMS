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

package com.bc.fiduceo.reader.hirs;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HIRS_L1C_ReaderPluginTest {

    private HIRS_L1C_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new HIRS_L1C_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys(){
        final String[] expected = {"hirs-ma", "hirs-mb", "hirs-n06", "hirs-n07", "hirs-n08", "hirs-n09", "hirs-n10", "hirs-n11", "hirs-n12", "hirs-n14", "hirs-n15", "hirs-n16", "hirs-n17", "hirs-n18", "hirs-n19", "hirs-tn"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        // we dont't need a GeometryFactory for this test tb 2016-08-01
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof HIRS_L1C_Reader);
    }
}
