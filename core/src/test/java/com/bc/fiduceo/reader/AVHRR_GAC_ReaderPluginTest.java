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

public class AVHRR_GAC_ReaderPluginTest {

    private AVHRR_GAC_ReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AVHRR_GAC_ReaderPlugin();
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader();
        assertNotNull(reader);
        assertTrue(reader instanceof AVHRR_GAC_Reader);
    }

    @Test
    public void testGetSupportedSensorKeys(){
        final String[] expected = {"avhrr-n06", "avhrr-n07", "avhrr-n08", "avhrr-n09", "avhrr-n10", "avhrr-n11", "avhrr-n12", "avhrr-n13", "avhrr-n14", "avhrr-n15", "avhrr-n16", "avhrr-n17", "avhrr-n18", "avhrr-n19", "avhrr-m01", "avhrr-m02"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }

}
