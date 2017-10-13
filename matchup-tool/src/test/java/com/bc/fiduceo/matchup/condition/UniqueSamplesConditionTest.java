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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class UniqueSamplesConditionTest {

    @Test
    public void testApply_emptyMatchupSet() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "erster";
        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);
        final MatchupSet matchupSet = new MatchupSet();

        // just check that it does not crash tb 2017-10-02
        condition.apply(matchupSet, new ConditionEngineContext());
    }

    @Test
    public void testApply_oneMatchup() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "first";
        configuration.associatedSensorKey = "second";

        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final MatchupSet matchupSet = new MatchupSet();
        final ArrayList<SampleSet> sampleSets = new ArrayList<>();
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(18, 19, -108.3, 23.7, 100000000));
        sampleSet.setSecondary("first", new Sample(118, 119, -108.32, 23.69, 10000003));
        sampleSets.add(sampleSet);
        matchupSet.setSampleSets(sampleSets);

        condition.apply(matchupSet, new ConditionEngineContext());
        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_twoPrimaryToOneSecondaryMatchup() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "first";
        configuration.associatedSensorKey = "second";

        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final MatchupSet matchupSet = new MatchupSet();
        final ArrayList<SampleSet> sampleSets = new ArrayList<>();

        SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(18, 19, -108.3, 23.7, 100000000));
        sampleSet.setSecondary("first", new Sample(118, 119, -108.32, 23.69, 10000003));
        sampleSets.add(sampleSet);

        sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(19, 19, -108.31, 23.69, 100000001));    // this one is closer in space
        sampleSet.setSecondary("first", new Sample(118, 119, -108.32, 23.69, 10000003));
        sampleSets.add(sampleSet);

        matchupSet.setSampleSets(sampleSets);

        condition.apply(matchupSet, new ConditionEngineContext());
        assertEquals(1, matchupSet.getNumObservations());
        final SampleSet remaining = matchupSet.getSampleSets().get(0);
        assertEquals(19, remaining.getPrimary().getX());
        assertEquals(118, remaining.getSecondary("first").getX());
    }

    @Test
    public void testApply_twoSecondaryToOnePrimaryMatchup() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "second";
        configuration.associatedSensorKey = "first";

        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final MatchupSet matchupSet = new MatchupSet();
        final ArrayList<SampleSet> sampleSets = new ArrayList<>();

        SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(18, 19, -108.3, 23.7, 100000000));
        sampleSet.setSecondary("first", new Sample(119, 119, -108.35, 23.71, 10000004));
        sampleSets.add(sampleSet);

        sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(18, 19, -108.3, 23.7, 100000000));
        sampleSet.setSecondary("first", new Sample(118, 119, -108.32, 23.69, 10000003));
        sampleSets.add(sampleSet);

        matchupSet.setSampleSets(sampleSets);

        condition.apply(matchupSet, new ConditionEngineContext());
        assertEquals(1, matchupSet.getNumObservations());
        final SampleSet remaining = matchupSet.getSampleSets().get(0);
        assertEquals(18, remaining.getPrimary().getX());
        assertEquals(118, remaining.getSecondary("first").getX());
    }

    @Test
    public void testGetReference_secondary() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "reference";
        configuration.associatedSensorKey = "associated";
        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(1, 2, 3, 4, 5));
        sampleSet.setSecondary("reference", new Sample(6, 7, 8, 9, 10));

        final Sample reference = condition.getReference(sampleSet);
        assertEquals(6, reference.getX());
    }

    @Test
    public void testGetReference_primary() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "primary";
        configuration.associatedSensorKey = "reference";
        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(1, 2, 3, 4, 5));
        sampleSet.setSecondary("reference", new Sample(6, 7, 8, 9, 10));

        final Sample reference = condition.getReference(sampleSet);
        assertEquals(1, reference.getX());
    }

    @Test
    public void testGetAssociated_primary() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "reference";
        configuration.associatedSensorKey = "primary";
        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(1, 2, 3, 4, 5));
        sampleSet.setSecondary("reference", new Sample(6, 7, 8, 9, 10));

        final Sample reference = condition.getAssociated(sampleSet);
        assertEquals(1, reference.getX());
    }

    @Test
    public void testGetAssociated_secondary() {
        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
        configuration.referenceSensorKey = "primary";
        configuration.associatedSensorKey = "reference";
        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);

        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(1, 2, 3, 4, 5));
        sampleSet.setSecondary("reference", new Sample(6, 7, 8, 9, 10));

        final Sample reference = condition.getAssociated(sampleSet);
        assertEquals(6, reference.getX());
    }

    @Test
    public void testCreateKey() {
        final Sample sample = new Sample(34, 66, 1, 2, 3);

        assertEquals("34_66", UniqueSamplesCondition.createKey(sample));
    }

    // @todo 1 tb/tb continue here 2017-10-02
//    @Test
//    public void testCreateSampleSet() {
//        final Sample reference = new Sample(1, 2, 3, 4, 5);
//        final Sample associated = new Sample(6, 7, 8, 9, 10);
//
//        final UniqueSamplesCondition.Configuration configuration = new UniqueSamplesCondition.Configuration();
//        configuration.referenceSensorKey = "primary";
//        configuration.associatedSensorKey = "reference";
//        final UniqueSamplesCondition condition = new UniqueSamplesCondition(configuration);
//    }
}
