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

package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.*;
import com.google.common.geometry.S2Point;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class BcS2GeometryFactoryTest {

    private GeometryFactory factory;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testParsePolygon() {
        final Geometry geometry = factory.parse("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof BcS2Polygon);

        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(6.0, 1.9999999999999996)\n" +
                "(4.999999999999999, 1.9999999999999996)\n" +
                "(4.0, 2.0)\n" +
                "(3.0000000000000004, 1.9999999999999996)\n" +
                "(3.000000000000001, 3.0000000000000004)\n" +
                "(4.0, 3.0000000000000004)\n" +
                "(4.999999999999999, 3.0000000000000004)\n" +
                "(6.000000000000001, 3.0000000000000004)\n" +
                ">\n", geometry.toString());
    }

    @Test
    public void testParseLineString() {
        final Geometry geometry = factory.parse("LINESTRING(2 1, 3 2, 4 3, 5 4)");
        assertNotNull(geometry);
        assertTrue(geometry instanceof BcS2LineString);

        final Point[] coordinates = geometry.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals(coordinates[0].getLon(), 1.9999999999999996, 1e-8);
        assertEquals(coordinates[0].getLat(), 1.0, 1e-8);

        assertEquals(coordinates[3].getLon(), 5.0, 1e-8);
        assertEquals(coordinates[3].getLat(), 4.0, 1e-8);
    }

    @Test
    public void testMultiLineString() {
        final Geometry geometry = factory.parse("MULTILINESTRING((0 1, 1 1), (1 4, 3 4))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof BcS2MultiLineString);

        Point[] coordinates = geometry.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals(0.0, coordinates[0].getLon(), 1e-8);
        assertEquals( 1.0, coordinates[0].getLat(),1e-8);

        assertEquals( 3.0, coordinates[3].getLon(),1e-8);
        assertEquals( 4.0, coordinates[3].getLat(),1e-8);
    }

    @Test
    public void testParseMultiPolygon() {
        BcS2MultiPolygon bcS2MultiPolygon = (BcS2MultiPolygon) factory.parse("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        assertNotNull(bcS2MultiPolygon);

        Point[] coordinates = bcS2MultiPolygon.getCoordinates();
        assertEquals(9, coordinates.length);
        assertEquals(coordinates[0].toString(), "POINT(29.999999999999993 20.0)");
        assertEquals(coordinates[1].toString(), "POINT(100.0 10.0)");
    }

    @Test
    public void testParse_Unsupported() {
        try {
            factory.parse("GEOMETRYCOLLECTION()");
            fail("IllegalArgumentException expected");
        } catch (RuntimeException expected) {
        }

        try {
            factory.parse("LINEARRING(295895.3238300492 2251783.230814348, 296907.69382697035 2251783.230814348, 296907.69382697035 2252680.3463808503, 295895.3238300492 2252680.3463808503, 295895.3238300492 2251783.230814348 )");
            fail("IllegalArgumentException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testFormat_point() {
        final Point point = factory.createPoint(34.8, -72.44);

        final String wkt = factory.format(point);
        assertEquals("POINT(34.8,-72.44)", wkt);
    }

    @Test
    public void testFormat_lineString() {
        final ArrayList<Point> points = new ArrayList<>();
        points.add(factory.createPoint(3, 1));
        points.add(factory.createPoint(4, 2));
        points.add(factory.createPoint(5, 1));
        final LineString lineString = factory.createLineString(points);

        final String wkt = factory.format(lineString);
        assertEquals("LINESTRING(3.0000000000000004 1.0,4.0 2.0,5.0 0.9999999999999998)", wkt);
    }

    @Test
    public void testFormat_polygon() {
        final ArrayList<Point> points = new ArrayList<>();
        points.add(factory.createPoint(3, 1));
        points.add(factory.createPoint(4, 2));
        points.add(factory.createPoint(5, 1));
        final Polygon polygon = factory.createPolygon(points);

        final String wkt = factory.format(polygon);
        assertEquals("POLYGON((3.0000000000000004 1.0,4.0 2.0,5.0 0.9999999999999998,3.0000000000000004 1.0))", wkt);
    }

    @Test
    public void testCreatePoint() {
        Point point = factory.createPoint(22.89, -12.45);
        assertNotNull(point);
        assertEquals(22.89, point.getLon(), 1e-8);
        assertEquals(-12.45, point.getLat(), 1e-8);

        point = factory.createPoint(-107.335, 20.97);
        assertNotNull(point);
        assertEquals(-107.335, point.getLon(), 1e-8);
        assertEquals(20.97, point.getLat(), 1e-8);
    }

    @Test
    public void testCreatePolygonFromPoints_wktClosed() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(2, 2));
        points.add(factory.createPoint(4, 2));
        points.add(factory.createPoint(4, 4));
        points.add(factory.createPoint(2, 4));
        points.add(factory.createPoint(2, 2));

        final Polygon polygon = factory.createPolygon(points);
        assertTrue(polygon.isValid());
        assertNotNull(polygon);
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(1.9999999999999996, 2.0)\n" +
                "(2.0, 4.0)\n" +
                "(4.0, 4.0)\n" +
                "(4.0, 2.0)\n" +
                ">\n", polygon.toString());
    }

    @Test
    public void testCreatePolygonFromPoints_wktNotClosed() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(2, 2));
        points.add(factory.createPoint(4, 2));
        points.add(factory.createPoint(4, 4));
        points.add(factory.createPoint(2, 4));

        final Polygon polygon = factory.createPolygon(points);
        assertNotNull(polygon);
        assertTrue(polygon.isValid());
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(1.9999999999999996, 2.0)\n" +
                "(2.0, 4.0)\n" +
                "(4.0, 4.0)\n" +
                "(4.0, 2.0)\n" +
                ">\n", polygon.toString());
    }

    @Test
    public void testCreateLineStringFromPoints() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(-106, 8));
        points.add(factory.createPoint(-108, 8.2));
        points.add(factory.createPoint(-109.3, 8.7));

        final LineString lineString = factory.createLineString(points);
        assertNotNull(lineString);

        final Point[] coordinates = lineString.getCoordinates();
        assertEquals(3, coordinates.length);

        assertEquals(-106.0, coordinates[0].getLon(), 1e-8);
        assertEquals(8.0, coordinates[0].getLat(), 1e-8);
        assertEquals(-109.3, coordinates[2].getLon(), 1e-8);
        assertEquals(8.7, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testCreateMultiPolygonFromPolygonList() {
        final List<Polygon> polygonList = new ArrayList<>();
        polygonList.add((Polygon) factory.parse("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))"));
        polygonList.add((Polygon) factory.parse("POLYGON((2 0, 1 1, 3 1, 3 0, 2 0))"));

        final MultiPolygon multiPolygon = factory.createMultiPolygon(polygonList);
        assertNotNull(multiPolygon);

        final Point[] coordinates = multiPolygon.getCoordinates();
        assertEquals(10, coordinates.length);

        assertEquals(0.0, coordinates[2].getLon(), 1e-8);
        assertEquals(1.0, coordinates[2].getLat(), 1e-8);

        assertEquals(3.0, coordinates[6].getLon(), 1e-8);
        assertEquals(1.0, coordinates[6].getLat(), 1e-8);
    }

    @Test
    public void testCreateTimeAxis() {
        final LineString lineString = (LineString) factory.parse("LINESTRING(0 0, 0 1, 1 1, 2 1)");

        final TimeAxis timeAxis = factory.createTimeAxis(lineString, new Date(100000000000L), new Date(101000000000L));
        assertNotNull(timeAxis);
        final Date time = timeAxis.getTime(factory.createPoint(0.5, 1.0));
        assertEquals(100500025387L, time.getTime());
    }

    @Test
    public void testToStorageFormat_point() {
        final Geometry point = factory.parse("POINT(-22.5 67.23)");

        final byte[] storageFormat = factory.toStorageFormat(point);
        assertEquals("POINT(-22.500000000000004,67.23)", new String(storageFormat));
    }

    @Test
    public void testToStorageFormat_lineString() {
        final Geometry point = factory.parse("LINESTRING(1 8, 2 8.5, 3 8.7)");

        final byte[] storageFormat = factory.toStorageFormat(point);
        assertEquals("LINESTRING(1.0 7.999999999999998,2.0 8.5,3.0000000000000004 8.700000000000001)", new String(storageFormat));
    }

    @Test
    public void testToStorageFormat_polygon() {
        final Geometry point = factory.parse("POLYGON((1 8, 2 8.5, 3 8.2, 1 8))");

        final byte[] storageFormat = factory.toStorageFormat(point);
        assertEquals("POLYGON((3.0000000000000004 8.2,2.0 8.5,1.0 7.999999999999998,3.0000000000000004 8.2))", new String(storageFormat));
    }

    @Test
    public void testFromStorageFormat_point() {
        final String pointWkt = "POINT(-22.5 67.23)";

        final Geometry pointGeometry = factory.fromStorageFormat(pointWkt.getBytes());
        assertTrue(pointGeometry instanceof Point);

        final Point[] coordinates = pointGeometry.getCoordinates();
        assertEquals(1, coordinates.length);
        assertEquals(-22.5, coordinates[0].getLon(), 1e-8);
        assertEquals(67.23, coordinates[0].getLat(), 1e-8);
    }

    @Test
    public void testFromStorageFormat_lineString() {
        final String lineStringWkt = "LINESTRING(1 8, 2 8.5, 3 8.7)";

        final Geometry lineStringGeometry = factory.fromStorageFormat(lineStringWkt.getBytes());
        assertTrue(lineStringGeometry instanceof LineString);

        final Point[] coordinates = lineStringGeometry.getCoordinates();
        assertEquals(3, coordinates.length);
        assertEquals(1, coordinates[0].getLon(), 1e-8);
        assertEquals(8, coordinates[0].getLat(), 1e-8);
        assertEquals(3, coordinates[2].getLon(), 1e-8);
        assertEquals(8.7, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testFromStorageFormat_polygon() {
        final String polygonWkt = "POLYGON((1 8, 2 8.5, 3 8.2, 1 8))";

        final Geometry polygonGeometry = factory.fromStorageFormat(polygonWkt.getBytes());
        assertTrue(polygonGeometry instanceof Polygon);

        final Point[] coordinates = polygonGeometry.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals(3, coordinates[0].getLon(), 1e-8);
        assertEquals(8.2, coordinates[0].getLat(), 1e-8);
        assertEquals(1, coordinates[2].getLon(), 1e-8);
        assertEquals(8, coordinates[2].getLat(), 1e-8);
        assertEquals(3, coordinates[3].getLon(), 1e-8);
        assertEquals(8.2, coordinates[3].getLat(), 1e-8);
    }

    @Test
    public void testExtractS2Points_emptyList() {
        final List<Point> points = new ArrayList<>();

        final List<S2Point> s2Points = BcS2GeometryFactory.extractS2Points(points);
        assertEquals(0, s2Points.size());
    }

    @Test
    public void testExtractS2Points() {
        final List<Point> points = new ArrayList<>();
        points.add(factory.createPoint(23, 34));
        points.add(factory.createPoint(33, 44));
        points.add(factory.createPoint(43, 54));

        final List<S2Point> s2Points = BcS2GeometryFactory.extractS2Points(points);
        assertEquals(3, s2Points.size());
        assertEquals("(44.0, 33.0)", s2Points.get(1).toDegreesString());
    }
}
