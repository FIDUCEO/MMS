package com.bc.fiduceo.geometry;

public interface AbstractGeometryFactory {

    Geometry parse(String wkt);
}
