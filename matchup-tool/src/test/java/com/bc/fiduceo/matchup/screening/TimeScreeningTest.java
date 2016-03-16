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

package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimeScreeningTest {

    @Test
    public void testExecute_emptyMatchupCollection() {
        final MatchupCollection matchupCollection = new MatchupCollection();
        assertEquals(0, matchupCollection.getNumMatchups());

        final TimeScreening timeScreening = new TimeScreening(1200);
        final MatchupCollection result = timeScreening.execute(matchupCollection);
        assertEquals(0, result.getNumMatchups());
    }

    @Test
    public void testExecute_oneMatchupSet() {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = new ArrayList<>();
        sampleSets.add(createSampleSet(1000, 1100));
        sampleSets.add(createSampleSet(2000, 2100));
        sampleSets.add(createSampleSet(3000, 4300)); // <- this one gets removed
        sampleSets.add(createSampleSet(5000, 4800));
        sampleSets.add(createSampleSet(6000, 3800)); // <- this one gets removed
        sampleSets.add(createSampleSet(7000, 8080));
        matchupSet.setSampleSets(sampleSets);
        matchupCollection.add(matchupSet);

        assertEquals(6, matchupCollection.getNumMatchups());

        final TimeScreening timeScreening = new TimeScreening(1200);
        final MatchupCollection result = timeScreening.execute(matchupCollection);
        assertEquals(4, result.getNumMatchups());

        final MatchupSet resultSet = result.getSets().get(0);
        final List<SampleSet> resultSampleSets = resultSet.getSampleSets();
        assertTimeDelta(100, resultSampleSets.get(0));
        assertTimeDelta(100, resultSampleSets.get(1));
        assertTimeDelta(200, resultSampleSets.get(2));
        assertTimeDelta(1080, resultSampleSets.get(3));
    }

    @Test
    public void testExecute_twoMatchupSets() {
        final MatchupCollection matchupCollection = new MatchupCollection();
        MatchupSet matchupSet = new MatchupSet();
        List<SampleSet> sampleSets = new ArrayList<>();
        sampleSets.add(createSampleSet(1000, 1100));
        sampleSets.add(createSampleSet(2000, 2400));
        sampleSets.add(createSampleSet(3000, 2300)); // <- this one gets removed
        sampleSets.add(createSampleSet(5000, 4700));
        sampleSets.add(createSampleSet(6000, 4000)); // <- this one gets removed
        sampleSets.add(createSampleSet(7000, 8700)); // <- this one gets removed
        matchupSet.setSampleSets(sampleSets);
        matchupCollection.add(matchupSet);

        matchupSet = new MatchupSet();
        sampleSets = new ArrayList<>();
        sampleSets.add(createSampleSet(100000, 100500));
        sampleSets.add(createSampleSet(200000, 201100)); // <- this one gets removed
        sampleSets.add(createSampleSet(300000, 400300)); // <- this one gets removed
        sampleSets.add(createSampleSet(500000, 500000));
        sampleSets.add(createSampleSet(600000, 600001));
        sampleSets.add(createSampleSet(700000, 700600)); // <- this one gets removed
        matchupSet.setSampleSets(sampleSets);
        matchupCollection.add(matchupSet);

        assertEquals(12, matchupCollection.getNumMatchups());

        final TimeScreening timeScreening = new TimeScreening(500);
        final MatchupCollection result = timeScreening.execute(matchupCollection);
        assertEquals(6, result.getNumMatchups());

        List<SampleSet> resultSampleSets = result.getSets().get(0).getSampleSets();
        assertTimeDelta(100, resultSampleSets.get(0));
        assertTimeDelta(400, resultSampleSets.get(1));
        assertTimeDelta(300, resultSampleSets.get(2));

        resultSampleSets = result.getSets().get(1).getSampleSets();
        assertTimeDelta(500, resultSampleSets.get(0));
        assertTimeDelta(0, resultSampleSets.get(1));
        assertTimeDelta(1, resultSampleSets.get(2));
    }

    private void assertTimeDelta(int expectedTimeDelta, SampleSet sampleSet) {
        assertEquals(expectedTimeDelta, Math.abs(sampleSet.getPrimary().time - sampleSet.getSecondary().time));
    }

    private SampleSet createSampleSet(int primaryTime, int secondaryTime) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        sampleSet.setSecondary(new Sample(0, 0, 0, 0, secondaryTime));
        return sampleSet;
    }
}
