
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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class GeometryFactoryTest_JTS {

    private GeometryFactory factory;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.JTS);
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
