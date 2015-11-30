package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Polygon;

class JTSPolygon implements Polygon {

    private final com.vividsolutions.jts.geom.Polygon jtsPolygon;

    public JTSPolygon(com.vividsolutions.jts.geom.Polygon jtsPolygon) {
        this.jtsPolygon = jtsPolygon;
    }

    @Override
    public String toString() {
        return jtsPolygon.toString();
    }
}
