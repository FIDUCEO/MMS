package com.bc.fiduceo.geometry.s2;

import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import org.junit.*;

import java.io.IOException;

/**
 * @author tom.bc
 */
public class S2PolygonTest {

    private S2WKTReader s2WKTReader;

    @Before
    public void setUp() throws IOException {
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
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-5.5 0, -5.5 1, -4.5 1, -4.5 0, -5.5 0))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(7, coordinates.length);
        assertEquals(-4.0, coordinates[0].getLon(), 1e-8);
        assertEquals(0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(-4.7499999999998925, coordinates[2].getLon(), 1e-8);
        assertEquals(1.0000285529444368, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testIntersect_intersectionNorth() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
        Point[] coordinates = intersection.getCoordinates();
        assertEquals(-9.999999999999998, coordinates[0].getLon(), 1e-8);
        assertEquals(10.0, coordinates[0].getLat(), 1e-8);
        assertEquals(9.999999999999998, coordinates[2].getLon(), 1e-8);
        assertEquals(-10.0, coordinates[2].getLat(), 1e-8);

    }

    @Test
    public void testIntersect_intersectionSouth() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-8 -12,-8 10,9 10,9 -12,-8 -12))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testIntersect_intersectionEast() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((-10 -8,12 -8,12 9,-10 9,-10 -8))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testSamePolygon() {
        final S2Polygon s2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final S2Polygon s2Polygon_2 = createS2Polygon("POLYGON((10 10,-10 10,-10 -10,10 -10,10 10))");

        Geometry intersection = s2Polygon_1.intersection(s2Polygon_2);
        assertNotNull(intersection);
        assertEquals(intersection.toString(), "Polygon: (0) loops:\n");
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

    @Test
    public void testIsValid_valid() throws Exception {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");
        assertEquals(true, s2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_selfIntersectingPolygon() throws Exception {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((0 0, 4 0, 4 3, 1 3, 3 1, 2 1, 2 4, 0 4, 0 0))");
        assertEquals(false, s2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToValid_polygonIsADonut() throws Exception {
        // the donut polygon does not conform the ogc wkt-specification, the inner loop has to be in clockwise order.
        // We suspect google S2 library spacific behavior.  tb 24.2.2016
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(1 2, 4 2, 2 4, 1 2))");
        assertEquals(true, s2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_polygonIsADamagedDonut() throws Exception {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(4 2, 7 2, 5 4, 4 2))");
        assertEquals(false, s2Polygon.isValid());
    }

    private S2Polygon createS2Polygon(String wellKnownText) {
        com.google.common.geometry.S2Polygon polygon = (com.google.common.geometry.S2Polygon) s2WKTReader.read(wellKnownText);
        return new S2Polygon(polygon);
    }
}
