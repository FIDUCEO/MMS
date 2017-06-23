package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.*;
import com.bc.geometry.s2.S2WKTReader;
import com.bc.geometry.s2.S2WKTWriter;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
    public void testGetIntersection_noIntersection() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((5 0, 5 1, 4 1, 4 0, 5 0))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_intersectionWest() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-5 0, -5 1, -4 1, -4 0, -5 0))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((-5.5 0, -5.5 1, -4.5 1, -4.5 0, -5.5 0))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(6, coordinates.length);
        assertEquals(-4.5, coordinates[0].getLon(), 1e-8);
        assertEquals(1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(-5.0, coordinates[2].getLon(), 1e-8);
        assertEquals(1.0, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testGetIntersection_intersectionNorth() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(9.0, coordinates[0].getLon(), 1e-8);
        assertEquals(10.028657322246222, coordinates[0].getLat(), 1e-8);
        assertEquals(-8.0, coordinates[2].getLon(), 1e-8);
        assertEquals(-10.0, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testGetIntersection_intersectionSouth() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((-8 -12,-8 10,9 10,9 -12,-8 -12))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_intersectionEast() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((-10 -8,12 -8,12 9,-10 9,-10 -8))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_samePolygon() {
        final BcS2Polygon bcS2Polygon_1 = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Polygon bcS2Polygon_2 = createBcS2Polygon("POLYGON((10 10,-10 10,-10 -10,10 -10,10 10))");

        final Geometry intersection = bcS2Polygon_1.getIntersection(bcS2Polygon_2);
        assertNotNull(intersection);
        assertEquals("Polygon: (0) loops:\n", intersection.toString());
    }

    @Test
    public void testGetIntersection_Point_contained() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Point point = createBcS2Point("POINT(0 0)");

        final Geometry intersection = polygon.getIntersection(point);
        assertNotNull(intersection);
        assertEquals("POINT(0.0 0.0)", intersection.toString());
    }

    @Test
    public void testGetIntersection_Point_outside() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10,-10 10,10 10,10 -10,-10 -10))");
        final BcS2Point point = createBcS2Point("POINT(-28 65)");

        final Geometry intersection = polygon.getIntersection(point);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_multiLineString_noIntersection() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2MultiLineString multiLineString = createBcS2MultiLineString("MULTILINESTRING((-30 -30, -30 -35), (80 76, 82 78))");

        final Geometry intersection = polygon.getIntersection(multiLineString);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_multiLineString_oneIntersection() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2MultiLineString multiLineString = createBcS2MultiLineString("MULTILINESTRING((-20 -5, 20 -5), (80 76, 82 78))");

        final Geometry intersection = polygon.getIntersection(multiLineString);
        assertTrue(intersection instanceof LineString);
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(2, coordinates.length);
        assertEquals("POINT(-9.999999999999998 -5.238747270748654)", coordinates[0].toString());
        assertEquals("POINT(9.999999999999998 -5.238747270748654)", coordinates[1].toString());
    }

    @Test
    public void testGetIntersection_multiLineString_twoIntersection() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2MultiLineString multiLineString = createBcS2MultiLineString("MULTILINESTRING((-20 -5, 20 -5), (-20 5, 30 5))");

        final Geometry intersection = polygon.getIntersection(multiLineString);
        assertTrue(intersection instanceof LineString);
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals("POINT(-9.999999999999998 -5.238747270748654)", coordinates[0].toString());
        assertEquals("POINT(9.999999999999998 -5.238747270748654)", coordinates[1].toString());
        assertEquals("POINT(-9.999999999999998 5.327071852971211)", coordinates[2].toString());
        assertEquals("POINT(9.999999999999998 5.492998762169851)", coordinates[3].toString());
    }

    @Test
    public void testGetIntersection_lineString_noIntersection() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2LineString lineString = createBcS2LineString("LINESTRING(-20 -20, -20 -25)");

        final Geometry intersection = polygon.getIntersection(lineString);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_lineString_oneIntersection() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2LineString lineString = createBcS2LineString("LINESTRING(-20 -3, 20 5)");

        final Geometry intersection = polygon.getIntersection(lineString);
        assertTrue(intersection instanceof LineString);
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(2, coordinates.length);
        assertEquals("POINT(-9.999999999999998 -0.9814422857241891)", coordinates[0].toString());
        assertEquals("POINT(9.999999999999998 3.0850405670714447)", coordinates[1].toString());
    }

    @Test
    public void testGetIntersection_lineString_twoIntersections() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final BcS2LineString lineString = createBcS2LineString("LINESTRING(-12 4, -4 14, 8 -12)");

        final Geometry intersection = polygon.getIntersection(lineString);
        assertTrue(intersection instanceof LineString);
        final Point[] coordinates = intersection.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals("POINT(-9.999999999999998 6.5625763293836314)", coordinates[0].toString());
        assertEquals("POINT(-7.214559017433774 10.072365739254874)", coordinates[1].toString());
        assertEquals("POINT(-2.16543386643938 10.143983344524166)", coordinates[2].toString());
        assertEquals("POINT(7.091947583745752 -10.07501579834513)", coordinates[3].toString());
    }

    @Test
    public void testGetIntersection_unsupportedGeometry() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(null);

        try {
            polygon.getIntersection(geometryCollection);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetCoordinates() {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");

        final Point[] coordinates = bcS2Polygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(5, coordinates.length);
        assertEquals(5.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-1.0, coordinates[0].getLat(), 1e-8);

        assertEquals(4.0, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);

        assertEquals(5.0, coordinates[4].getLon(), 1e-8);
        assertEquals(-1.0, coordinates[4].getLat(), 1e-8);
    }

    @Test
    public void testIsValid_valid() throws Exception {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((5 -1, 5 0, 4 0, 4 -1, 5 -1))");
        assertEquals(true, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_selfIntersectingPolygon() throws Exception {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((0 0, 4 0, 4 3, 1 3, 3 1, 2 1, 2 4, 0 4, 0 0))");
        assertEquals(false, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToValid_polygonIsADonut() throws Exception {
        // the donut polygon does not conform the ogc wkt-specification, the inner loop has to be in clockwise order.
        // We suspect google S2 library specific behavior.  tb 24.2.2016
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(1 2, 4 2, 2 4, 1 2))");
        assertEquals(true, bcS2Polygon.isValid());
    }

    @Test
    public void testIsValid_resolvesToInvalid_polygonIsADamagedDonut() throws Exception {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(4 2, 7 2, 5 4, 4 2))");
        assertEquals(false, bcS2Polygon.isValid());
    }

    @Test
    public void testGetDifference_noIntersection() {
        final BcS2Polygon s2Polygon_1 = createBcS2Polygon("POLYGON((2 1, 6 1, 7 3, 4 3, 2 1))");
        final BcS2Polygon s2Polygon_2 = createBcS2Polygon("POLYGON((1 -2, 4 -2, 3 -1, 2 -1, 1 -2))");

        final Polygon difference = s2Polygon_1.getDifference(s2Polygon_2);
        assertFalse(difference.isEmpty());

        assertEquals("POLYGON((4.0 3.0000000000000004,1.9999999999999996 1.0,6.0 1.0,7.0 3.0000000000000004,4.0 3.0000000000000004))",
                S2WKTWriter.write(difference.getInner()));
    }

    @Test
    public void testGetDifference_intersectionNorth() {
        final BcS2Polygon s2Polygon_1 = createBcS2Polygon("POLYGON((2 1, 6 1, 7 3, 4 3, 2 1))");
        final BcS2Polygon s2Polygon_2 = createBcS2Polygon("POLYGON((5 2, 6 2, 6 4, 5 4, 5 2))");

        final Polygon difference = s2Polygon_1.getDifference(s2Polygon_2);
        assertFalse(difference.isEmpty());

        assertEquals("POLYGON((4.999999999999999 2.0,4.999999999999997 3.0009124369434743,4.0 3.0000000000000004,1.9999999999999996 1.0,6.0 1.0,7.0 3.0000000000000004,6.000000000000001 3.0009124369434748,6.0 2.0,4.999999999999999 2.0))",
                S2WKTWriter.write(difference.getInner()));
    }

    @Test
    public void testGetDifference_intersectionSouth() {
        final BcS2Polygon s2Polygon_1 = createBcS2Polygon("POLYGON((-5 -3, -2 -3, -2 -1, -5 -1, -5 -3))");
        final BcS2Polygon s2Polygon_2 = createBcS2Polygon("POLYGON((-4 -4, -3 -4, -3.5 -2, -4 -4))");

        final Polygon difference = s2Polygon_1.getDifference(s2Polygon_2);
        assertFalse(difference.isEmpty());

        assertEquals("POLYGON((-3.5 -2.0,-3.249986353722413 -3.000997979465906,-1.9999999999999996 -3.0000000000000004,-1.9999999999999996 -1.0,-5.0 -0.9999999999999998,-4.999999999999999 -3.0000000000000004,-3.7500136462775866 -3.0009979794659065,-3.5 -2.0))",
                S2WKTWriter.write(difference.getInner()));
    }

    @Test
    public void testGetUnion_intersectingPolygons() {
        final BcS2Polygon s2Polygon_1 = createBcS2Polygon("POLYGON((2 -1, 4 -1, 4 1, 2 1, 2 -1))");
        final BcS2Polygon s2Polygon_2 = createBcS2Polygon("POLYGON((-1 -0.5, 3 -0.3, 3 0.5, -1 0.5, -1 -0.5))");

        final Polygon union = s2Polygon_1.getUnion(s2Polygon_2);
        assertFalse(union.isEmpty());

        assertEquals("POLYGON((1.9999999999999996 -0.3501761146482015,1.9999999999999996 -1.0,4.000000000000001 -1.0,4.000000000000001 1.0,1.9999999999999996 1.0,1.9999999999999996 0.500228561696982,-1.0 0.5,-1.0 -0.5,1.9999999999999996 -0.3501761146482015))",
                S2WKTWriter.write(union.getInner()));
    }

    @Test
    public void testContains_pointInside() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((0 0 , 1 0, 1 1, 0 1, 0 0))");
        final BcS2Point point = createBcS2Point("POINT(0.2 0.7)");

        assertTrue(polygon.contains(point));
    }

    @Test
    public void testContains_pointOutside() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((0 0 , 1 0, 1 1, 0 1, 0 0))");
        final BcS2Point point = createBcS2Point("POINT(1.1 0.8)");

        assertFalse(polygon.contains(point));
    }

    @Test
    public void testContains_pointOnCorner_isInside() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((0 0 , 1 0, 1 1, 0 1, 0 0))");
        final BcS2Point point = createBcS2Point("POINT(0 0)");

        assertTrue(polygon.contains(point));
    }

    @Test
    public void testContains_unsupportedGeometry() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((0 0 , 1 0, 1 1, 0 1, 0 0))");
        final BcS2Polygon polygon_2 = createBcS2Polygon("POLYGON((1 0 , 2 0, 2 1, 1 1, 1 0))");

        try {
            polygon.contains(polygon_2);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testShiftLon() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((0 0 , 1 0, 1 1, 0 1, 0 0))");

        try {
            polygon.shiftLon(-3.9);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testExtractPoints_polygon() {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((2 1, 6 1, 7 3, 4 3, 2 1))");

        final ArrayList<Point> points = BcS2Polygon.extractPoints((S2Polygon) bcS2Polygon.getInner());
        assertEquals(5, points.size());

        assertEquals(2.0, points.get(0).getLon(), 1e-8);
        assertEquals(1.0, points.get(0).getLat(), 1e-8);

        assertEquals(7.0, points.get(2).getLon(), 1e-8);
        assertEquals(3.0, points.get(2).getLat(), 1e-8);

        assertEquals(2.0, points.get(4).getLon(), 1e-8);
        assertEquals(1.0, points.get(4).getLat(), 1e-8);
    }

    @Test
    public void testExtractPoints_polygon_twoLoops() {
        final BcS2Polygon bcS2Polygon = createBcS2Polygon("POLYGON((0 0, 0 5, 5 5, 5 0, 0 0),(1 2, 4 2, 2 4, 1 2))");

        final ArrayList<Point> points = BcS2Polygon.extractPoints((S2Polygon) bcS2Polygon.getInner());
        assertEquals(9, points.size());

        assertEquals(5.0, points.get(0).getLon(), 1e-8);
        assertEquals(0.0, points.get(0).getLat(), 1e-8);

        assertEquals(0.0, points.get(2).getLon(), 1e-8);
        assertEquals(5.0, points.get(2).getLat(), 1e-8);

        assertEquals(4.0, points.get(6).getLon(), 1e-8);
        assertEquals(2.0, points.get(6).getLat(), 1e-8);
    }

    @Test
    public void testGetCentroid() {
        final BcS2Polygon polygon = createBcS2Polygon("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");

        final Point centroid = polygon.getCentroid();
        assertEquals(0.0, centroid.getLat(), 1e-8);
        assertEquals(0.0, centroid.getLon(), 1e-8);
    }

    private BcS2Point createBcS2Point(String wellKnownText) {
        final S2Point s2Point = (S2Point) s2WKTReader.read(wellKnownText);
        return new BcS2Point(new S2LatLng(s2Point));
    }

    private BcS2Polygon createBcS2Polygon(String wellKnownText) {
        S2Polygon polygon = (S2Polygon) s2WKTReader.read(wellKnownText);
        return new BcS2Polygon(polygon);
    }

    private BcS2LineString createBcS2LineString(String wellKnownText) {
        S2Polyline polyline = (S2Polyline) s2WKTReader.read(wellKnownText);
        return new BcS2LineString(polyline);
    }

    @SuppressWarnings("unchecked")
    private BcS2MultiLineString createBcS2MultiLineString(String wellKnownText) {
        List<S2Polyline> polylines = (List<S2Polyline>) s2WKTReader.read(wellKnownText);
        return new BcS2MultiLineString(polylines);
    }
}
