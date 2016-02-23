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

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class S2GeometryFactoryTest {

    private GeometryFactory factory;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testParsePolygon() {
        final Geometry geometry = factory.parse("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof S2Polygon);

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
        assertTrue(geometry instanceof S2LineString);

        Point[] coordinates = geometry.getCoordinates();
        assertEquals(coordinates[0].getLon(), 1.9999999999999996, 1e-8);
        assertEquals(coordinates[0].getLat(), 1.0, 1e-8);

        assertEquals(coordinates[3].getLon(), 5.0, 1e-8);
        assertEquals(coordinates[3].getLat(), 4.0, 1e-8);
    }

    @Test
    public void testParseMultiPoylgon() {
        S2MultiPolygon s2MultiPolygon = (S2MultiPolygon) factory.parse("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        assertNotNull(s2MultiPolygon);

        Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertEquals(6, coordinates.length);
        assertEquals(coordinates[0].toString(), "POINT(29.999999999999993 20.0)");
        assertEquals(coordinates[1].toString(), "POINT(100.0 10.0)");
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
        points.add(factory.createPoint(3, 1));
        final Polygon polygon = factory.createPolygon(points);

        final String wkt = factory.format(polygon);
        assertEquals("POLYGON((3.0000000000000004 1.0,4.0 2.0,5.0 0.9999999999999998,3.0000000000000004 1.0,3.0000000000000004 1.0))", wkt);
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
    public void testCreatePolygonFromPoints() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(2, 2));
        points.add(factory.createPoint(4, 2));
        points.add(factory.createPoint(4, 4));
        points.add(factory.createPoint(2, 4));
        points.add(factory.createPoint(2, 2));

        final Polygon polygon = factory.createPolygon(points);
        assertNotNull(polygon);
        assertEquals("Polygon: (1) loops:\n" +
                             "loop <\n" +
                             "(1.9999999999999996, 2.0)\n" +
                             "(2.0, 4.0)\n" +
                             "(4.0, 4.0)\n" +
                             "(4.0, 2.0)\n" +
                             "(1.9999999999999996, 2.0)\n" +
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

        // @todo 3 tb/tb invent some test here to verify the correctness of creation 2015-12-01
        //assertEquals("bla", geometry.checkSensorTypeName());
    }
}
