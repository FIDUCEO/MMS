package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.MultiLineString;
import com.bc.fiduceo.geometry.Point;

public class JTSMultiLineString implements MultiLineString {

    private final com.vividsolutions.jts.geom.MultiLineString jtsMultiLineString;

    public JTSMultiLineString(com.vividsolutions.jts.geom.MultiLineString jtsMultiLineString) {
        this.jtsMultiLineString = jtsMultiLineString;
    }

    @Override
    public Geometry[] getGeometries() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setGeometries(Geometry[] geometries) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isEmpty() {
        return jtsMultiLineString.isEmpty();
    }

    @Override
    public boolean isValid() {
        return jtsMultiLineString.isValid();
    }

    @Override
    public Point[] getCoordinates() {
        return new Point[0];
    }

    @Override
    public String toString() {
        return jtsMultiLineString.toString();
    }

    @Override
    public Object getInner() {
        return null;
    }
}
