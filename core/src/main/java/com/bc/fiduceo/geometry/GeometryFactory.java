package com.bc.fiduceo.geometry;

import com.bc.fiduceo.geometry.jts.JtsGeometryFactory;
import com.bc.fiduceo.geometry.s2.S2GeometryFactory;

import java.util.List;

public class GeometryFactory implements AbstractGeometryFactory {

    public enum Type {
        JTS,
        S2
    }

    private final AbstractGeometryFactory factoryImpl;

    public GeometryFactory(Type type) {
        if (type == Type.JTS) {
            factoryImpl = new JtsGeometryFactory();
        } else if (type == Type.S2) {
            factoryImpl = new S2GeometryFactory();
        } else {
            throw new IllegalArgumentException("unknown geometry factory type");
        }
    }

    public Geometry parse(String wkt) {
        return factoryImpl.parse(wkt);
    }

    @Override
    public Point createPoint(double lon, double lat) {
        return factoryImpl.createPoint(lon, lat);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        return factoryImpl.createPolygon(points);
    }

    @Override
    public LineString createLineString(List<Point> points) {
        return factoryImpl.createLineString(points);
    }
}
