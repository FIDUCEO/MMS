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

package com.bc.fiduceo.post.plugin;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddAmsr2ScanDataQualityPluginTest {

    private AddAmsr2ScanDataQualityPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddAmsr2ScanDataQualityPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-amsr2-scan-data-quality", plugin.getPostProcessingName());
    }
}
