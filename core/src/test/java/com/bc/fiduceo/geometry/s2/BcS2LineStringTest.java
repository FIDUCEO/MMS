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
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BcS2LineStringTest {

    @Test
    public void testIsEmpty_empty() {
        final S2Polyline innerLineString = new S2Polyline(new ArrayList<>());
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        assertTrue(bcS2LineString.isEmpty());
    }

    @Test
    public void testIsEmpty_notEmpty() {
        final ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(new S2Point());
        final S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        assertFalse(bcS2LineString.isEmpty());
    }

    @Test
    public void testToString() {
        final ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(new S2Point(0.1, 0.2, 0.5));
        vertices.add(new S2Point(0.3, 0.9, 0.8));
        final S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        assertEquals("LINESTRING(63.43494882292201 65.90515744788931,71.56505117707799 40.14006614878386)",
                bcS2LineString.toString());
    }

    @Test
    public void testGetCoordinates() {
        final ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(-11.3, 22.6).toPoint());
        vertices.add(S2LatLng.fromDegrees(-11.6, 21.5).toPoint());
        final S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        final Point[] coordinates = bcS2LineString.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(2, coordinates.length);
        assertEquals(-11.3, coordinates[0].getLat(), 1e-8);
        assertEquals(22.6, coordinates[0].getLon(), 1e-8);
        assertEquals(-11.6, coordinates[1].getLat(), 1e-8);
        assertEquals(21.5, coordinates[1].getLon(), 1e-8);
    }

    @Test
    public void testGetInner() {
        final ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(new S2Point(0.1, 0.2, 0.5));
        final S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        assertNotNull(bcS2LineString.getInner());
        assertSame(innerLineString, bcS2LineString.getInner());
    }

    @Test
    public void testIsValidLineString() throws Exception {
        List<S2Point> vertices = new ArrayList<>();
        vertices.add(S2Point.normalize(new S2Point(1, -1.1, 0.8)));
        S2Polyline s2Polyline = new S2Polyline(vertices);
        BcS2LineString bcS2LineString = new BcS2LineString(s2Polyline);

        assertNotNull(bcS2LineString);
        assertTrue(bcS2LineString.isValid());
    }

    @Test
    public void testGetIntersection_point_notIntersecting() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(-1, 2).toPoint());
        vertices.add(S2LatLng.fromDegrees(1, 2).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        final BcS2Point bcS2Point = new BcS2Point(S2LatLng.fromDegrees(3, 3));

        final Geometry intersection = bcS2LineString.getIntersection(bcS2Point);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_point_intersecting() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(0, 2).toPoint());
        vertices.add(S2LatLng.fromDegrees(0, -2).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        final BcS2Point bcS2Point = new BcS2Point(S2LatLng.fromDegrees(0, 1));

        final Geometry intersection = bcS2LineString.getIntersection(bcS2Point);
        assertNotNull(intersection);
        assertEquals("POINT(1.0 0.0)", intersection.toString());
    }

    @Test
    public void testGetIntersection_lineString_intersecting() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(-1, 2).toPoint());
        vertices.add(S2LatLng.fromDegrees(3, 3).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(1, 1).toPoint());
        vertices.add(S2LatLng.fromDegrees(2, 3).toPoint());
        innerLineString = new S2Polyline(vertices);
        final BcS2LineString otherLineString = new BcS2LineString(innerLineString);

        final Geometry intersection = bcS2LineString.getIntersection(otherLineString);
        assertTrue(intersection instanceof BcS2Point);
        assertEquals("POINT(2.7139604154540273 1.857161476202161)", intersection.toString());
    }

    @Test
    public void testGetIntersection_lineString_twoIntersections() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(4, 2).toPoint());
        vertices.add(S2LatLng.fromDegrees(6, 3).toPoint());
        vertices.add(S2LatLng.fromDegrees(7, 1).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(4, 4).toPoint());
        vertices.add(S2LatLng.fromDegrees(8, 1).toPoint());
        innerLineString = new S2Polyline(vertices);
        final BcS2LineString otherLineString = new BcS2LineString(innerLineString);

        final Geometry intersection = bcS2LineString.getIntersection(otherLineString);
        assertTrue(intersection instanceof GeometryCollection);
        final Geometry[] geometries = ((GeometryCollection) intersection).getGeometries();
        assertEquals(2, geometries.length);
        assertEquals("POINT(2.802304313649126 5.605740396925363)", geometries[0].toString());
        assertEquals("POINT(2.2093772407623993 6.396718299253454)", geometries[1].toString());
    }

    @Test
    public void testGetIntersection_lineString_notIntersecting() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(-1, 2).toPoint());
        vertices.add(S2LatLng.fromDegrees(3, 1).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(0, 4).toPoint());
        vertices.add(S2LatLng.fromDegrees(3, 2).toPoint());
        innerLineString = new S2Polyline(vertices);
        final BcS2LineString otherLineString = new BcS2LineString(innerLineString);

        final Geometry intersection = bcS2LineString.getIntersection(otherLineString);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_polygon() {
        ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(S2LatLng.fromDegrees(-2, 3).toPoint());
        vertices.add(S2LatLng.fromDegrees(4, 5).toPoint());
        S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        final Polygon polygon = (Polygon) new GeometryFactory(GeometryFactory.Type.S2).parse("POLYGON((-5 -5, 5 -5, 5 5, -5 5, -5 -5))");

        final Geometry intersection = bcS2LineString.getIntersection(polygon);
        assertTrue(intersection.isValid());
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(2, coordinates.length);
        assertEquals("POINT(3.0000000000000004 -2.0)", coordinates[0].toString());
        assertEquals("POINT(4.999999999999999 3.999999999999998)", coordinates[1].toString());
    }
}
