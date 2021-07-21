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

package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.util.TimeUtils;
import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class BcS2TimeAxis implements TimeAxis {

    private final S2Polyline polyline;
    private final LineString originalGeometry;
    private final double invLength;
    private final Date startTime;
    private final long timeInterval;

    BcS2TimeAxis(LineString lineString, Date startTime, Date endTime) {
        originalGeometry = lineString;
        this.polyline = (S2Polyline) lineString.getInner();

        final S1Angle arclengthAngle = polyline.getArclengthAngle();
        this.invLength = 1.0 / arclengthAngle.radians();

        this.startTime = startTime;
        this.timeInterval = endTime.getTime() - startTime.getTime();
    }

    @Override
    public TimeInterval getIntersectionTime(Polygon polygon) {
        final S2Polygon inner = (S2Polygon) polygon.getInner();
        List<S2Polyline> s2Polylines = inner.intersectWithPolyLine(polyline);
        if (s2Polylines.isEmpty()) {
            return null;
        }

        if (s2Polylines.size() > 1) {
            throw new RuntimeException("More than one intersection, not implemented yet!");
        }

        final S2Polyline intersection = s2Polylines.get(0);

        final S2Point intersectionStartPoint = intersection.vertex(0);
        final long offsetTime = calculateLineDuration(intersectionStartPoint);

        final S2Point intersectionEndPoint = intersection.vertex(intersection.numVertices() - 1);
        final long totalTime = calculateLineDuration(intersectionEndPoint);

        final long duration = totalTime - offsetTime;

        final long startMillis = startTime.getTime() + offsetTime;
        return new TimeInterval(TimeUtils.create(startMillis), TimeUtils.create(startMillis + duration));
    }

    @Override
    public TimeInterval getProjectionTime(LineString polygonSide) {
        final Point[] coordinates = polygonSide.getCoordinates();
        final Date projectionStartTime = getTime(coordinates[0]);
        final Date projectionStopTime = getTime(coordinates[coordinates.length - 1]);
        if (projectionStopTime.before(projectionStartTime)) {
            return new TimeInterval(projectionStopTime, projectionStartTime);
        }
        return new TimeInterval(projectionStartTime, projectionStopTime);
    }

    @Override
    public Date getTime(Point coordinate) {
        final S2Point searchPoint = ((S2LatLng) coordinate.getInner()).toPoint();
        final int nearestEdgeIndex = polyline.getNearestEdgeIndex(searchPoint);
        if (nearestEdgeIndex < 0) {
            return null;
        }

        final long offsetTime = calculateLineDuration(searchPoint);
        if (offsetTime > timeInterval) {
            return null;    // projection is outside the time axis range, beyond the last point tb 2015-11-23
        }

        final long timeMillis = startTime.getTime() + offsetTime;
        return TimeUtils.create(timeMillis);
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getEndTime() {
        return TimeUtils.create(startTime.getTime() + timeInterval);
    }

    @Override
    public long getDurationInMillis() {
        return timeInterval;
    }

    @Override
    public Geometry getGeometry() {
        return originalGeometry;
    }

    // package access for testing only tb 2015-11-20
    S2Polyline createSubLineTo(S2Point point) {
        final List<S2Point> vertices = new ArrayList<>();

        final int nearestEdgeIndex = polyline.getNearestEdgeIndex(point);
        final S2Point projectedIntersection = polyline.projectToEdge(point, nearestEdgeIndex);
        if (nearestEdgeIndex == 0) {
            vertices.add(polyline.vertex(0));
            vertices.add(projectedIntersection);
        } else {
            for (int i = 0; i <= nearestEdgeIndex; i++) {
                vertices.add(polyline.vertex(i));
            }
            vertices.add(projectedIntersection);
        }

        return new S2Polyline(vertices);
    }

    private long calculateLineDuration(S2Point point) {
        final S2Polyline offsetGeometry = createSubLineTo(point);
        final S1Angle offsetAngle = offsetGeometry.getArclengthAngle();
        return (long) (timeInterval * offsetAngle.radians() * invLength);
    }
}
