package com.bc.fiduceo.geometry;

import java.util.List;

public interface MultiPolygon extends Polygon {

    List<Polygon> getPolygons();
}
