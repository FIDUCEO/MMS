/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MxD35_ReaderPluginTest {

    private MxD35_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new MxD35_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"mod35-te", "myd35-aq"};

        final String[] keys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());// we don't need a geometry factory for this test tb 2017-05-29
        assertNotNull(reader);
        assertTrue(reader instanceof MxD35_Reader);
    }
}
