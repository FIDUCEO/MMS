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


import static org.junit.Assert.*;

import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariablesConfigurationTest {

    private VariablesConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new VariablesConfiguration();
    }

    @Test
    public void testGetRenames_emptyConfiguration() {
        final Map<String, String> renames = configuration.getRenames("hirs-n18");
        assertEquals(0, renames.size());
    }

    @Test
    public void testGetExcludes_emptyConfiguration() {
        final List<String> excludes = configuration.getExcludes("atsr-e2");
        assertEquals(0, excludes.size());
    }

    @Test
    public void testAddGetRenames() {
        final Map<String, String> renames1 = new HashMap<>();
        renames1.put("abla", "ablabla");
        renames1.put("asülz", "aschwafel");

        final Map<String, String> renames2 = new HashMap<>();
        renames2.put("bla", "blabla");
        renames2.put("sülz", "schwafel");

        configuration.addRenames("atsr-e1, atsr-e2", renames1);
        configuration.addRenames("atsr-e1, atsr-e2", renames2);

        final Map<String, String> renamesForSensor = configuration.getRenames("atsr-e2");
        assertEquals(4, renamesForSensor.size());
        assertEquals("ablabla", renamesForSensor.get("abla"));
        assertEquals("aschwafel", renamesForSensor.get("asülz"));
        assertEquals("blabla", renamesForSensor.get("bla"));
        assertEquals("schwafel", renamesForSensor.get("sülz"));
    }

    @Test
    public void testAddGetRenames_sensorNotPresent() {
        final Map<String, String> renames = new HashMap<>();
        renames.put("schnick", "schnack");

        configuration.addRenames("atsr-e1, atsr-e2", renames);

        final Map<String, String> renamesForSensor = configuration.getRenames("avhrr-n17");
        assertEquals(0, renamesForSensor.size());
    }

    @Test
    public void testAddGetExcludes() {
        final List<String> excludes = new ArrayList<>();
        excludes.add("trump");
        excludes.add("assad");

        configuration.addExcludes("avhrr-n14, avhrr-n15, avhrr-n16", excludes);

        final List<String> excludesForSensor = configuration.getExcludes("avhrr-n15");
        assertEquals(2, excludesForSensor.size());
        assertEquals("trump", excludesForSensor.get(0));
        assertEquals("assad", excludesForSensor.get(1));
    }

    @Test
    public void testAddGetExcludes_sensorNotPresent() {
        final List<String> excludes = new ArrayList<>();
        excludes.add("seehofer");

        configuration.addExcludes("avhrr-n14, avhrr-n15, avhrr-n16", excludes);

        final List<String> excludesForSensor = configuration.getExcludes("aatsr-en");
        assertEquals(0, excludesForSensor.size());
    }

    @Test
    public void testAddGetSensorRenames() throws Exception {
        configuration.addSensorRename("name_a", "name_b");
        configuration.addSensorRename("name_c", "name_d");
        configuration.addSensorRename("name_e", "name_f");
        configuration.addSensorRename("name_e", "name_g");

        final Map<String, String> sensorRenames = configuration.getSensorRenames();
        assertNotNull(sensorRenames);
        assertEquals(3, sensorRenames.size());
        assertEquals("name_b", sensorRenames.get("name_a"));
        assertEquals("name_d", sensorRenames.get("name_c"));
        assertEquals("name_g", sensorRenames.get("name_e"));
        try {
            sensorRenames.put("A", "B");
            fail("UnsupportedOperationException expected. Unmodifiable Map expected.");
        } catch (UnsupportedOperationException expected) {
        } catch (Exception expected) {
            fail("UnsupportedOperationException expected. Unmodifiable Map expected.");
        }
    }

    @Test
    public void testSetGetSeparator() throws Exception {
        final String defaultSeparator = VariablesConfiguration.DEFAULT_SEPARATOR;
        assertEquals("_", defaultSeparator);

        assertEquals(defaultSeparator, configuration.getSeparator("aba_aba"));

        configuration.setSeparator("aba_aba", "._.");
        assertEquals("._.", configuration.getSeparator("aba_aba"));
    }
}
