package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;

/**
 * @author muhammad.bc
 */
public class S2MultiPolygon implements Polygon {

    @Override
    public void shiftLon(double lon) {

    }

    @Override
    public Geometry intersection(Geometry other) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Point[] getCoordinates() {
        return new Point[0];
    }

    @Override
    public Object getInner() {
        return null;
    }
}
