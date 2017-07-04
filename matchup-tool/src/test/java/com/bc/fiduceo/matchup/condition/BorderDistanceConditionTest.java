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

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BorderDistanceConditionTest {

    @Test
    public void testApply_emptySampleSet_primary() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.usePrimary = true;
        configuration.primary_x = 3;
        configuration.primary_y = 2;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();

        final ConditionEngineContext context = createContext();
        condition.apply(matchupSet, context);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_emptySampleSet_secondary() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.useSecondary = true;
        configuration.secondary_x = 3;
        configuration.secondary_y = 2;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();

        final ConditionEngineContext context = createContext();
        condition.apply(matchupSet, context);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlyPrimary_leftUpper() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.usePrimary = true;
        configuration.primary_x = 3;
        configuration.primary_y = 4;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(1, 675, 45, 109));   // <- this one gets removed primary x too small
        sampleSets.add(createSampleSet(34, 81, 38, 2005));
        sampleSets.add(createSampleSet(23, 2, 55, 32));     // <- this one gets removed primary y too small

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlyPrimary_rightLower() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.usePrimary = true;
        configuration.primary_x = 3;
        configuration.primary_y = 5;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(98, 675, 45, 109));   // <- this one gets removed primary x too large
        sampleSets.add(createSampleSet(34, 81, 18, 2005));
        sampleSets.add(createSampleSet(23, 2996, 55, 32));     // <- this one gets removed primary y too large

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_both() {
        final BorderDistanceCondition.Configuration primary;
        primary = new BorderDistanceCondition.Configuration();
        primary.usePrimary = true;
        primary.primary_x = 5;
        primary.primary_y = 5;

        final BorderDistanceCondition.Configuration secondary;
        secondary = new BorderDistanceCondition.Configuration();
        secondary.useSecondary = true;
        secondary.secondaryName = SampleSet.getOnlyOneSecondaryKey();
        secondary.secondary_x = 5;
        secondary.secondary_y = 5;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Arrays.asList(primary, secondary));
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(33, 675, 45, 109));
        sampleSets.add(createSampleSet(34, 2996, 3, 205)); // <- this one gets removed primary y and secondary x too small
        sampleSets.add(createSampleSet(23, 108, 55, 32));

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlySecondary_leftUpper() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.useSecondary = true;
        configuration.secondary_x = 4;
        configuration.secondary_y = 2;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(13, 675, 3, 109));   // <- this one gets removed x too small
        sampleSets.add(createSampleSet(34, 81, 14, 1));  // <- this one gets removed y too small
        sampleSets.add(createSampleSet(23, 23, 55, 32));

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_onlySecondary_rightLower() {
        final BorderDistanceCondition.Configuration configuration = new BorderDistanceCondition.Configuration();
        configuration.useSecondary = true;
        configuration.secondary_x = 4;
        configuration.secondary_y = 4;

        final BorderDistanceCondition condition = new BorderDistanceCondition(Collections.singletonList(configuration));
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(62, 675, 45, 109));
        sampleSets.add(createSampleSet(34, 81, 108, 205));      // <- this one gets removed x too large
        sampleSets.add(createSampleSet(23, 435, 55, 2998));     // <- this one gets removed y too large

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_no_condition_set() {
        final BorderDistanceCondition condition = new BorderDistanceCondition(new ArrayList<>());
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(62, 675, 45, 109));
        sampleSets.add(createSampleSet(34, 81, 108, 205));
        sampleSets.add(createSampleSet(23, 435, 55, 2998));

        final ConditionEngineContext context = createContext();

        condition.apply(matchupSet, context);

        assertEquals(3, matchupSet.getNumObservations());
    }

    private ConditionEngineContext createContext() {
        final ConditionEngineContext context = new ConditionEngineContext();
        context.setPrimarySize(new Dimension("", 100, 3000));
        context.setSecondarySize(new Dimension("", 100, 3000));
        return context;
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(primaryX, primaryY, 0.0, 0.0, 0));
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), new Sample(secondaryX, secondaryY, 0.0, 0.0, 0));
        return sampleSet;
    }
}
