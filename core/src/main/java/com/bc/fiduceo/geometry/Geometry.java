package com.bc.fiduceo.geometry;


public interface Geometry {

    Geometry intersection(Geometry other);

    Object getInner();

    String toString();
}
