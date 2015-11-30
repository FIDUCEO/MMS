package com.bc.fiduceo.geometry;

public class JTSPolygon implements Polygon {

    private final com.vividsolutions.jts.geom.Polygon jtsPolygon;

    public JTSPolygon(com.vividsolutions.jts.geom.Polygon jtsPolygon) {
        this.jtsPolygon = jtsPolygon;
    }

    @Override
    public String toString() {
        return jtsPolygon.toString();
    }
}
