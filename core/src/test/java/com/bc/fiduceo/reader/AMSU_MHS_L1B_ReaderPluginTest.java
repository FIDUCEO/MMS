/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.*;

public class AMSU_MHS_L1B_ReaderPluginTest {

    private AMSU_MHS_L1B_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new AMSU_MHS_L1B_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKey() throws Exception {
        final String[] expected = {"amsub-tn", "amsub-n06", "amsub-n07", "amsub-n08", "amsub-n09", "amsub-n10", "amsub-n11", "amsub-n12", "amsub-n14", "amsub-n15", "amsub-n16", "amsub-n17", "amsub-n18", "amsub-n19"};

        final String[] sensorKeys= plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader();
        assertNotNull(reader);
        assertTrue(reader instanceof AMSU_MHS_L1B_Reader);
    }
}
