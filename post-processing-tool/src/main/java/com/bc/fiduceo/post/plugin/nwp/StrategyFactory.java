package com.bc.fiduceo.post.plugin.nwp;


class StrategyFactory {

    private static Strategy timeSeriesStrategy;
    private static Strategy sensorExtractsStrategy;

    static Strategy getTimeSeries() {
        if (timeSeriesStrategy == null) {
            timeSeriesStrategy = new TimeSeriesStrategy();
        }

        return timeSeriesStrategy;
    }

    static Strategy getSensorExtracts() {
        if (sensorExtractsStrategy == null) {
            sensorExtractsStrategy = new SensorExtractionStrategy();
        }

        return sensorExtractsStrategy;
    }
}
