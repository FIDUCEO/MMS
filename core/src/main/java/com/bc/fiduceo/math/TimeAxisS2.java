package com.bc.fiduceo.math;

import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.Date;
import java.util.List;

public class TimeAxisS2 {

    private final S2Polyline polyline;

    public TimeAxisS2(S2Polyline polyline, Date startTime, Date endTime) {
        this.polyline = polyline;
    }

    public TimeInterval getIntersectionTime(S2Polygon polygon) {
        List<S2Polyline> s2Polylines = polygon.intersectWithPolyLine(polyline);
        if (s2Polylines.isEmpty() ) {
            return null;
        }

        // @todo 2 tb/tb check under which circumstaces we can have more than one intersection - and what doe we
        // do then ??? 2015-11-18
        final S2Polyline intersection = s2Polylines.get(0);
        S2Point vertex = intersection.vertex(0);

        return null;
    }
}
