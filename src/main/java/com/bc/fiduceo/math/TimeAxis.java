package com.bc.fiduceo.math;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.util.Date;

public class TimeAxis {

    // @todo 3 tb/tb check if we can gain performance when using prepared geometries 2015-08-14

    private final Date startTime;
    private final LineString lineString;
    private final double inverseAxisLength;
    private final LengthIndexedLine lengthIndexedLine;
    private final long timeInterval;

    public TimeAxis(LineString lineString, Date startTime, Date endTime) {
        this.startTime = startTime;
        this.lineString = lineString;
        this.inverseAxisLength = 1.0 / lineString.getLength();
        lengthIndexedLine = new LengthIndexedLine(lineString);
        timeInterval = endTime.getTime() - startTime.getTime();
    }

    public TimeInterval getIntersectionTime(Polygon polygon) {
        final LineString intersection = (LineString) polygon.intersection(lineString);
        if (intersection.isEmpty()) {
            return null;
        }

        final Point startPoint = intersection.getStartPoint();
        final double pointLength = lengthIndexedLine.indexOf(startPoint.getCoordinate());
        final double intersectionLength = intersection.getLength();

        final double relativeIntersection = intersectionLength * inverseAxisLength;
        final double relativeOffset = pointLength * inverseAxisLength;

        final long intersectionDuration = (long) (timeInterval * relativeIntersection);
        final long offsetTime = (long) (timeInterval * relativeOffset);

        final long startMillis = startTime.getTime() + offsetTime;
        return new TimeInterval(new Date(startMillis), new Date(startMillis + intersectionDuration));
    }

    public TimeInterval getProjectionTime(LineString polygonSide) {
        final int numPoints = polygonSide.getNumPoints();

        final Point startPoint = polygonSide.getPointN(0);
        final Coordinate startProjection = findProjection(startPoint);

        final Point endPoint = polygonSide.getPointN(numPoints - 1);
        final Coordinate endProjection = findProjection(endPoint);

        final double startOffset = lengthIndexedLine.indexOf(startProjection);
        final double endOffset = lengthIndexedLine.indexOf(endProjection);

        final double relativeStartOffset;
        final double relativeEndOffset;
        if (startOffset > endOffset) {
            relativeStartOffset = endOffset * inverseAxisLength;
            relativeEndOffset = startOffset * inverseAxisLength;
        }  else {
            relativeStartOffset = startOffset * inverseAxisLength;
            relativeEndOffset = endOffset * inverseAxisLength;
        }

        final long startOffsetTime = (long) (relativeStartOffset * timeInterval);
        final long endOffsetTime = (long) (relativeEndOffset * timeInterval);

        final long startMillis = startTime.getTime() + startOffsetTime;
        final long endMillis = startTime.getTime() + endOffsetTime;

        return new TimeInterval(new Date(startMillis), new Date(endMillis));
    }

    public Date getTime(Point point) {
        final Coordinate projection = findProjection(point);
        if (projection == null) {
            return null;
        }

        final double pointLength = lengthIndexedLine.indexOf(projection);
        final double relativeOffset = pointLength * inverseAxisLength;
        final long offsetTime = (long) (timeInterval * relativeOffset);
        final long startMillis = startTime.getTime() + offsetTime;

        return new Date(startMillis);
    }

    private Coordinate findProjection(Point point) {
        // @todo 2 tb/tb we can speed up this routine by starting searching in the middle of the line
        // go to one direction, if the abs-value of the projectionFactor increases, search in the other direction
        // projectionFactor abs-value should decrease if we`re going in the right direction 2015-08-14

        final int numPoints = lineString.getNumPoints();

        for (int n = 0; n < numPoints - 1; n++) {
            final Point point1 = lineString.getPointN(n);
            final Point point2 = lineString.getPointN(n + 1);
            final LineSegment lineSegment = new LineSegment(point1.getCoordinate(), point2.getCoordinate());
            final double projectionFactor = lineSegment.projectionFactor(point.getCoordinate());
            if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
                return lineSegment.project(point.getCoordinate());
            }
        }

        return null;
    }
}
