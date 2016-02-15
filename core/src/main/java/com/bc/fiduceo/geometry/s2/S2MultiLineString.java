package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author muhammad.bc
 */
class S2MultiLineString implements LineString {
    private List<S2Polyline> s2PolylineList;

    public S2MultiLineString(List<S2Polyline> s2Polylines) {
        this.s2PolylineList = s2Polylines;
    }

    @Override
    public Geometry intersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Point[] getCoordinates() {
        List<Point> pointList = new ArrayList<>();
        for (S2Polyline s2Polyline : s2PolylineList) {
            int i = s2Polyline.numVertices();
            for (int j = 0; j < i; j++) {
                    pointList.add(new S2Point(new S2LatLng(s2Polyline.vertex(j))));
                }
            }
        return pointList.toArray(new Point[pointList.size()]);
    }

    @Override
    public Object getInner() {
        return s2PolylineList;
    }
}
