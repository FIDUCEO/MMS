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
import com.vividsolutions.jts.geom.LineString;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class JTSLineStringTest {

    private LineString innerLineString;
    private JTSLineString lineString;

    @Before
    public void setUp() {
        innerLineString = mock(LineString.class);
        lineString = new JTSLineString(innerLineString);
    }

    @Test
    public void testIsEmpty() {
        when(innerLineString.isEmpty()).thenReturn(true);

        assertTrue(lineString.isEmpty());

        verify(innerLineString, times(1)).isEmpty();
        verifyNoMoreInteractions(innerLineString);
    }

    @Test
    public void testToString(){
        when(innerLineString.toString()).thenReturn("yepp!");

        assertEquals("yepp!", lineString.toString());
    }

    @Test
    public void testGetInner() {
        assertSame(innerLineString, lineString.getInner());
    }

    @Test
    public void testGetPoints() {
        final Coordinate[] innerCoordinates = new Coordinate[3];
        innerCoordinates[0] = new Coordinate(21, -5);
        innerCoordinates[1] = new Coordinate(22, -6);
        innerCoordinates[2] = new Coordinate(23, -8);

        when(innerLineString.getCoordinates()).thenReturn(innerCoordinates);

        final Point[] coordinates = lineString.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(3, coordinates.length);

        assertEquals(21, coordinates[0].getLon(), 1e-8);
        assertEquals(-5, coordinates[0].getLat(), 1e-8);

        assertEquals(23, coordinates[2].getLon(), 1e-8);
        assertEquals(-8, coordinates[2].getLat(), 1e-8);
    }
}
