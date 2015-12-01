package com.bc.fiduceo.geometry.s2;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.google.common.geometry.S2Polyline;

class S2LineString implements LineString {

    private final S2Polyline googleLineString;

    public S2LineString(S2Polyline googleLineString) {
        this.googleLineString = googleLineString;
    }

    @Override
    public String toString() {
        return googleLineString.toString();
    }

    @Override
    public Geometry intersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getInner() {
        throw new RuntimeException("not implemented");
    }
}
