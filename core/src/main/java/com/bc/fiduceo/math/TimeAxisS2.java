package com.bc.fiduceo.math;

import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeAxisS2 {

    private final S2Polyline polyline;
    private final double invLength;
    private final Date startTime;
    private final long timeInterval;

    public TimeAxisS2(S2Polyline polyline, Date startTime, Date endTime) {
        this.polyline = polyline;

        final S1Angle arclengthAngle = polyline.getArclengthAngle();
        this.invLength = 1.0 / arclengthAngle.radians();

        this.startTime = startTime;
        this.timeInterval = endTime.getTime() - startTime.getTime();
    }

    public TimeInterval getIntersectionTime(S2Polygon polygon) {
        List<S2Polyline> s2Polylines = polygon.intersectWithPolyLine(polyline);
        if (s2Polylines.isEmpty()) {
            return null;
        }

        // @todo 2 tb/tb check under which circumstances we can have more than one intersection - and what we
        // do then ??? 2015-11-18
        final S2Polyline intersection = s2Polylines.get(0);

        final S2Point intersectionStartPoint = intersection.vertex(0);
        final S2Polyline offsetGeometry = createSubLineTo(intersectionStartPoint);
        final S1Angle offsetAngle = offsetGeometry.getArclengthAngle();
        final long offsetTime = (long) (timeInterval * offsetAngle.radians() * invLength);

        final S2Point intersectionEndPoint = intersection.vertex(intersection.numVertices() - 1);
        final S2Polyline totalGeometry = createSubLineTo(intersectionEndPoint);
        final S1Angle totalAngle = totalGeometry.getArclengthAngle();
        final long totalTime = (long) (timeInterval * totalAngle.radians() * invLength);
        final long duration = totalTime - offsetTime;

        final long startMillis = startTime.getTime() + offsetTime;
        return new TimeInterval(new Date(startMillis), new Date(startMillis + duration));
    }

    // package access for testing only tb 2015-11-20
    S2Polyline createSubLineTo(S2Point intersectionStartPoint) {
        final List<S2Point> vertices = new ArrayList<>();

        final int lastVertexIndex = polyline.numVertices() - 1;
        final int nearestEdgeIndex = polyline.getNearestEdgeIndex(intersectionStartPoint);
        if (nearestEdgeIndex == 0) {
            vertices.add(polyline.vertex(0));
            vertices.add(intersectionStartPoint);
        } else {
            int lastCopyIndex;
            if (nearestEdgeIndex == lastVertexIndex) {
                lastCopyIndex = nearestEdgeIndex - 1;
            } else {
                final S2Point point_0 = polyline.vertex(nearestEdgeIndex);
                final S2Point point_1 = polyline.vertex(nearestEdgeIndex + 1);

                final double angle_0 = point_0.angle(intersectionStartPoint);
                final double angle_1 = point_1.angle(intersectionStartPoint);
                if (angle_0 < angle_1) {
                    lastCopyIndex = nearestEdgeIndex;  // closer to the point we need to copy
                } else {
                    lastCopyIndex = nearestEdgeIndex - 1; // closer to the point we want to skip
                }
            }

            for (int i = 0; i <= lastCopyIndex; i++) {
                vertices.add(polyline.vertex(i));
            }
            vertices.add(intersectionStartPoint);
        }

        return new S2Polyline(vertices);
    }
}
