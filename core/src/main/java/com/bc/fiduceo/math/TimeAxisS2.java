package com.bc.fiduceo.math;

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

        return null;
    }
}
