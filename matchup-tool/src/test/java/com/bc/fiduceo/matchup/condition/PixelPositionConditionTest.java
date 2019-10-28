package com.bc.fiduceo.matchup.condition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PixelPositionConditionTest {

    @Test
    public void testConfigurationConstructor() {
        final PixelPositionCondition.Configuration configuration = new PixelPositionCondition.Configuration();
        assertEquals(Integer.MIN_VALUE, configuration.minX);
        assertEquals(Integer.MAX_VALUE, configuration.maxX);

        assertEquals(Integer.MIN_VALUE, configuration.minY);
        assertEquals(Integer.MAX_VALUE, configuration.maxY);
    }
}
