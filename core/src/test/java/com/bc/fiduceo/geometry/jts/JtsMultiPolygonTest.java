/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
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

public class JtsMultiPolygonTest {

    private MultiPolygon innerPolygon;
    private JTSMultiPolygon multiPolygon;
    private WKTReader wktReader;

    @Before
    public void setUp() {
        innerPolygon = mock(MultiPolygon.class);
        multiPolygon = new JTSMultiPolygon(innerPolygon);
        wktReader = new WKTReader();
    }

    @Test
    public void testIsEmpty() {
        when(innerPolygon.isEmpty()).thenReturn(true);

        assertTrue(multiPolygon.isEmpty());

        verify(innerPolygon, times(1)).isEmpty();
        verifyNoMoreInteractions(innerPolygon);
    }

    @Test
    public void testToString() {
        when(innerPolygon.toString()).thenReturn("inner-to-string");

        assertEquals("inner-to-string", multiPolygon.toString());
    }

    @Test
    public void testGetInner() {
        assertSame(innerPolygon, multiPolygon.getInner());
    }

    @Test
    public void testGetCoordinates() throws ParseException {
        final MultiPolygon innerPolygon = (MultiPolygon) wktReader.read("MULTIPOLYGON(((10 0, 10 2, 11 2, 11 0, 10 0)),((15 5, 15 7, 16 7, 16 5, 15 5)))");

        final JTSMultiPolygon jtsPolygon = new JTSMultiPolygon(innerPolygon);
        final Point[] coordinates = jtsPolygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(10, coordinates.length);

        assertEquals(10.0, coordinates[0].getLon(), 1e-8);
        assertEquals(0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(11.0, coordinates[2].getLon(), 1e-8);
        assertEquals(2.0, coordinates[2].getLat(), 1e-8);

        assertEquals(16.0, coordinates[7].getLon(), 1e-8);
        assertEquals(7.0, coordinates[7].getLat(), 1e-8);
    }

    @Test
    public void testShiftLon() throws ParseException {
        final MultiPolygon innerPolygon = (MultiPolygon) wktReader.read("MULTIPOLYGON(((11 0, 11 2, 12 2, 12 0, 11 0)),((15 5, 15 7, 16 7, 16 5, 15 5)))");

        final JTSMultiPolygon jtsPolygon = new JTSMultiPolygon(innerPolygon);
        jtsPolygon.shiftLon(-5);

        final Point[] coordinates = jtsPolygon.getCoordinates();
        assertEquals(6.0, coordinates[0].getLon(), 1e-8);
        assertEquals(0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(7.0, coordinates[2].getLon(), 1e-8);
        assertEquals(2.0, coordinates[2].getLat(), 1e-8);

        assertEquals(11.0, coordinates[7].getLon(), 1e-8);
        assertEquals(7.0, coordinates[7].getLat(), 1e-8);
    }

    @Test
    public void testIntersection_polygon() throws ParseException {
        final MultiPolygon innerPolygon = (MultiPolygon) wktReader.read("MULTIPOLYGON(((11 0, 11 2, 12 2, 12 0, 11 0)),((15 5, 15 7, 16 7, 16 5, 15 5)))");
        final JTSMultiPolygon jtsMultiPolygon = new JTSMultiPolygon(innerPolygon);

        final Polygon innerIntersectPolygon = (Polygon) wktReader.read("POLYGON((11 1, 11 3, 14 3, 14 1, 11 1))");
        final JTSPolygon jtsIntersectPolygon = new JTSPolygon(innerIntersectPolygon);

        final com.bc.fiduceo.geometry.Geometry intersection = jtsMultiPolygon.intersection(jtsIntersectPolygon);
        assertNotNull(intersection);
        assertTrue(intersection instanceof JTSPolygon);

        assertEquals("POLYGON ((11 1, 11 2, 12 2, 12 1, 11 1))", intersection.toString());
    }
}
