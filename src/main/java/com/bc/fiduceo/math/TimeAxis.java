package com.bc.fiduceo.math;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.util.Date;

public class TimeAxis {

    private final Date startTime;
    private final Date endTime;
    private final LineString lineString;
    private final double inverseAxisLength;

    public TimeAxis(LineString lineString, Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.lineString = lineString;
        this.inverseAxisLength = 1.0 / lineString.getLength();

    }

    public TimeInterval intersect(Polygon polygon) {
        final LineString intersection = (LineString) polygon.intersection(lineString);
        final LengthIndexedLine lengthIndexedLine = new LengthIndexedLine(lineString);  // @todo 1 tb/tb move to constructor 2015-08-14

        final Point startPoint = intersection.getStartPoint();
        final double pointLength = lengthIndexedLine.indexOf(startPoint.getCoordinate());
        final double intersectionLength = intersection.getLength();

        final double relativeIntersection = intersectionLength * inverseAxisLength;
        final double relativeOffset = pointLength * inverseAxisLength;

        final long timeInterval = endTime.getTime() - startTime.getTime();
        final long intersectionDuration = (long) (timeInterval * relativeIntersection);
        final long offsetTime = (long) (timeInterval * relativeOffset);

        final long startMillis = startTime.getTime() + offsetTime;
        return new TimeInterval(new Date(startMillis), new Date(startMillis + intersectionDuration));
    }
}
