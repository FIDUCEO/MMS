package com.bc.fiduceo.geometry.s2;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.google.common.geometry.S2LatLng;

class S2Point implements Point{

    private final S2LatLng s2LatLng;

    S2Point(S2LatLng s2LatLng) {
        this.s2LatLng = s2LatLng;
    }

    @Override
    public double getLon() {
        return s2LatLng.lngDegrees();
    }

    @Override
    public double getLat() {
        return s2LatLng.latDegrees();
    }

    @Override
    public Geometry intersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getInner() {
        return s2LatLng;
    }
}
