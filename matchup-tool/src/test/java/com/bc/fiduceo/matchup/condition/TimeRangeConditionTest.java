package com.bc.fiduceo.matchup.condition;

import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Sabine on 04.04.2016.
 */
public class TimeRangeConditionTest {

    private final int oneDayMillis = 1000 * 60 * 60 * 24;
    private final int fiveDays = 5 * oneDayMillis;
    private final int twelveDays = 12 * oneDayMillis;
    private Date startDate;
    private Date endDate;
    private TimeRangeCondition timeRangeCondition;

    @Before
    public void setUp() throws Exception {
        startDate = new Date();
        endDate = new Date(startDate.getTime() + twelveDays);
        timeRangeCondition = new TimeRangeCondition(startDate, endDate);
    }

    @Test
    public void testApply() throws Exception {
        final long startTime = startDate.getTime();
        final long endTime = endDate.getTime();

        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(startTime - 1, 100500));    // <- this one gets removed
        sampleSets.add(createSampleSet(startTime, 100100));
        sampleSets.add(createSampleSet(startTime + fiveDays, 100500));
        sampleSets.add(createSampleSet(endTime, 100500));
        sampleSets.add(createSampleSet(endTime + 1, 100500));    // <- this one gets removed

        timeRangeCondition.apply(matchupSet, new ConditionsContext());

        assertEquals(3, matchupSet.getNumObservations());
        final List<SampleSet> resultSet = matchupSet.getSampleSets();

        assertEquals(startTime, resultSet.get(0).getPrimary().time);
        assertEquals(startTime + fiveDays, resultSet.get(1).getPrimary().time);
        assertEquals(endTime, resultSet.get(2).getPrimary().time);

    }

    private SampleSet createSampleSet(long primaryTime, long secondaryTime) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        sampleSet.setSecondary(new Sample(0, 0, 0, 0, secondaryTime));
        return sampleSet;
    }
}