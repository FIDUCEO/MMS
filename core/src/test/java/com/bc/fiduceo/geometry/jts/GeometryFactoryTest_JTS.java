
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

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeometryFactoryTest_JTS {

    private GeometryFactory factory;
    private WKBReader wkbReader;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.JTS);
        wkbReader = new WKBReader();
    }

    @Test
    public void testParsePolygon() {
        final Geometry geometry = factory.parse("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSPolygon);

        assertEquals("POLYGON ((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))", geometry.toString());
    }

    @Test
    public void testParseLineString() {
        final Geometry geometry = factory.parse("LINESTRING(3 1, 3 2, 3 3, 3 4)");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSLineString);

        assertEquals("LINESTRING (3 1, 3 2, 3 3, 3 4)", geometry.toString());
    }

    @Test
    public void testParsePoint() {
        final Geometry geometry = factory.parse("POINT(4 0)");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSPoint);

        final JTSPoint point = (JTSPoint) geometry;

        assertEquals(4.0, point.getLon(), 1e-8);
        assertEquals(0.0, point.getLat(), 1e-8);
    }

    @Test
    public void testToStorageFormat_point() throws ParseException {
        final Geometry pointGeometry = factory.parse("POINT(6 -1)");

        final byte[] bytes = factory.toStorageFormat(pointGeometry);
        assertNotNull(bytes);
        assertEquals(21, bytes.length);

        final com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(bytes);
        assertTrue(geometry instanceof com.vividsolutions.jts.geom.Point);
        final com.vividsolutions.jts.geom.Point jtsPoint = (com.vividsolutions.jts.geom.Point) geometry;
        assertEquals(6.0, jtsPoint.getX(), 1e-8);
        assertEquals(-1.0, jtsPoint.getY(), 1e-8);
    }

    @Test
    public void testToStorageFormat_lineString() throws ParseException {
        final Geometry lineStringGeometry = factory.parse("LINESTRING (-108 11, -107 12, -106 13)");

        final byte[] bytes = factory.toStorageFormat(lineStringGeometry);
        assertNotNull(bytes);
        assertEquals(57, bytes.length);

        final com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(bytes);
        assertTrue(geometry instanceof com.vividsolutions.jts.geom.LineString);
        final com.vividsolutions.jts.geom.LineString jtsLineString = (com.vividsolutions.jts.geom.LineString) geometry;

        Coordinate coordinate = jtsLineString.getCoordinateN(0);
        assertEquals(-108.0, coordinate.x, 1e-8);
        assertEquals(11.0, coordinate.y, 1e-8);

        coordinate = jtsLineString.getCoordinateN(2);
        assertEquals(-106.0, coordinate.x, 1e-8);
        assertEquals(13.0, coordinate.y, 1e-8);
    }

    @Test
    public void testToStorageFormat_polygon() throws ParseException {
        final Geometry polygonGeometry = factory.parse("POLYGON((-8 0, -7 0, -7 1, -8 1, -8 0))");

        final byte[] bytes = factory.toStorageFormat(polygonGeometry);
        assertNotNull(bytes);
        assertEquals(93, bytes.length);

        final com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(bytes);
        assertTrue(geometry instanceof com.vividsolutions.jts.geom.Polygon);

        final com.vividsolutions.jts.geom.Polygon jtsPolygon = (com.vividsolutions.jts.geom.Polygon) geometry;
        final Coordinate[] coordinates = jtsPolygon.getCoordinates();
        assertEquals(5, coordinates.length);

        assertEquals(-8.0, coordinates[0].x, 1e-8);
        assertEquals(0.0, coordinates[0].y, 1e-8);

        assertEquals(-7.0, coordinates[2].x, 1e-8);
        assertEquals(1.0, coordinates[2].y, 1e-8);
    }

    @Test
    public void testFromStorageFormat_point() {
        final Geometry pointGeometry = factory.parse("POINT(7 -2)");

        final byte[] storageFormat = factory.toStorageFormat(pointGeometry);
        final Geometry geometry = factory.fromStorageFormat(storageFormat);
        assertNotNull(geometry);
        assertTrue(geometry instanceof Point);

        final Point point = (Point) geometry;
        assertEquals(7.0, point.getLon(), 1e-8);
        assertEquals(-2.0, point.getLat(), 1e-8);
    }

    @Test
    public void testFromStorageFormat_lineString() {
        final Geometry lineStringGeometry = factory.parse("LINESTRING (-109 10, -108 11, -107 12)");

        final byte[] storageFormat = factory.toStorageFormat(lineStringGeometry);
        final Geometry geometry = factory.fromStorageFormat(storageFormat);
        assertNotNull(geometry);
        assertTrue(geometry instanceof LineString);

        final LineString lineString = (LineString) geometry;
        final Point[] coordinates = lineString.getCoordinates();
        assertEquals(3, coordinates.length);

        assertEquals(-109, coordinates[0].getLon(), 1e-8);
        assertEquals(10, coordinates[0].getLat(), 1e-8);

        assertEquals(-107, coordinates[2].getLon(), 1e-8);
        assertEquals(12, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testFromStorageFormat_polygon() {
        final Geometry polygonGeometry = factory.parse("POLYGON((-9 1, -8 1, -8 2, -9 2, -9 1))");

        final byte[] storageFormat = factory.toStorageFormat(polygonGeometry);
        final Geometry geometry = factory.fromStorageFormat(storageFormat);
        assertNotNull(geometry);
        assertTrue(geometry instanceof Polygon);

        final Polygon polygon = (Polygon) geometry;
        final Point[] coordinates = polygon.getCoordinates();
        assertEquals(5, coordinates.length);

        assertEquals(-9, coordinates[0].getLon(), 1e-8);
        assertEquals(1, coordinates[0].getLat(), 1e-8);

        assertEquals(-8, coordinates[2].getLon(), 1e-8);
        assertEquals(2, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testCreatePoint() {
        Point point = factory.createPoint(11.78, -23.56);
        assertNotNull(point);
        assertEquals(11.78, point.getLon(), 1e-8);
        assertEquals(-23.56, point.getLat(), 1e-8);

        point = factory.createPoint(-106.224, 19.86);
        assertNotNull(point);
        assertEquals(-106.224, point.getLon(), 1e-8);
        assertEquals(19.86, point.getLat(), 1e-8);
    }

    @Test
    public void testCreatePolygonFromPoints() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(0, 0));
        points.add(factory.createPoint(1, 0));
        points.add(factory.createPoint(1, 1));
        points.add(factory.createPoint(0, 1));
        points.add(factory.createPoint(0, 0));

        final Polygon polygon = factory.createPolygon(points);
        assertNotNull(polygon);
        assertEquals("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))", polygon.toString());
    }

    @Test
    public void testCreateLineStringFromPoints() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(11, -3));
        points.add(factory.createPoint(11.4, -3.5));
        points.add(factory.createPoint(12, -4.1));

        final LineString lineString = factory.createLineString(points);
        assertNotNull(lineString);
        assertEquals("LINESTRING (11 -3, 11.4 -3.5, 12 -4.1)", lineString.toString());
    }
}
