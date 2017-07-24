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

import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

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
        sampleSets.add(createSampleSet(1000, 1100, null));
        sampleSets.add(createSampleSet(2000, 2100, null));
        sampleSets.add(createSampleSet(3000, 4300, null)); // <- this one gets removed
        sampleSets.add(createSampleSet(5000, 4800, null));
        sampleSets.add(createSampleSet(6000, 3800, null)); // <- this one gets removed
        sampleSets.add(createSampleSet(7000, 8080, null));
        matchupSet.setSampleSets(sampleSets);

        final TimeDeltaCondition timeDeltaCondition = new TimeDeltaCondition(1200);
        timeDeltaCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(4, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_withSecondarySensorName() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = new ArrayList<>();
        final String secSensorName = "name";
        sampleSets.add(createSampleSet(1000, 1100, secSensorName));
        sampleSets.add(createSampleSet(2000, 2100, secSensorName));
        sampleSets.add(createSampleSet(3000, 4300, secSensorName)); // <- this one gets removed
        sampleSets.add(createSampleSet(5000, 4800, secSensorName));
        sampleSets.add(createSampleSet(6000, 3800, secSensorName)); // <- this one gets removed
        sampleSets.add(createSampleSet(7000, 8080, secSensorName));
        matchupSet.setSampleSets(sampleSets);

        final TimeDeltaCondition timeDeltaCondition = new TimeDeltaCondition(1200);
        timeDeltaCondition.setSecondarySensorNames(secSensorName);
        timeDeltaCondition.apply(matchupSet, new ConditionEngineContext());

        assertEquals(4, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_SecondarySensorNameIsSetInCondition_butNotInSampleSets() {
        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        final String secSensorName = "name";
        final String differentSecSensorName = "different";
        sampleSets.add(createSampleSet(1000, 1100, differentSecSensorName));

        final TimeDeltaCondition timeDeltaCondition = new TimeDeltaCondition(1200);
        timeDeltaCondition.setSecondarySensorNames(secSensorName);
        try {
            timeDeltaCondition.apply(matchupSet, new ConditionEngineContext());
            fail("NullPointerException expected");
        } catch (NullPointerException expected) {
            assertEquals(null, expected.getMessage());
        }
    }

    @Test
    public void testApply_noPrimaryCheck_butCheckBetweenThreeSecondaries() throws Exception {
        //preparation
        final int maxTimeDeltaInMillis = 5;
        final TimeDeltaCondition condition = new TimeDeltaCondition(maxTimeDeltaInMillis);
        condition.setPrimaryCheck(false);
        condition.setSecondaryCheck(true);
        condition.setSecondarySensorNames("a", "b", "c");

        final MatchupSet matchupSet = new MatchupSet();
        final SampleSet validSet = createSecSampleSet(new TimeAndName("a", 8), new TimeAndName("b", 4), new TimeAndName("c", 9));
        final SampleSet invalidSet = createSecSampleSet(new TimeAndName("a", 2), new TimeAndName("b", 5), new TimeAndName("c", 8));
        matchupSet.getSampleSets().add(validSet);
        matchupSet.getSampleSets().add(invalidSet);

        //execution
        condition.apply(matchupSet, new ConditionEngineContext());

        //verification
        assertEquals(1, matchupSet.getNumObservations());
        assertSame(validSet, matchupSet.getSampleSets().get(0));
    }

    private SampleSet createSampleSet(int primaryTime, int secondaryTime, String secSensorName) {
        if (secSensorName == null) {
            secSensorName = SampleSet.getOnlyOneSecondaryKey();
        }
        return createSampleSet(primaryTime, new TimeAndName(secSensorName, secondaryTime));
    }

    private SampleSet createSecSampleSet(TimeAndName... secTimesAndName) {
        return createSampleSet(0, secTimesAndName);

    }

    private SampleSet createSampleSet(int primaryTime, TimeAndName... secTimesAndName) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        for (TimeAndName timeAndName : secTimesAndName) {
            final String name = timeAndName.secSensorName;
            final int time = timeAndName.secondaryTime;
            sampleSet.setSecondary(name, new Sample(0, 0, 0, 0, time));
        }
        return sampleSet;
    }

    static class TimeAndName {

        final int secondaryTime;
        final String secSensorName;

        public TimeAndName(String secSensorName, int secondaryTime) {
            this.secondaryTime = secondaryTime;
            this.secSensorName = secSensorName;
        }
    }
}
