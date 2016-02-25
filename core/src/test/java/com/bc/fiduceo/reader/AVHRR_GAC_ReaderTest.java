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

package com.bc.fiduceo.reader;


import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AVHRR_GAC_ReaderTest {

    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() {
        reader = new AVHRR_GAC_Reader();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] sensorKeys = reader.getSupportedSensorKeys();
        assertNotNull(sensorKeys);
        assertEquals(16, sensorKeys.length);
        assertEquals("avhrr-n06", sensorKeys[0]);
        assertEquals("avhrr-n14", sensorKeys[8]);
        assertEquals("avhrr-m02", sensorKeys[15]);
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("[0-9]{14}-ESACCI-L1C-AVHRR([0-9]{2}|MTA)_G-fv\\d\\d.\\d.nc", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc");
        assertTrue(matcher.matches());
    }
}
