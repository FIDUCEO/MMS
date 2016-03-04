
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.math;

import com.bc.fiduceo.util.TimeUtils;

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

        return new TimeInterval(TimeUtils.create(min), TimeUtils.create(max));
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

    public TimeInterval[] split(int numSegments) {
        final TimeInterval[] segments = new TimeInterval[numSegments];
        final long stopMillis = stopTime.getTime();
        final long startMillis = startTime.getTime();
        final long duration = stopMillis - startMillis;
        final long step = duration / numSegments;

        long offset = 0;
        for (int i = 0; i < numSegments; i++) {
            long startOffset = startMillis + offset;
            final Date segmentStart = TimeUtils.create(startOffset);
            Date segmentEnd;
            if (i == numSegments -1) {
                segmentEnd = TimeUtils.create(stopMillis);
            } else {
                segmentEnd = TimeUtils.create(startOffset + step);
            }
            segments[i] = new TimeInterval(segmentStart, segmentEnd);
            offset += step;
        }
        return segments;
    }
}
