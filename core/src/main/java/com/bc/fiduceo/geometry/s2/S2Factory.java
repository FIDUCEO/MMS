package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.AbstractGeometryFactory;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

public class S2Factory implements AbstractGeometryFactory {

    private final S2WKTReader s2WKTReader;

    public S2Factory() {
        s2WKTReader = new S2WKTReader();
    }

    @Override
    public Geometry parse(String wkt) {
        final Object geometry = s2WKTReader.read(wkt);
        if (geometry instanceof com.google.common.geometry.S2Polygon) {
            return new S2Polygon(geometry);
        } else if (geometry instanceof com.google.common.geometry.S2Polyline) {
            return new S2LineString((S2Polyline) geometry);
        }

        throw new RuntimeException("Unsupported geometry type");
    }

    @Override
    public Point createPoint(double lon, double lat) {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lon);

        return new S2Point(s2LatLng);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        ArrayList<com.google.common.geometry.S2Point> loopPoints = new ArrayList<>();

        for (final Point point : points) {
            final S2LatLng s2LatLng = (S2LatLng) point.getInner();
            loopPoints.add(s2LatLng.toPoint());
        }

        final S2Loop s2Loop = new S2Loop(loopPoints);
        final com.google.common.geometry.S2Polygon googlePolygon = new com.google.common.geometry.S2Polygon(s2Loop);
        return new S2Polygon(googlePolygon);
    }
}
