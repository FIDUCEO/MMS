package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.matchup.MatchupSet;
import org.junit.Before;
import org.junit.Test;

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
    }

}
