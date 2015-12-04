
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.Date;
import java.util.List;

public class JtsGeometryFactory implements AbstractGeometryFactory {

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;

    public JtsGeometryFactory() {
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
        } else if (geometry instanceof com.vividsolutions.jts.geom.Point) {
            return new JTSPoint(geometry.getCoordinate());
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

    @Override
    public TimeAxis createTimeAxis(LineString lineString, Date startTime, Date endTime) {
        final com.vividsolutions.jts.geom.LineString jtsLineString = (com.vividsolutions.jts.geom.LineString) lineString.getInner();
        return new JTSTimeAxis(jtsLineString, startTime, endTime);
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
