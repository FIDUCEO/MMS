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

package com.bc.fiduceo.matchup;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchupSetTest {

    private MatchupSet matchupSet;
    private Path path;

    @Before
    public void setUp() throws Exception {
        matchupSet = new MatchupSet();
        path = mock(Path.class);
    }

    @Test
    public void testSetGetPrimaryObservationPath() {
        final String primaryPath = "/the/primary/file.nc";
        when(path.toString()).thenReturn(primaryPath);

        matchupSet.setPrimaryObservationPath(path);
        assertEquals(primaryPath, matchupSet.getPrimaryObservationPath().toString());
    }

    @Test
    public void testSetGetSecondaryObservationPath() {
        final String secondaryPath = "/the/secondary/file.nc";
        when(path.toString()).thenReturn(secondaryPath);

        matchupSet.setSecondaryObservationPath(path);
        assertEquals(secondaryPath, matchupSet.getSecondaryObservationPath().toString());
    }

    @Test
    public void testAddPrimary() {
        final Sample expectedSample = new Sample(2, 3, 4, 5, 6L);

        assertEquals(0, matchupSet.getNumObservations());

        matchupSet.addPrimary(expectedSample);

        assertEquals(1, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final SampleSet sampleSet = sampleSets.get(0);
        final Sample primary = sampleSet.getPrimary();
        assertEquals(expectedSample.lon, primary.lon, 1e-8);
    }

    @Test
    public void testProperty_ProcessingVersion() throws Exception {
        assertNull(matchupSet.getPrimaryProcessingVersion());
        matchupSet.setPrimaryProcessingVersion("prim");
        assertEquals("prim", matchupSet.getPrimaryProcessingVersion());
        matchupSet.setPrimaryProcessingVersion(null);
        assertNull(matchupSet.getPrimaryProcessingVersion());

        assertNull(matchupSet.getSecondaryProcessingVersion());
        matchupSet.setSecondaryProcessingVersion("sec");
        assertEquals("sec", matchupSet.getSecondaryProcessingVersion());
        matchupSet.setSecondaryProcessingVersion(null);
        assertNull(matchupSet.getSecondaryProcessingVersion());
    }
}
