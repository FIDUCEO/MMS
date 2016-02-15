package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
public class S2MultiPolygonTest {

    @Test
    public void testIntersectMultiPolygon_polygon_intersectAllPolygons() {
        S2Polygon s2Polygon = createS2Polygon("POLYGON ((10 10, 80 10, 80 80, 10 80))");
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Geometry intersection = s2MultiPolygon.intersection(s2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        Point[] coordinates = intersection.getCoordinates();
        assertEquals(15, coordinates.length);
        assertEquals(19.999999999999993, coordinates[0].getLon(), 1e-8);
        assertEquals(11.039051540001541, coordinates[0].getLat(), 1e-8);

        assertEquals(49.99999999999999, coordinates[2].getLon(), 1e-8);
        assertEquals(0.0, coordinates[2].getLat(), 1e-8);
        assertEquals(49.99999999999999, coordinates[14].getLon(), 1e-8);
        assertEquals(81.75015492348156, coordinates[14].getLat(), 1e-8);
    }

    @Test
    public void testIntersectMultiPolygon_polygon_noIntersection() {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON ((100 -20, 105 -20, 105 -18, 105 -20))");
        final S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Geometry intersection = s2MultiPolygon.intersection(s2Polygon);
        assertNotNull(intersection);
        assertTrue(intersection.isEmpty());
    }

    @Test
    public void testIntersectMultiPolygon_polygon_intersectOnePolygon() {
        final S2Polygon s2Polygon = createS2Polygon("POLYGON ((30 60, 30 75, 40 75, 40 60))");
        final S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 75)))");

        final Geometry intersection = s2MultiPolygon.intersection(s2Polygon);
        assertNotNull(intersection);
        assertFalse(intersection.isEmpty());

        assertTrue(intersection instanceof S2Polygon);
        final S2Polygon result = (S2Polygon) intersection;
        assertEquals("Polygon: (1) loops:\n" +
                             "loop <\n" +
                             "(70.5614933349686, 29.999999999999996)\n" +
                             "(59.99999999999999, 29.999999999999993)\n" +
                             "(59.99999999999999, 39.99999999999999)\n" +
                             "(70.56149333496859, 40.00000000000001)\n" +
                             "(70.0, 49.99999999999999)\n" +
                             "(90.0, 49.99999999999999)\n" +
                             "(75.0, 20.0)\n" +
                             "(70.0, 20.0)\n" +
                             ">\n", result.toString());

        // @todo 1 tb/tb there was a switch here in the result, changing suddenly the longitude of the result point on the north pole
        // check if this is anumerical issue or we have something broken here 2016-02-12
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testIntersectMultiPolygon_LineString_intersectAllPolygons() {
        S2MultiLineString s2MultiLineString = createS2Polylline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Geometry intersection = s2MultiPolygon.intersection(s2MultiLineString);
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
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        final Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(8, coordinates.length);
        assertEquals(20.0, coordinates[0].getLon(), 1e-8);
        assertEquals(-0.0, coordinates[0].getLat(), 1e-8);

        assertEquals(20.0, coordinates[3].getLon(), 1e-8);
        assertEquals(49.99999999999999, coordinates[3].getLat(), 1e-8);

        assertEquals(20.0, coordinates[7].getLon(), 1e-8);
        assertEquals(90.0, coordinates[7].getLat(), 1e-8);
    }

    // @todo 1 tb/tb add test for isEmpty() - check for empty list and for polygons contains in the list 2016-02-12

    @Test
    public void testPlotMultiPolygon() {
        List<Polygon> s2PolygonList = new ArrayList<>();
        s2PolygonList.add(createS2Polygon("POLYGON ((10 10, 80 10, 80 80, 10 80))"));
        s2PolygonList.add(createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))"));
        String multiPolygon = BoundingPolygonCreator.plotMultiPolygon(s2PolygonList);
        assertEquals("MULTIPOLYGON(((9.999999999999998 10.0,80.0 10.0,80.0 80.0,10.0 80.0)),((9.0 -10.0,9.000000000000002 12.000000000000002,-7.999999999999998 12.000000000000002,-7.999999999999998 -10.0)))", multiPolygon);

    }

    @Test
    public void testGetInner() {
        final S2MultiPolygon multiPolygon = createS2MultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90, 20 90)))");

        Object inner = multiPolygon.getInner();
        assertNotNull(inner);
        assertTrue(inner instanceof List);
        assertEquals(2, ((List) inner).size());
    }

    @SuppressWarnings("unchecked")
    private S2MultiPolygon createS2MultiPolygon(String wellKnownText) {
        S2WKTReader s2WKTReader = new S2WKTReader();
        List<com.google.common.geometry.S2Polygon> read = (List<com.google.common.geometry.S2Polygon>) s2WKTReader.read(wellKnownText);
        return new S2MultiPolygon(read);
    }

    private S2Polygon createS2Polygon(String wellKnownText) {
        S2WKTReader s2WKTReader = new S2WKTReader();
        com.google.common.geometry.S2Polygon polygon_1 = (com.google.common.geometry.S2Polygon) s2WKTReader.read(wellKnownText);
        return new S2Polygon(polygon_1);
    }

    @SuppressWarnings("unchecked")
    private S2MultiLineString createS2Polylline(String wkt) {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polyline> read = (List<S2Polyline>) reader.read(wkt);
        return new S2MultiLineString(read);
    }

}
