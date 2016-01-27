package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by tom.bc on 1/27/2016.
 */
public class S2PolygonTest {

    private S2WKTReader s2WKTReader;

    @Before
    public void setUp() {
        s2WKTReader = new S2WKTReader();
    }

    @Test
    public void testIsEmpty() {
        com.google.common.geometry.S2Polygon googlePolygon = new com.google.common.geometry.S2Polygon();
        final S2Polygon s2Polygon = new S2Polygon(googlePolygon);

        assertTrue(s2Polygon.isEmpty());
    }

    @Test
    public void testIntersect_noIntersection() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((5 0, 5 1, 4 1, 4 0, 5 0))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersect_intersectionWest() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final S2Polygon s2Polygon_2 =  createS2Polygon("POLYGON((-5.5 0, -5.5 1, -4.5 1, -4.5 0, -5.5 0))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(5, coordinates.length);
        assertEquals(-4.5, coordinates[0].getLon(), 1e-8);
        assertEquals(1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(-5.0, coordinates[2].getLon(), 1e-8);
        assertEquals(1.0, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testGetCoordinates() {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");

        final Point[] coordinates = s2Polygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(4, coordinates.length);
        assertEquals(5.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(4.0, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);
    }

    private S2Polygon createS2Polygon(String wellKnownText) {
        com.google.common.geometry.S2Polygon polygon_1 = (com.google.common.geometry.S2Polygon) s2WKTReader.read(wellKnownText);
        return new S2Polygon(polygon_1);
    }
}
