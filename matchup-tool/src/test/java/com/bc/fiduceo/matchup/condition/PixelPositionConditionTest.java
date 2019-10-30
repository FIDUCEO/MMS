package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PixelPositionConditionTest {

    private MatchupSet matchupSet;
    private ConditionEngineContext context;

    @Before
    public void setUp() {
        matchupSet = new MatchupSet();
        context = new ConditionEngineContext();
    }

    @Test
    public void testConfigurationConstructor() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        assertEquals(Integer.MIN_VALUE, configuration.minX);
        assertEquals(Integer.MAX_VALUE, configuration.maxX);

        assertEquals(Integer.MIN_VALUE, configuration.minY);
        assertEquals(Integer.MAX_VALUE, configuration.maxY);

        assertTrue(configuration.isPrimary);
        assertEquals(0, configuration.secondaryNames.length);
    }

    @Test
    public void testApply_emptyMatchupSet() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        condition.apply(matchupSet, context);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_noConstraints_primary() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        Util.addSampleSet(12, 198, 217, 2356, matchupSet);
        Util.addSampleSet(134, 1112, 2267, 4356, matchupSet);

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(12, sampleSets.get(0).getPrimary().getX());
        assertEquals(134, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_noConstraints_secondary() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        configuration.isPrimary = false;
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        Util.addSampleSet(13, 199, 218, 2357, matchupSet);
        Util.addSampleSet(135, 1113, 2268, 4357, matchupSet);

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(13, sampleSets.get(0).getPrimary().getX());
        assertEquals(135, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_x_constrained_primary() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        configuration.minX = 50;
        configuration.maxX = 500;
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        Util.addSampleSet(10, 2000, 217, 2356, matchupSet);
        Util.addSampleSet(100, 3000, 1, 4356, matchupSet);   // <- keep
        Util.addSampleSet(100, 4000, 2, 4356, matchupSet);   // <- keep
        Util.addSampleSet(1000, 5000, 2267, 4356, matchupSet);

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.get(0).getSecondary(SampleSet.getOnlyOneSecondaryKey()).getX());
        assertEquals(2, sampleSets.get(1).getSecondary(SampleSet.getOnlyOneSecondaryKey()).getX());
    }

    @Test
    public void testApply_x_constrained_secondary() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        configuration.minX = 50;
        configuration.maxX = 2500;
        configuration.isPrimary = false;
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        Util.addSampleSet(101, 20001, 200, 2357, matchupSet);   // <- keep
        Util.addSampleSet(102, 30002, 30, 4358, matchupSet);
        Util.addSampleSet(103, 40003, 2000, 4359, matchupSet);   // <- keep
        Util.addSampleSet(104, 50004, 4000, 4360, matchupSet);

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(101, sampleSets.get(0).getPrimary().getX());
        assertEquals(103, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_y_constrained_primary() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        configuration.minY = 1000;
        configuration.maxY = 2000;
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        Util.addSampleSet(1, 1400, 1, 2356, matchupSet);    // <- keep
        Util.addSampleSet(3, 998, 2267, 4356, matchupSet);
        Util.addSampleSet(5, 1998, 2, 4356, matchupSet);   // <- keep
        Util.addSampleSet(7, 2010, 2267, 4356, matchupSet);

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(1, sampleSets.get(0).getPrimary().getX());
        assertEquals(5, sampleSets.get(1).getPrimary().getX());
    }

    @Test
    public void testApply_y_constrained_twoSecondaries() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        configuration.minY = 1000;
        configuration.maxY = 2000;
        configuration.isPrimary = false;
        configuration.secondaryNames = new String[]{"sec_B"};
        final PixelPositionCondition condition = new PixelPositionCondition(configuration);

        SampleSet sampleSet = Util.addSampleSet(1, 1400, 1, 2356, "sec_A", matchupSet);
        sampleSet.setSecondary("sec_B", new Sample(10, 235, Double.NaN, Double.NaN, -1));

        Util.addSampleSet(3, 998, 2267, 1356, "sec_A", matchupSet);   // <- keep

        sampleSet = Util.addSampleSet(5, 1998, 2, 4356, "sec_A", matchupSet);
        sampleSet.setSecondary("sec_B", new Sample(20, 435, Double.NaN, Double.NaN, -1));

        Util.addSampleSet(7, 2010, 2267, 1456, "sec_B", matchupSet);  // <- keep

        condition.apply(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        assertEquals(3, sampleSets.get(0).getPrimary().getX());
        assertEquals(7, sampleSets.get(1).getPrimary().getX());
    }
}
