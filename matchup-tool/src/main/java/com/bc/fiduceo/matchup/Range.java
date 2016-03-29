package com.bc.fiduceo.matchup;

class Range {

    private double min;
    private double max;

    Range() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
    }

    void aggregate(double value) {
        max = Math.max(max, value);
        min = Math.min(min, value);
    }

    double getMax() {
        return max;
    }

    double getMin() {
        return min;
    }
}
