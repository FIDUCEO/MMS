package com.bc.fiduceo.geometry;

import com.bc.fiduceo.math.TimeInterval;

import java.util.Date;

public class L3TimeAxis implements TimeAxis {

    private final Date startDate;
    private final Date stopDate;
    private final Geometry geometry;

    public L3TimeAxis(Date startDate, Date stopDate, Geometry geometry) {
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.geometry = geometry;
    }

    @Override
    public TimeInterval getIntersectionTime(Polygon polygon) {
        return new TimeInterval(startDate, stopDate);
    }

    @Override
    public TimeInterval getProjectionTime(LineString polygonSide) {
        return new TimeInterval(startDate, stopDate);
    }

    @Override
    public Date getTime(Point coordinate) {
        final long avgTime = (stopDate.getTime() + startDate.getTime()) / 2;

        return new Date(avgTime);
    }

    @Override
    public Date getStartTime() {
        return startDate;
    }

    @Override
    public Date getEndTime() {
        return stopDate;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public long getDurationInMillis() {
        return stopDate.getTime() - startDate.getTime();
    }
}
