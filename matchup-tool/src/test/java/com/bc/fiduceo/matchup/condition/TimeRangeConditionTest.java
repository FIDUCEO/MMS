package com.bc.fiduceo.matchup.condition;

import static org.junit.Assert.*;

import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.*;

import java.util.Date;
import java.util.List;

public class TimeRangeConditionTest {

    private final int oneDayMillis = 1000 * 60 * 60 * 24;
    private Date startDate;
    private Date endDate;
    private TimeRangeCondition timeRangeCondition;
    private ConditionEngineContext context;

    @Before
    public void setUp() throws Exception {
        startDate = new Date();

        final int twelveDays = 12 * oneDayMillis;
        endDate = new Date(startDate.getTime() + twelveDays);

        context = new ConditionEngineContext();
        context.setStartDate(startDate);
        context.setEndDate(endDate);
        timeRangeCondition = new TimeRangeCondition();
    }

    @Test
    public void testApply() throws Exception {
        final long startTime = startDate.getTime();
        final long endTime = endDate.getTime();

        final MatchupSet matchupSet = new MatchupSet();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(startTime - 1, 100500));    // <- this one gets removed
        sampleSets.add(createSampleSet(startTime, 100100));
        final int fiveDays = 5 * oneDayMillis;
        sampleSets.add(createSampleSet(startTime + fiveDays, 100500));
        sampleSets.add(createSampleSet(endTime, 100500));
        sampleSets.add(createSampleSet(endTime + 1, 100500));    // <- this one gets removed

        timeRangeCondition.apply(matchupSet, context);

        assertEquals(3, matchupSet.getNumObservations());
        final List<SampleSet> resultSet = matchupSet.getSampleSets();

        assertEquals(startTime, resultSet.get(0).getPrimary().time);
        assertEquals(startTime + fiveDays, resultSet.get(1).getPrimary().time);
        assertEquals(endTime, resultSet.get(2).getPrimary().time);

    }

    private SampleSet createSampleSet(long primaryTime, long secondaryTime) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), new Sample(0, 0, 0, 0, secondaryTime));
        return sampleSet;
    }
}