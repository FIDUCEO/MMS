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

import com.bc.fiduceo.geometry.Point;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
    public void testGetIntersection() {
        final ArrayList<S2Point> vertices = new ArrayList<>();
        vertices.add(new S2Point(0.1, 0.2, 0.5));
        final S2Polyline innerLineString = new S2Polyline(vertices);
        final BcS2LineString bcS2LineString = new BcS2LineString(innerLineString);

        final BcS2Point point = new BcS2Point(new S2LatLng());

        try {
            bcS2LineString.getIntersection(point);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

    }
}
