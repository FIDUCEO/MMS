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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OverlapRemoveConditionTest {

    private OverlapRemoveCondition primaryCondition;
    private OverlapRemoveCondition secondaryCondition;
    private MatchupSet matchupSet;
    private ConditionEngineContext context;

    @Before
    public void setUp() {
        primaryCondition = new OverlapRemoveCondition(true);
        secondaryCondition = new OverlapRemoveCondition(false);
        matchupSet = new MatchupSet();
        context = new ConditionEngineContext();
    }

    @Test
    public void testRemove_emptyMatchupSet() {
        primaryCondition.apply(matchupSet, context);
        assertEquals(0, matchupSet.getNumObservations());

        secondaryCondition.apply(matchupSet, new ConditionEngineContext());
        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_oneMatchup_primary() {
        addSampleSet(108, 346, 3567, 12056);

        primaryCondition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_oneMatchup_secondary() {
        addSampleSet(22, 105, 1944, 22055);

        secondaryCondition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_twoMatchups_primary_nonOverlapping() {
        addSampleSet(108, 346, 3567, 12056);
        addSampleSet(208, 446, 3567, 12056);

        primaryCondition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testRemove_twoMatchups_secondary_nonOverlapping() {
        addSampleSet(108, 346, 3567, 12056);
        addSampleSet(108, 346, 3577, 12106);

        secondaryCondition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    private void addSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(primaryX, primaryY, -22.5, 18.98, 111027));
        sampleSet.setSecondary(new Sample(secondaryX, secondaryY, -23.5, 19.98, 121027));
        sampleSets.add(sampleSet);
    }
}
