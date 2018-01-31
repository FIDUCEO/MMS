package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.geometry.s2.S2WKTReader;
import com.bc.geometry.s2.S2WKTWriter;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BcS2MultiPolygonTest {

    private S2WKTReader s2WKTReader;

    @Before
    public void setUp() {
        s2WKTReader = new S2WKTReader();
    }

    @Test
    public void testGetIntersection_point_outside() {
        final BcS2Point bcS2Point = createS2Point("POINT(-18.3 25.4)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 30 0, 30 20, 20 10)),((20 70, 50 70, 50 80, 20 80)))");


        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Point);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_point_insideFirst() {
        final BcS2Point bcS2Point = createS2Point("POINT(25 10)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 30 0, 30 20, 20 10)),((20 70, 50 70, 50 80, 20 80)))");


        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Point);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        assertEquals("POINT(25.0 10.0)", intersection.toString());
    }

    @Test
    public void testGetIntersection_point_insideSecond() {
        final BcS2Point bcS2Point = createS2Point("POINT(30 75)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 30 0, 30 20, 20 10)),((20 70, 50 70, 50 80, 20 80)))");


        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Point);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        assertEquals("POINT(29.999999999999986 75.0)", intersection.toString());
    }

    @Test
    public void testGetIntersection_polygon_intersectAllPolygons() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((30 30, 45 30, 45 75, 30 75, 30 30))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50, 20 0)),((20 70, 50 70, 50 80, 20 80, 20 70)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        assertTrue(intersection instanceof BcS2MultiPolygon);

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(9, coordinates.length);
        assertEquals(30.0, coordinates[0].getLon(), 1e-8);
        assertEquals(30.0, coordinates[0].getLat(), 1e-8);

        assertEquals(30.0, coordinates[2].getLon(), 1e-8);
        assertEquals(43.277555540062295, coordinates[2].getLat(), 1e-8);

        assertEquals(45.0, coordinates[6].getLon(), 1e-8);
        assertEquals(75.0, coordinates[6].getLat(), 1e-8);
    }

    @Test
    public void testGetIntersection_polygon_noIntersection() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((5 0, 15 0, 15 5, 5 5, 5 0))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Polygon);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_polygon_intersectOnePolygon() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((5 15, 15 15, 15 20, 5 20, 5 15))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        assertTrue(intersection instanceof BcS2Polygon);
        final BcS2Polygon result = (BcS2Polygon) intersection;
        assertEquals("POLYGON((9.999999999999996 15.054701128833466,14.999999999999996 14.999999999999996,14.999999999999998 20.0,9.999999999999998 20.07030897931526,9.999999999999996 15.054701128833466))", S2WKTWriter.write(result.getInner()));
    }

    @Test
    public void testGetIntersection_multiPolygon_noIntersection() {
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");
        final BcS2MultiPolygon other = createS2MultiPolygon("MULTIPOLYGON (((-10 10, -20 10, -20 30, -10 30, -10 10)),((-30 10, -40 10, -40 20, -30 20, -30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testGetIntersection_multiPolygon_oneIntersection() {
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");
        final BcS2MultiPolygon other = createS2MultiPolygon("MULTIPOLYGON (((-10 10, -20 10, -20 30, -10 30, -10 10)),((25 5, 45 5, 45 15, 25 15, 25 5)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());

        final Polygon polygon = (Polygon) intersection;
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(15.16549350298379, 40.0)\n" +
                "(15.165493502983786, 29.999999999999993)\n" +
                "(10.0, 29.999999999999993)\n" +
                "(10.0, 40.00000000000001)\n" +
                ">\n", polygon.toString());
    }

    @Test
    public void testGetIntersection_multiPolygon_twoIntersections() {
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");
        final BcS2MultiPolygon other = createS2MultiPolygon("MULTIPOLYGON (((-10 10, -20 10, -20 30, -10 30, -10 10)),((5 5, 45 5, 45 15, 5 15, 5 5)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());

        final BcS2MultiPolygon multiPolygon = (BcS2MultiPolygon) intersection;
        final Point[] coordinates = multiPolygon.getCoordinates();
        assertEquals(10, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(10.0, coordinates[0].getLat(), 1e-8);
        assertEquals(40.00000000000001, coordinates[5].getLon(), 1e-8);
        assertEquals(15.399125759717224, coordinates[5].getLat(), 1e-8);
        assertEquals(40.00000000000001, coordinates[9].getLon(), 1e-8);
        assertEquals(15.399125759717224, coordinates[9].getLat(), 1e-8);
    }

    @Test
    public void testGetIntersection_multiPolygon_fourIntersections() {
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");
        final BcS2MultiPolygon other = createS2MultiPolygon("MULTIPOLYGON (((5 18, 45 18, 45 26, 5 26, 5 18)),((5 5, 45 5, 45 15, 5 15, 5 5)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());

        final BcS2MultiPolygon multiPolygon = (BcS2MultiPolygon) intersection;
        final Point[] coordinates = multiPolygon.getCoordinates();
        assertEquals(20, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(27.341635611747215, coordinates[0].getLat(), 1e-8);
        assertEquals(29.999999999999993, coordinates[16].getLon(), 1e-8);
        assertEquals(15.85775457400195, coordinates[16].getLat(), 1e-8);
        assertEquals(40.00000000000001, coordinates[19].getLon(), 1e-8);
        assertEquals(15.399125759717224, coordinates[19].getLat(), 1e-8);
    }

    @Test
    public void testGetIntersection_lineString_noIntersection() {
        final BcS2LineString lineString = createS2LineString("LINESTRING(-23 -19, -24 -19.8)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(lineString);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_lineString_intersectSecond() {
        final BcS2LineString lineString = createS2LineString("LINESTRING(25 10, 40 25)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(lineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        final BcS2MultiLineString result = (BcS2MultiLineString) intersection;
        final Point[] coordinates = result.getCoordinates();
        assertEquals(2, coordinates.length);
        assertEquals("POINT(29.999999999999993 15.39374601204165)", coordinates[0].toString());
        assertEquals("POINT(34.640527811426715 20.069945497961907)", coordinates[1].toString());
    }

    @Test
    public void testGetIntersectio_lineString_intersectBoth() {
        final BcS2LineString lineString = createS2LineString("LINESTRING(0 25, 50 15)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(lineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        final BcS2MultiLineString result = (BcS2MultiLineString) intersection;
        final Point[] coordinates = result.getCoordinates();
        assertEquals(4, coordinates.length);
        assertEquals("POINT(9.999999999999996 24.323812139767362)", coordinates[0].toString());
        assertEquals("POINT(20.0 22.97662792253651)", coordinates[1].toString());
        assertEquals("POINT(33.634710262860956 20.065065789466985)", coordinates[2].toString());
        assertEquals("POINT(39.99999999999999 18.29075744982923)", coordinates[3].toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetIntersection_multiLineString_intersectAllPolygons() {
        final BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2MultiLineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());

        final List<S2Polyline> intersectionInner = (List<S2Polyline>) intersection.getInner();

        assertEquals(intersectionInner.size(), 2);
        assertEquals(intersectionInner.get(0).vertex(0).getX(), 0.883022221559489, 1e-8);
        assertEquals(intersectionInner.get(0).vertex(0).getY(), 0.32139380484326957, 1e-8);

        assertEquals(intersectionInner.get(1).numVertices(), 4);
        assertEquals(intersectionInner.get(1).vertex(3).getX(), 0.8528685319524433, 1e-8);
        assertEquals(intersectionInner.get(1).vertex(3).getY(), 0.49240387650610395, 1e-8);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetIntersection_multiLineString_intersectOnePolygon() {
        final BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((10 40, 40 50),(10 20, 40 40))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2MultiLineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());

        final List<S2Polyline> intersectionInner = (List<S2Polyline>) intersection.getInner();

        assertEquals(intersectionInner.size(), 2);
        assertEquals(0.6685052611131922, intersectionInner.get(0).vertex(0).getX(), 1e-8);
        assertEquals(0.24331601649555723, intersectionInner.get(0).vertex(0).getY(), 1e-8);

        assertEquals(intersectionInner.get(1).numVertices(), 2);
        assertEquals(0.638614550493825, intersectionInner.get(1).vertex(1).getX(), 1e-8);
        assertEquals(0.4602503227130726, intersectionInner.get(1).vertex(1).getY(), 1e-8);
    }

    @Test
    public void testGetIntersection_multiLineString_noIntersection() {
        final BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((-10 40, -8 50),(-10 20, -11 40))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2MultiLineString);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetCoordinates() {
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50, 20 0)),((20 70, 50 70, 50 90, 20 90, 20 70)))");

        final Point[] coordinates = bcS2MultiPolygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(10, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(20.0, coordinates[3].getLon(), 1e-8);
        assertEquals(49.99999999999999, coordinates[3].getLat(), 1e-8);

        assertEquals(50.0, coordinates[7].getLon(), 1e-8);
        assertEquals(90.0, coordinates[7].getLat(), 1e-8);
    }

    @Test
    public void testIsEmpty_noPolygons() {
        final BcS2MultiPolygon emptyMultiPolygon = new BcS2MultiPolygon(new ArrayList<>());
        assertTrue(emptyMultiPolygon.isEmpty());
    }

    @Test
    public void testIsEmpty_containsEmptyPolygons() {
        final ArrayList<Polygon> polygonList = createListWithEmptyPolygon();
        final BcS2MultiPolygon emptyMultiPolygon = new BcS2MultiPolygon(polygonList);

        assertTrue(polygonList.get(0).isEmpty());
        assertTrue(emptyMultiPolygon.isEmpty());
    }

    @Test
    public void testIsEmpty_notEmpty_mixedPolygons() {
        final List<Polygon> polygonList = createListWithEmptyPolygon();
        final BcS2Polygon emptyPolygon = new BcS2Polygon(polygonList.get(0).getInner());
        final S2Polygon s2Polygon = (S2Polygon) s2WKTReader.read("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        polygonList.add(new BcS2Polygon(s2Polygon));

        final BcS2MultiPolygon multiPolygon = new BcS2MultiPolygon(polygonList);

        assertTrue(emptyPolygon.isEmpty());
        assertFalse(multiPolygon.isEmpty());
    }

    @Test
    public void testIsValid_noPolygons() {
        final BcS2MultiPolygon emptyMultiPolygon = new BcS2MultiPolygon(new ArrayList<>());
        assertFalse(emptyMultiPolygon.isValid());
    }

    @Test
    public void testIsValid_onePolygon_valid() {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.isValid()).thenReturn(true);

        final ArrayList<Polygon> polygonList = new ArrayList<>();
        polygonList.add(polygon);
        final BcS2MultiPolygon multiPolygon = new BcS2MultiPolygon(polygonList);

        assertTrue(multiPolygon.isValid());
    }

    @Test
    public void testIsValid_onePolygon_invalid() {
        final Polygon polygon = mock(Polygon.class);
        when(polygon.isValid()).thenReturn(false);

        final ArrayList<Polygon> polygonList = new ArrayList<>();
        polygonList.add(polygon);
        final BcS2MultiPolygon multiPolygon = new BcS2MultiPolygon(polygonList);

        assertFalse(multiPolygon.isValid());
    }

    @Test
    public void testIsValid_twoPolygons_allValid() {
        final Polygon polygon_1 = mock(Polygon.class);
        when(polygon_1.isValid()).thenReturn(true);

        final Polygon polygon_2 = mock(Polygon.class);
        when(polygon_2.isValid()).thenReturn(true);

        final ArrayList<Polygon> polygonList = new ArrayList<>();
        polygonList.add(polygon_1);
        polygonList.add(polygon_2);
        final BcS2MultiPolygon multiPolygon = new BcS2MultiPolygon(polygonList);

        assertTrue(multiPolygon.isValid());
    }

    @Test
    public void testIsValid_twoPolygons_oneInvalid() {
        final Polygon polygon_1 = mock(Polygon.class);
        when(polygon_1.isValid()).thenReturn(false);

        final Polygon polygon_2 = mock(Polygon.class);
        when(polygon_2.isValid()).thenReturn(true);

        final ArrayList<Polygon> polygonList = new ArrayList<>();
        polygonList.add(polygon_1);
        polygonList.add(polygon_2);
        final BcS2MultiPolygon multiPolygon = new BcS2MultiPolygon(polygonList);

        assertFalse(multiPolygon.isValid());
    }

    @Test
    public void testGetInner() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Object inner = multiPolygon.getInner();
        assertNotNull(inner);
        assertTrue(inner instanceof List);
        final List innerList = (List) inner;
        assertTrue(innerList.get(0) instanceof S2Polygon);
    }

    @Test
    public void testGetPolygons() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((21 0, 51 0, 51 20, 21 50)),((21 70, 51 70, 51 90, 21 90)))");

        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(0.0, 21.0)\n" +
                "(0.0, 50.99999999999999)\n" +
                "(20.0, 51.0)\n" +
                "(49.99999999999999, 21.0)\n" +
                ">\n", polygons.get(0).toString());
    }

    @Test
    public void testShiftLon() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 0, 50 0, 50 20, 10 50)),((10 70, 50 70, 50 90, 10 90)))");

        try {
            multiPolygon.shiftLon(22.5);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testContains() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 0, 60 0, 50 20, 10 50)),((10 70, 60 70, 50 90, 10 90)))");
        final BcS2Point bcS2Point = createS2Point("POINT(14 25.4)");

        try {
            multiPolygon.contains(bcS2Point);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetDifference() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 0, 60 0, 50 20, 10 50)),((10 70, 60 70, 50 90, 10 90)))");
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((30 20, 45 30, 45 75, 30 75, 30 20))");

        try {
            multiPolygon.getDifference(bcS2Polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetUnion() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 0, 60 0, 60 20, 10 50)),((10 70, 60 70, 60 90, 10 90)))");
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((30 20, 55 30, 55 75, 30 75, 30 20))");

        try {
            multiPolygon.getUnion(bcS2Polygon);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetCentroid() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 0, 60 0, 50 30, 10 50)),((10 70, 60 70, 50 86, 10 90)))");

        try {
            multiPolygon.getCentroid();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @SuppressWarnings("unchecked")
    private BcS2MultiPolygon createS2MultiPolygon(String wellKnownText) {
        List<S2Polygon> googlePolygonList = (List<S2Polygon>) s2WKTReader.read(wellKnownText);
        List<Polygon> polygonList = new ArrayList<>();
        for (S2Polygon googlePolygon : googlePolygonList) {
            polygonList.add(new BcS2Polygon(googlePolygon));
        }
        return new BcS2MultiPolygon(polygonList);
    }

    private BcS2Polygon createS2Polygon(String wellKnownText) {
        S2Polygon polygon = (S2Polygon) s2WKTReader.read(wellKnownText);
        return new BcS2Polygon(polygon);
    }

    private BcS2LineString createS2LineString(String wellKnownText) {
        S2Polyline lineString = (S2Polyline) s2WKTReader.read(wellKnownText);
        return new BcS2LineString(lineString);
    }

    @SuppressWarnings("unchecked")
    private BcS2MultiLineString createS2Polyline(String wkt) {
        List<S2Polyline> read = (List<S2Polyline>) s2WKTReader.read(wkt);
        return new BcS2MultiLineString(read);
    }

    private BcS2Point createS2Point(String wellKnownText) {
        final S2Point point = (S2Point) s2WKTReader.read(wellKnownText);
        return new BcS2Point(new S2LatLng(point));
    }

    private ArrayList<Polygon> createListWithEmptyPolygon() {
        final ArrayList<Polygon> polygonList = new ArrayList<>();
        final S2Polygon googlePolygon = new S2Polygon();
        polygonList.add(new BcS2Polygon(googlePolygon));
        return polygonList;
    }
}
