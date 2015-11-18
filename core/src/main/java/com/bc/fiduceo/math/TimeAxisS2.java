package com.bc.fiduceo.math;

import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.Date;

public class TimeAxisS2 {

    private final S2Polyline polyline;

    public TimeAxisS2(S2Polyline polyline, Date startTime, Date endTime) {
        this.polyline = polyline;
    }

    public TimeInterval getIntersectionTime(S2Polygon polygon) {
        //polygon.
       // polyline.
        return null;
    }
}
