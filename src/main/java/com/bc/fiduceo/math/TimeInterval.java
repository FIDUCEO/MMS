package com.bc.fiduceo.math;

import java.util.Date;

public class TimeInterval {

    private final Date startTime;
    private final Date stopTime;

    public TimeInterval(Date startTime, Date stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }
}
