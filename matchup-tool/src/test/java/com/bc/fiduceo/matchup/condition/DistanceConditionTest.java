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
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DistanceConditionTest {

    @Test
    public void testApply_emptySampleSet() {
        final DistanceCondition distanceCondition = new DistanceCondition(2.82);
        final MatchupSet matchupSet = new MatchupSet();

        distanceCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(-11.0, 22.0, -11.002, 21.998, null));
        sampleSets.add(createSampleSet(-11.5, 22.5, -10.0, 20.0, null));  // <- this one gets removed
        sampleSets.add(createSampleSet(-12.0, 23.0, -12.002, 22.998, null));

        final DistanceCondition distanceCondition = new DistanceCondition(5.08);
        distanceCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_withSecondarySensorName() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final String secondarySensorName = "secName";
        sampleSets.add(createSampleSet(-11.0, 22.0, -11.002, 21.998, secondarySensorName));
        sampleSets.add(createSampleSet(-11.5, 22.5, -10.0, 20.0, secondarySensorName));  // <- this one gets removed
        sampleSets.add(createSampleSet(-12.0, 23.0, -12.002, 22.998, secondarySensorName));


        final DistanceCondition distanceCondition = new DistanceCondition(5.08);
        distanceCondition.setSecondarySensorName(secondarySensorName);
        distanceCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_SecondarySensorNameIsSetInCondition_butNotInSampleSets() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final String secondarySensorName = "secName";
        final String differentSecSensorName = "different";
        sampleSets.add(createSampleSet(-11.0, 22.0, -11.002, 21.998, differentSecSensorName));

        final DistanceCondition distanceCondition = new DistanceCondition(5.08);
        distanceCondition.setSecondarySensorName(secondarySensorName);

        try {
            distanceCondition.apply(matchupSet, new ConditionEngineContext());
            fail("NullPointerException expected");
        } catch (NullPointerException expected) {
            assertEquals(null, expected.getMessage());
        }
    }

    private SampleSet createSampleSet(double primaryLon, double primaryLat, double secondaryLon, double secondaryLat, String secondarySensorName) {
        if (secondarySensorName == null) {
            secondarySensorName = SampleSet.getOnlyOneSecondaryKey();
        }

        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, primaryLon, primaryLat, 0));
        sampleSet.setSecondary(secondarySensorName, new Sample(0, 0, secondaryLon, secondaryLat, 0));
        return sampleSet;
    }
}
