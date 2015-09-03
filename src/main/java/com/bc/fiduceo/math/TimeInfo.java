package com.bc.fiduceo.math;


public class TimeInfo {

    private TimeInterval timeInterval;
    private int minimalTimeDelta;

    public TimeInfo() {
        minimalTimeDelta = Integer.MAX_VALUE;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getMinimalTimeDelta() {
        return minimalTimeDelta;
    }

    public void setMinimalTimeDelta(int minimalTimeDelta) {
        this.minimalTimeDelta = minimalTimeDelta;
    }
}
