package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.AbstractGeometryFactory;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.List;

public class JtsFactory implements AbstractGeometryFactory {

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;

    public JtsFactory() {
        wktReader = new WKTReader();
        geometryFactory = new GeometryFactory();
    }

    @Override
    public Geometry parse(String wkt) {
        final com.vividsolutions.jts.geom.Geometry geometry;
        try {
            geometry = wktReader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            return new JTSPolygon((com.vividsolutions.jts.geom.Polygon) geometry);
        } else if (geometry instanceof com.vividsolutions.jts.geom.LineString) {
            return new JTSLineString((com.vividsolutions.jts.geom.LineString) geometry);
        }

        throw new RuntimeException("Unsupported geometry type");
    }

    @Override
    public Point createPoint(double lon, double lat) {
        final Coordinate coordinate = new Coordinate(lon, lat);
        return new JTSPoint(coordinate);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = (Coordinate) point.getInner();
        }

        com.vividsolutions.jts.geom.Polygon jtsPolygon = geometryFactory.createPolygon(coordinates);
        return new JTSPolygon(jtsPolygon);
    }
}
