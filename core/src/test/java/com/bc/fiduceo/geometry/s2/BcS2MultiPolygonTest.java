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

/**
 * @author muhammad.bc
 */
public class BcS2MultiPolygonTest {

    private S2WKTReader s2WKTReader;

    @Before
    public void setUp() {
        s2WKTReader = new S2WKTReader();
    }

    @Test
    public void testGetIntersection_invalidInputGeometry() {
        final BcS2Point bcS2Point = createS2Point("POINT(-18.3 25.4)");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 30 0, 30 20, 20 10)),((20 70, 50 70, 50 80, 20 80)))");

        try {
            bcS2MultiPolygon.getIntersection(bcS2Point);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
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
        assertEquals(7, coordinates.length);
        assertEquals(30.0, coordinates[0].getLon(), 1e-8);
        assertEquals(30.0, coordinates[0].getLat(), 1e-8);

        assertEquals(30.0, coordinates[2].getLon(), 1e-8);
        assertEquals(43.277555540062295, coordinates[2].getLat(), 1e-8);

        assertEquals(30.0, coordinates[6].getLon(), 1e-8);
        assertEquals(75.0, coordinates[6].getLat(), 1e-8);
    }

    @Test
    public void testIntersectMultiPolygon_polygon_noIntersection() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((5 0, 15 0, 15 5, 5 5, 5 0))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Polygon);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersectMultiPolygon_polygon_intersectOnePolygon() {
        final BcS2Polygon bcS2Polygon = createS2Polygon("POLYGON ((5 15, 15 15, 15 20, 5 20, 5 15))");
        final BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((10 10, 20 10, 20 30, 10 30, 10 10)),((30 10, 40 10, 40 20, 30 20, 30 10)))");

        final Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        assertTrue(intersection instanceof BcS2Polygon);
        final BcS2Polygon result = (BcS2Polygon) intersection;
        assertEquals("POLYGON((9.999999999999996 15.054701128833466,14.999999999999996 14.999999999999996,14.999999999999998 20.0,9.999999999999998 20.07030897931526,9.999999999999996 15.054701128833466))", S2WKTWriter.write(result.getInner()));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testIntersectMultiPolygon_LineString_intersectAllPolygons() {
        BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Geometry intersection = bcS2MultiPolygon.getIntersection(bcS2MultiLineString);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        List<S2Polyline> intersectionInner = (List<S2Polyline>) intersection.getInner();

        assertEquals(intersectionInner.size(), 2);
        assertEquals(intersectionInner.get(0).vertex(0).getX(), 0.883022221559489, 1e-8);
        assertEquals(intersectionInner.get(0).vertex(0).getY(), 0.32139380484326957, 1e-8);

        assertEquals(intersectionInner.get(1).numVertices(), 4);
        assertEquals(intersectionInner.get(1).vertex(3).getX(), 0.8528685319524433, 1e-8);
        assertEquals(intersectionInner.get(1).vertex(3).getY(), 0.49240387650610395, 1e-8);
    }

    @Test
    public void testGetCoordinates() {
        BcS2MultiPolygon bcS2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Point[] coordinates = bcS2MultiPolygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(8, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(20.0, coordinates[3].getLon(), 1e-8);
        assertEquals(49.99999999999999, coordinates[3].getLat(), 1e-8);

        assertEquals(20.0, coordinates[7].getLon(), 1e-8);
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
    public void testGetInner() {
        final BcS2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Object inner = multiPolygon.getInner();
        assertNotNull(inner);
        assertTrue(inner instanceof List);
        assertEquals(2, ((List) inner).size());
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
        S2Polygon polygon_1 = (S2Polygon) s2WKTReader.read(wellKnownText);
        return new BcS2Polygon(polygon_1);
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
