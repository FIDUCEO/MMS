package com.bc.fiduceo.geometry.jts;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;

class JTSLineString implements LineString{

    private final com.vividsolutions.jts.geom.LineString jtsLineString;

    JTSLineString(com.vividsolutions.jts.geom.LineString jtsLineString) {
        this.jtsLineString = jtsLineString;
    }

    @Override
    public Geometry intersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String toString() {
        return jtsLineString.toString();
    }

    @Override
    public Object getInner() {
        throw new RuntimeException("not implemented");
    }
}
