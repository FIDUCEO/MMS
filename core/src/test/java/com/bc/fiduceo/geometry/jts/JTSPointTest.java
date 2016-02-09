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


import com.bc.fiduceo.geometry.Point;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class JTSPointTest {

    private static final double LON = 23.44;
    private static final double LAT = 34.109;

    private Coordinate coordinate;
    private JTSPoint jtsPoint;

    @Before
    public void setUp() {
        coordinate = new Coordinate(LON, LAT);
        jtsPoint = new JTSPoint(coordinate);
    }

    @Test
    public void testGetInner() {
        final Object inner = jtsPoint.getInner();
        assertSame(coordinate, inner);
    }

    @Test
    public void testIsEmpty() {
        assertFalse(jtsPoint.isEmpty());
    }

    @Test
    public void testGetLon() {
        assertEquals(LON, jtsPoint.getLon(), 1e-8);
    }

    @Test
    public void testSetGetLon() {
        final double lon = 23.9987;

        jtsPoint.setLon(lon);
        assertEquals(lon, jtsPoint.getLon(), 1e-8);
    }

    @Test
    public void testGetLat() {
        assertEquals(LAT, jtsPoint.getLat(), 1e-8);
    }

    @Test
    public void testSetGetLat() {
        final double lat = -11.6334;

        jtsPoint.setLat(lat);
        assertEquals(lat, jtsPoint.getLat(), 1e-8);
    }

    @Test
    public void testGetCoordinates() {
        final Point[] coordinates = jtsPoint.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(1, coordinates.length);
        assertEquals(LON, coordinates[0].getLon(), 1e-8);
        assertEquals(LAT, coordinates[0].getLat(), 1e-8);
    }

    @Test
    public void testEquals() {
        assertTrue(jtsPoint.equals(jtsPoint));

        final JTSPoint equalPoint = new JTSPoint(new Coordinate(LON, LAT));
        assertTrue(jtsPoint.equals(equalPoint));
        assertTrue(equalPoint.equals(jtsPoint));

        final JTSPoint notEqualPoint = new JTSPoint(new Coordinate(LON + 0.000001, LAT - 0.0000001));
        assertFalse(jtsPoint.equals(notEqualPoint));
        assertFalse(notEqualPoint.equals(jtsPoint));
    }
}
