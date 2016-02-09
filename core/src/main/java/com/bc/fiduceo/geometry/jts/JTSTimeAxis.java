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

package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.util.Date;

class JTSTimeAxis implements TimeAxis {

    private final Date startTime;
    private final com.vividsolutions.jts.geom.LineString lineString;
    private final double inverseAxisLength;
    private final LengthIndexedLine lengthIndexedLine;
    private final long timeInterval;

    JTSTimeAxis(com.vividsolutions.jts.geom.LineString lineString, Date startTime, Date endTime) {
        this.startTime = startTime;
        this.lineString = lineString;
        this.inverseAxisLength = 1.0 / lineString.getLength();
        lengthIndexedLine = new LengthIndexedLine(lineString);
        timeInterval = endTime.getTime() - startTime.getTime();
    }

    @Override
    public TimeInterval getIntersectionTime(Polygon polygon) {
        final com.vividsolutions.jts.geom.Polygon inner = (com.vividsolutions.jts.geom.Polygon) polygon.getInner();
        final com.vividsolutions.jts.geom.LineString intersection = (com.vividsolutions.jts.geom.LineString) inner.intersection(lineString);
        if (intersection.isEmpty()) {
            return null;
        }

        final com.vividsolutions.jts.geom.Point startPoint = intersection.getStartPoint();
        final double pointLength = lengthIndexedLine.indexOf(startPoint.getCoordinate());
        final double intersectionLength = intersection.getLength();

        final double relativeIntersection = intersectionLength * inverseAxisLength;
        final double relativeOffset = pointLength * inverseAxisLength;

        final long intersectionDuration = (long) (timeInterval * relativeIntersection);
        final long offsetTime = (long) (timeInterval * relativeOffset);

        final long startMillis = startTime.getTime() + offsetTime;
        return new TimeInterval(TimeUtils.create(startMillis), TimeUtils.create(startMillis + intersectionDuration));
    }

    @Override
    public TimeInterval getProjectionTime(LineString polygonSide) {
        final com.vividsolutions.jts.geom.LineString inner = (com.vividsolutions.jts.geom.LineString) polygonSide.getInner();
        final int numPoints = inner.getNumPoints();

        Coordinate startProjection;
        final com.vividsolutions.jts.geom.Point startPoint = inner.getPointN(0);
        startProjection = findProjection(startPoint.getCoordinate());

        final com.vividsolutions.jts.geom.Point endPoint = inner.getPointN(numPoints - 1);
        final Coordinate endProjection = findProjection(endPoint.getCoordinate());

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

        return new TimeInterval(TimeUtils.create(startMillis), TimeUtils.create(endMillis));
    }

    @Override
    public Date getTime(Point coordinate) {
        final com.vividsolutions.jts.geom.Coordinate inner = (Coordinate) coordinate.getInner();
        final Coordinate projection = findProjection(inner);
        if (projection == null) {
            return null;
        }

        final double pointLength = lengthIndexedLine.indexOf(projection);
        final double relativeOffset = pointLength * inverseAxisLength;
        final long offsetTime = (long) (timeInterval * relativeOffset);
        final long startMillis = startTime.getTime() + offsetTime;

        return TimeUtils.create(startMillis);
    }

    private Coordinate findProjection(Coordinate coordinate) {
        // @todo 2 tb/tb we can speed up this routine by starting searching in the middle of the line
        // go to one direction, if the abs-value of the projectionFactor increases, search in the other direction
        // projectionFactor abs-value should decrease if we`re going in the right direction 2015-08-14

        final int numPoints = lineString.getNumPoints();

        for (int n = 0; n < numPoints - 1; n++) {
            final com.vividsolutions.jts.geom.Point point1 = lineString.getPointN(n);
            final com.vividsolutions.jts.geom.Point point2 = lineString.getPointN(n + 1);
            final LineSegment lineSegment = new LineSegment(point1.getCoordinate(), point2.getCoordinate());
            final double projectionFactor = lineSegment.projectionFactor(coordinate);
            if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
                return lineSegment.project(coordinate);
            }
        }

        return null;
    }
}
