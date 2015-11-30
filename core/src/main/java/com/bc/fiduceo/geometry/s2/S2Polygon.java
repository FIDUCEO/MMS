package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Polygon;

class S2Polygon implements Polygon {

    private final com.google.common.geometry.S2Polygon googlePolygon;

    public S2Polygon(Object geometry) {
        this.googlePolygon = (com.google.common.geometry.S2Polygon) geometry;
    }

    @Override
    public String toString() {
        return googlePolygon.toString();
    }
}
