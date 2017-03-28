package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Test;

import static org.junit.Assert.*;

public class StrategyFactoryTest {

    @Test
    public void testGetTimeSeriesStrategy() {
        final Strategy strategy = StrategyFactory.getTimeSeries();
        assertNotNull(strategy);
        assertTrue(strategy instanceof TimeSeriesStrategy);

        final Strategy strategy_2 = StrategyFactory.getTimeSeries();
        assertSame(strategy, strategy_2);
    }

    @Test
    public void testGetSensorExtractsStrategy() {
        final Strategy strategy = StrategyFactory.getSensorExtracts();
        assertNotNull(strategy);
        assertTrue(strategy instanceof SensorExtractionStrategy);

        final Strategy strategy_2 = StrategyFactory.getSensorExtracts();
        assertSame(strategy, strategy_2);
    }
}
