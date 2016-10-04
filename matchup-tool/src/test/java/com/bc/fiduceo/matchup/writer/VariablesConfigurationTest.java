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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VariablesConfigurationTest {

    private VariablesConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new VariablesConfiguration();
    }

    @Test
    public void testGetRenames_emptyConfiguration() {
        final List<VariableRename> renames = configuration.getRenames("hirs-n18");
        assertEquals(0, renames.size());
    }

    @Test
    public void testGetExcludes_emptyConfiguration() {
        final List<VariableExclude> excludes = configuration.getExcludes("atsr-e2");
        assertEquals(0, excludes.size());
    }

    @Test
    public void testAddGetRenames() {
        final List<VariableRename> renames = new ArrayList<>();
        renames.add(new VariableRename("bla", "blabla"));
        renames.add(new VariableRename("sülz", "schwafel"));

        configuration.addRenames("atsr-e1, atsr-e2", renames);

        final List<VariableRename> renamesForSensor = configuration.getRenames("atsr-e2");
        assertEquals(2, renamesForSensor.size());
        assertEquals("bla", renamesForSensor.get(0).getSourceName());
        assertEquals("sülz", renamesForSensor.get(1).getSourceName());
    }

    @Test
    public void testAddGetRenames_sensorNotPresent() {
        final List<VariableRename> renames = new ArrayList<>();
        renames.add(new VariableRename("schnick", "schnack"));

        configuration.addRenames("atsr-e1, atsr-e2", renames);

        final List<VariableRename> renamesForSensor = configuration.getRenames("avhrr-n17");
        assertEquals(0, renamesForSensor.size());
    }

    @Test
    public void testAddGetExcludes() {
        final List<VariableExclude> excludes = new ArrayList<>();
        excludes.add(new VariableExclude("trump"));
        excludes.add(new VariableExclude("assad"));

        configuration.addExcludes("avhrr-n14, avhrr-n15, avhrr-n16", excludes);

        final List<VariableExclude> excludesForSensor = configuration.getExcludes("avhrr-n15");
        assertEquals(2, excludesForSensor.size());
        assertEquals("trump", excludesForSensor.get(0).getSourceName());
        assertEquals("assad", excludesForSensor.get(1).getSourceName());
    }

    @Test
    public void testAddGetExcludes_sensorNotPresent() {
        final List<VariableExclude> excludes = new ArrayList<>();
        excludes.add(new VariableExclude("seehofer"));

        configuration.addExcludes("avhrr-n14, avhrr-n15, avhrr-n16", excludes);

        final List<VariableExclude> excludesForSensor = configuration.getExcludes("aatsr-en");
        assertEquals(0, excludesForSensor.size());
    }
}
