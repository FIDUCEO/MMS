package com.bc.fiduceo.math;


public class TimeInfo {

    private TimeInterval overlapInterval;
    private int minimalTimeDelta;

    public TimeInfo() {
        minimalTimeDelta = Integer.MAX_VALUE;
    }

    public TimeInterval getOverlapInterval() {
        return overlapInterval;
    }

    public void setOverlapInterval(TimeInterval overlapInterval) {
        this.overlapInterval = overlapInterval;
    }

    public int getMinimalTimeDelta() {
        return minimalTimeDelta;
    }

    public void setMinimalTimeDelta(int minimalTimeDelta) {
        this.minimalTimeDelta = minimalTimeDelta;
    }
}
