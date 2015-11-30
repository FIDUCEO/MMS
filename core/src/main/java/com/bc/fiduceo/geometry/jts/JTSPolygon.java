package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Polygon;

class JTSPolygon implements Polygon {

    private final com.vividsolutions.jts.geom.Polygon jtsPolygon;

    public JTSPolygon(com.vividsolutions.jts.geom.Polygon jtsPolygon) {
        this.jtsPolygon = jtsPolygon;
    }

    @Override
    public Geometry intersection(Geometry other) {
        final com.vividsolutions.jts.geom.Polygon intersection = (com.vividsolutions.jts.geom.Polygon) jtsPolygon.intersection((com.vividsolutions.jts.geom.Geometry) other.getInner()).clone();
        return new JTSPolygon(intersection);
    }

    @Override
    public String toString() {
        return jtsPolygon.toString();
    }

    @Override
    public Object getInner() {
        return jtsPolygon;
    }
}
