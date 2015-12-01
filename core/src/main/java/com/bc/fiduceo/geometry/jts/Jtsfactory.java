package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.*;
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
        final Coordinate[] coordinates = extractCoordinates(points);

        com.vividsolutions.jts.geom.Polygon polygon = geometryFactory.createPolygon(coordinates);
        return new JTSPolygon(polygon);
    }

    @Override
    public LineString createLineString(List<Point> points) {
        final Coordinate[] coordinates = extractCoordinates(points);

        com.vividsolutions.jts.geom.LineString lineString = geometryFactory.createLineString(coordinates);
        return new JTSLineString(lineString);
    }

    private static Coordinate[] extractCoordinates(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = (Coordinate) point.getInner();
        }
        return coordinates;
    }
}
