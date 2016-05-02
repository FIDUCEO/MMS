/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;
import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.util.TimeUtils;

import java.util.Date;
import java.util.List;

class TimeLatMapStrategy implements MapStrategy {

    private final int width;
    private final int height;

    private double scale;
    private long startTime;

    TimeLatMapStrategy(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void initialize(List<SamplingPoint> samplingPoints) {
        final TimeRange timeRange = extractTimeRangeInFullMonths(samplingPoints);
        startTime = timeRange.getStartDate().getTime();
        scale = 1.0 / (timeRange.getStopDate().getTime() - startTime);
    }

    @Override
    public PlotPoint map(SamplingPoint samplingPoint) {
        final double x_scale = scale * (samplingPoint.getTime() - startTime);
        final double y_scale = (90.0 - samplingPoint.getLat()) / 180.0;
        final int x = (int) (x_scale * width);
        final int y = (int) (y_scale * height);
        return new PlotPoint(x, y);
    }

//    // package access for testing only tb 2014-02-20
    static TimeRange extractTimeRangeInFullMonths(List<SamplingPoint> points) {
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        for (final SamplingPoint samplingPoint : points) {
            final long samplingPointTime = samplingPoint.getTime();
            if (samplingPointTime < minTime) {
                minTime = samplingPointTime;
            }

            if (samplingPointTime > maxTime) {
                maxTime = samplingPointTime;
            }
        }
        final Date startDate = new Date(minTime);
        final Date stopDate = new Date(maxTime);
        return new TimeRange(TimeUtils.getBeginOfMonth(startDate), TimeUtils.getEndOfMonth(stopDate));
    }
}
