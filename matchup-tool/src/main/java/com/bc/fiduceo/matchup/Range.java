package com.bc.fiduceo.matchup;

public class Range {

    private double min;
    private double max;

    public Range() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
    }

    public boolean isValid() {
        return min < max;
    }

    public void aggregate(double value) {
        max = Math.max(max, value);
        min = Math.min(min, value);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
}
