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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class S2PointTest {

    @Test
    public void testGetInner() {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(11, 87);
        final S2Point s2Point = new S2Point(s2LatLng);

        final Object inner = s2Point.getInner();
        assertSame(s2LatLng, inner);
    }

    @Test
    public void testGetLon() {
        final S2Point s2Point = createS2Point(12, 88);

        assertEquals(88, s2Point.getLon(), 1e-8);
    }

    @Test
    public void testSetGetLon() {
        final S2Point s2Point = createS2Point(9.34, 11.665);

        s2Point.setLon(-88.23);
        assertEquals(-88.23, s2Point.getLon(), 1e-8);
    }

    @Test
    public void testGetLat() {
        final S2Point s2Point = createS2Point(13, 89);

        assertEquals(13, s2Point.getLat(), 1e-8);
    }

    @Test
    public void testSetGetLat() {
        final S2Point s2Point = createS2Point(10.34, 10.665);

        s2Point.setLat(22.667);
        assertEquals(22.667, s2Point.getLat(), 1e-8);
    }

    @Test
    public void testIsEmpty() {
        final S2Point s2Point = createS2Point(13, 89);

        assertFalse(s2Point.isEmpty());
    }

    @Test
    public void testGetCoordinates() {
        final S2Point s2Point = createS2Point(14, 90);

        final Point[] coordinates = s2Point.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(1, coordinates.length);
        assertEquals(90, coordinates[0].getLon(), 1e-8);
        assertEquals(14, coordinates[0].getLat(), 1e-8);
    }

    @Test
    public void testToString() {
        final S2Point s2Point = createS2Point(16, 88);

        assertEquals("POINT(88.0 16.0)", s2Point.toString());
    }

    private S2Point createS2Point(double latDegrees, double lngDegrees) {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(latDegrees, lngDegrees);
        return new S2Point(s2LatLng);
    }
}
