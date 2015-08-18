package com.bc.fiduceo.math;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeInterval {

    private final Date startTime;
    private final Date stopTime;

    public static TimeInterval create(List<Date> dates) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (final Date date : dates) {
            final long time = date.getTime();
            if (time < min) {
                min = time;
            }
            if (time > max) {
                max = time;
            }
        }

        return new TimeInterval(new Date(min), new Date(max));
    }

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

    public TimeInterval intersect(TimeInterval other) {
        if (startTime.after(other.getStopTime()) ||
                stopTime.before(other.getStartTime())) {
            return null;    // no intersection at all
        }

        Date intersectStart;
        if (startTime.before(other.getStartTime())) {
            intersectStart = other.getStartTime();
        } else {
            intersectStart = startTime;
        }

        Date intersectStop;
        if (stopTime.after(other.getStopTime())) {
            intersectStop = other.getStopTime();
        } else {
            intersectStop = stopTime;
        }

        return new TimeInterval(intersectStart, intersectStop);
    }
}
