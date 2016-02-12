package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.geometry.s2.S2WKTReader;
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
    public void testS2MultiPolygon() {
        S2MultiPolygon s2MultiPolygon = createS2MultiPolygon("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertTrue(coordinates.length > 2);
        assertNotNull(s2MultiPolygon);
        assertEquals(coordinates[0].toString(), "POINT(29.999999999999993 20.0)");
        assertEquals(coordinates[1].toString(), "POINT(100.0 10.0)");
    }

    @Test
    public void testIntersectMultiPolygon() {
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


    @Test
    public void testPlotMultiPolygon() {
        List<Polygon> s2PolygonList = new ArrayList<>();
        s2PolygonList.add(createS2Polygon("POLYGON ((10 10, 80 10, 80 80, 10 80))"));
        s2PolygonList.add(createS2Polygon("POLYGON((-8 -10,-8 12,9 12,9 -10,-8 -10))"));
        String multiPolygon = BoundingPolygonCreator.plotMultiPolygon(s2PolygonList);
        assertEquals("MULTIPOLYGON(((9.999999999999998 10.0,80.0 10.0,80.0 80.0,10.0 80.0)),((9.0 -10.0,9.000000000000002 12.000000000000002,-7.999999999999998 12.000000000000002,-7.999999999999998 -10.0)))", multiPolygon);

    }


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
}
