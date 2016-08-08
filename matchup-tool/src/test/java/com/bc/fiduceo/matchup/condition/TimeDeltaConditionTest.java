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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimeDeltaConditionTest {

    @Test
    public void testApply_emptySampleSet() {
        final TimeDeltaCondition timeDeltaCondition = new TimeDeltaCondition(500);
        final MatchupSet matchupSet = new MatchupSet();

        timeDeltaCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = new ArrayList<>();
        sampleSets.add(createSampleSet(1000, 1100));
        sampleSets.add(createSampleSet(2000, 2100));
        sampleSets.add(createSampleSet(3000, 4300)); // <- this one gets removed
        sampleSets.add(createSampleSet(5000, 4800));
        sampleSets.add(createSampleSet(6000, 3800)); // <- this one gets removed
        sampleSets.add(createSampleSet(7000, 8080));
        matchupSet.setSampleSets(sampleSets);

        final TimeDeltaCondition timeDeltaCondition = new TimeDeltaCondition(1200);
        timeDeltaCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(4, matchupSet.getNumObservations());
    }

    private SampleSet createSampleSet(int primaryTime, int secondaryTime) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        sampleSet.setSecondary(new Sample(0, 0, 0, 0, secondaryTime));
        return sampleSet;
    }
}
