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

import com.bc.fiduceo.core.Sample;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchupSetTest {

    private MatchupSet matchupSet;
    private Path path;

    @Before
    public void setUp() {
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

        matchupSet.setSecondaryObservationPath(SampleSet.getOnlyOneSecondaryKey(), path);
        assertEquals(secondaryPath, matchupSet.getSecondaryObservationPath(SampleSet.getOnlyOneSecondaryKey()).toString());
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
        assertEquals(expectedSample.getLon(), primary.getLon(), 1e-8);
    }

    @Test
    public void testProperty_ProcessingVersion()  {
        assertNull(matchupSet.getPrimaryProcessingVersion());
        matchupSet.setPrimaryProcessingVersion("prim");
        assertEquals("prim", matchupSet.getPrimaryProcessingVersion());
        matchupSet.setPrimaryProcessingVersion(null);
        assertNull(matchupSet.getPrimaryProcessingVersion());

        assertNull(matchupSet.getSecondaryProcessingVersion(SampleSet.getOnlyOneSecondaryKey()));
        matchupSet.setSecondaryProcessingVersion(SampleSet.getOnlyOneSecondaryKey(), "sec");
        assertEquals("sec", matchupSet.getSecondaryProcessingVersion(SampleSet.getOnlyOneSecondaryKey()));
        matchupSet.setSecondaryProcessingVersion(SampleSet.getOnlyOneSecondaryKey(), null);
        assertNull(matchupSet.getSecondaryProcessingVersion(SampleSet.getOnlyOneSecondaryKey()));
    }

    @Test
    public void testAddSampleSets() {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(0, sampleSets.size());

        final List<SampleSet> toAddSets = new ArrayList<>();
        toAddSets.add(new SampleSet());
        toAddSets.add(new SampleSet());
        toAddSets.add(new SampleSet());

        matchupSet.addSampleSets(toAddSets);

        final List<SampleSet> secondSampleSets = matchupSet.getSampleSets();
        assertEquals(3, secondSampleSets.size());
    }

    @Test
    public void testClone_empty() {
        final MatchupSet clone = matchupSet.clone();

        assertEquals(0, clone.getNumObservations());
        assertNull(clone.getPrimaryObservationPath());
        assertNull(clone.getPrimaryProcessingVersion());

        final Set<String> secondarySensorKeys = clone.getSecondarySensorKeys();
        assertEquals(0, secondarySensorKeys.size());
    }

    @Test
    public void testClone() {
        final String primaryPath = "/data/one/file.nc";
        final Path primPath = mock(Path.class);
        when(primPath.toString()).thenReturn(primaryPath);

        final String secondaryPath = "/data/one/file.nc";
        final Path secPath = mock(Path.class);
        when(secPath.toString()).thenReturn(secondaryPath);

        final String primaryVersion = "11.a";
        final String secondaryVersion = "v2.4";
        final String secondarySensorName = "two!";

        final List<SampleSet> sampleSets = new ArrayList<>();
        final SampleSet setOne = new SampleSet();
        setOne.setPrimary(new Sample(1, 2, 3.0, 4.0, 5L));
        setOne.setSecondary("woah", new Sample(2, 3, 4.0, 5.0, 6L));
        sampleSets.add(setOne);

        final SampleSet setTwo = new SampleSet();
        setTwo.setPrimary(new Sample(3, 4, 5.0, 6.0, 7L));
        setTwo.setSecondary("heya", new Sample(4, 5, 6.0, 7.0, 8L));
        sampleSets.add(setTwo);

        matchupSet.setPrimaryObservationPath(primPath);
        matchupSet.setPrimaryProcessingVersion(primaryVersion);
        matchupSet.setSampleSets(sampleSets);
        matchupSet.setSecondaryObservationPath(secondarySensorName, secPath);
        matchupSet.setSecondaryProcessingVersion(secondarySensorName, secondaryVersion);
        final MatchupSet clone = matchupSet.clone();

        assertEquals(primaryPath, clone.getPrimaryObservationPath().toString());
        assertEquals(primaryVersion, clone.getPrimaryProcessingVersion());
        final Set<String> secondarySensorKeys = clone.getSecondarySensorKeys();
        assertEquals(1, secondarySensorKeys.size());
        assertEquals(secondaryPath, clone.getSecondaryObservationPath(secondarySensorName).toString());
        assertEquals(secondaryVersion, clone.getSecondaryProcessingVersion(secondarySensorName));

        assertEquals(2, clone.getNumObservations());
        final List<SampleSet> cloneSampleSets = clone.getSampleSets();
        final SampleSet sampleSet = cloneSampleSets.get(1);
        final Sample primary = sampleSet.getPrimary();
        assertEquals(3, primary.getX());
        assertEquals(4, primary.getY());
        assertEquals(5.0, primary.getLon(), 1e-8);
        assertEquals(6.0, primary.getLat(), 1e-8);
        assertEquals(7L, primary.getTime());
    }
}
