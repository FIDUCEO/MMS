package com.bc.fiduceo.geometry.jts;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.vividsolutions.jts.geom.Coordinate;

class JTSPoint implements Point{

    private final Coordinate coordinate;

    JTSPoint(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public Geometry intersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getInner() {
        return coordinate;
    }

    @Override
    public double getLon() {
        return coordinate.x;
    }

    @Override
    public double getLat() {
        return coordinate.y;
    }
}
