package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;

class S2Polygon implements Polygon {

    private final com.google.common.geometry.S2Polygon googlePolygon;

    public S2Polygon(Object geometry) {
        this.googlePolygon = (com.google.common.geometry.S2Polygon) geometry;
    }

    @Override
    public Geometry intersection(Geometry other) {
        final com.google.common.geometry.S2Polygon intersection = new com.google.common.geometry.S2Polygon();
        intersection.initToIntersection(googlePolygon, (com.google.common.geometry.S2Polygon) other.getInner());
        return new S2Polygon(intersection);
    }

    @Override
    public String toString() {
        return googlePolygon.toString();
    }

    @Override
    public Object getInner() {
        return googlePolygon;
    }
}
