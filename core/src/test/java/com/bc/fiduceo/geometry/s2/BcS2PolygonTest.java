package com.bc.fiduceo.geometry.s2;

import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Polygon;
import org.junit.*;

import java.io.IOException;

/**
 * @author tom.bc
 */
public class BcS2PolygonTest {

    private S2WKTReader s2WKTReader;

    @Before
    public void setUp() throws IOException {
        s2WKTReader = new S2WKTReader();
    }

    @Test
    public void testIsEmpty() {
        S2Polygon googlePolygon = new S2Polygon();
        final BcS2Polygon bcS2Polygon = new BcS2Polygon(googlePolygon);

        assertTrue(bcS2Polygon.isEmpty());
    }

    @Test
    public void testIntersect_noIntersection() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((5 0, 5 1, 4 1, 4 0, 5 0))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersect_intersectionWest() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((-5.5 0, -5.5 1, -4.5 1, -4.5 0, -5.5 0))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
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
    public void testIntersect_intersectionNorth() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
        Point[] coordinates = intersection.getCoordinates();
        assertEquals(9.0, coordinates[0].getLon(), 1e-8);
        assertEquals(10.028657322246222, coordinates[0].getLat(), 1e-8);
        assertEquals(-8.0, coordinates[2].getLon(), 1e-8);
        assertEquals(-10.0, coordinates[2].getLat(), 1e-8);

    }

    @Test
    public void testIntersect_intersectionSouth() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((-8 -12,-8 10,9 10,9 -12,-8 -12))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testIntersect_intersectionEast() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((-10 -8,12 -8,12 9,-10 9,-10 -8))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testSamePolygon() {
        final BcS2Polygon bcS2Polygon_1 = createS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createS2Polygon("POLYGON((10 10,-10 10,-10 -10,10 -10,10 10))");

        Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertEquals(intersection.toString(), "Polygon: (0) loops:\n");
    }

    @Test
    public void testGetCoordinates() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");

        final Point[] coordinates = bcS2Polygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(4, coordinates.length);
        assertEquals(5.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(4.0, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testIsValid_valid() throws Exception {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");
        assertEquals(true, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_selfIntersectingPolygon() throws Exception {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON((0 0, 4 0, 4 3, 1 3, 3 1, 2 1, 2 4, 0 4, 0 0))");
        assertEquals(false, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToValid_polygonIsADonut() throws Exception {
        // the donut polygon does not conform the ogc wkt-specification, the inner loop has to be in clockwise order.
        // We suspect google S2 library spacific behavior.  tb 24.2.2016
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(1 2, 4 2, 2 4, 1 2))");
        assertEquals(true, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_polygonIsADamagedDonut() throws Exception {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(4 2, 7 2, 5 4, 4 2))");
        assertEquals(false, bcS2Polygon.isValid());
    }

    private BcS2Polygon createS2Polygon(String wellKnownText) {
        S2Polygon polygon = (S2Polygon) s2WKTReader.read(wellKnownText);
        return new BcS2Polygon(polygon);
    }
}
