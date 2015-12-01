package com.bc.fiduceo.geometry;

import java.util.List;

public interface AbstractGeometryFactory {

    Geometry parse(String wkt);

    Point createPoint(double lon, double lat);

    Polygon createPolygon(List<Point> points);

    LineString createLineString(List<Point> points);
}
