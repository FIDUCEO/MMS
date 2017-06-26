package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
public class BcS2MultiLineStringTest {

    private BcS2GeometryFactory bcS2GeometryFactory;

    @Before
    public void setUp() throws Exception {
        bcS2GeometryFactory = new BcS2GeometryFactory();
    }

    @Test
    public void testGetCoordinateS2MultiLineString() {
        BcS2MultiLineString bcS2MultiLineString = createS2Polyline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        Point[] coordinates = bcS2MultiLineString.getCoordinates();
        assertEquals(coordinates.length, 7);
        assertEquals(coordinates[0].getLon(), 9.999999999999998, 1e-8);
        assertEquals(coordinates[0].getLat(), 18.0, 1e-8);

        assertEquals(coordinates[6].getLon(), 29.999999999999993, 1e-8);
        assertEquals(coordinates[6].getLat(), 10.0, 1e-8);
    }

    @Test
    public void testCreateFrom_listOfLineStrings() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        assertNotNull(multiLineString);
        final Point[] coordinates = multiLineString.getCoordinates();
        assertEquals(6, coordinates.length);

        assertEquals(10.0, coordinates[0].getLon(), 1e-8);
        assertEquals(10.0, coordinates[0].getLat(), 1e-8);

        assertEquals(3.0, coordinates[4].getLon(), 1e-8);
        assertEquals(5.0, coordinates[4].getLat(), 1e-8);
    }

    @Test
    public void testIsValid() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        assertNotNull(multiLineString);
        assertTrue(multiLineString.isValid());
    }

    @Test
    public void testIsEmpty_NoPoint() {
        S2Point[] s2PointList = {new S2Point()};
        S2Polyline s2Polyline = new S2Polyline(Arrays.asList(s2PointList));

        final BcS2LineString lineString_1 = new BcS2LineString(s2Polyline);
        final List<BcS2LineString> lineStringList = new ArrayList<>();
        lineStringList.add(lineString_1);

        final BcS2MultiLineString multiLineString = BcS2MultiLineString.createFrom(lineStringList);
        assertNotNull(multiLineString);
        assertTrue(multiLineString.isEmpty());
    }

    @Test
    public void testIsEmpty_ZeroPointIntervals() {
        S2Point[] s2PointList = {new S2Point(), new S2Point(30.0, 20.0, 1.0), new S2Point(), new S2Point(3.0, 7.0, 6.0)};
        S2Polyline s2Polyline = new S2Polyline(Arrays.asList(s2PointList));

        final BcS2LineString lineString_1 = new BcS2LineString(s2Polyline);
        final List<BcS2LineString> lineStringList = new ArrayList<>();
        lineStringList.add(lineString_1);

        final BcS2MultiLineString multiLineString = BcS2MultiLineString.createFrom(lineStringList);
        assertNotNull(multiLineString);
        assertFalse(multiLineString.isEmpty());
    }

    @Test
    public void testIsEmpty_Null_Polyline() {
        final BcS2LineString lineString_1 = new BcS2LineString(null);
        final List<BcS2LineString> lineStringList = new ArrayList<>();
        lineStringList.add(lineString_1);

        final BcS2MultiLineString multiLineString = BcS2MultiLineString.createFrom(lineStringList);
        assertNotNull(multiLineString);
        assertTrue(multiLineString.isEmpty());
    }

    @Test
    public void testIsNotEmpty() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        assertNotNull(multiLineString);
        assertFalse(multiLineString.isEmpty());
    }

    @Test
    public void testGetIntersection_point_noIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        final BcS2Point point = (BcS2Point) bcS2GeometryFactory.parse("POINT(0 0)");

        final Geometry intersection = multiLineString.getIntersection(point);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_point_intersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        final BcS2Point point = (BcS2Point) bcS2GeometryFactory.parse("POINT(10 10.5)");

        final Geometry intersection = multiLineString.getIntersection(point);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        assertEquals("POINT(9.999999999999998 10.5)", intersection.toString());
    }

    @Test
    public void testGetIntersection_lineString_noIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        final BcS2LineString lineString = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(0 -3, 1 -4, 2 -4)");

        final Geometry intersection = multiLineString.getIntersection(lineString);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_lineString_oneIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        final BcS2LineString lineString = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(8 14, 12 10)");

        final Geometry intersection = multiLineString.getIntersection(lineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        assertEquals("POINT(10.509490992914476 11.51080486077236)", intersection.toString());
    }

    @Test
    public void testGetIntersection_lineString_twoIntersections() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(10 10, 10 11, 11 12)",
                "LINESTRING(2 3, 3 5, 3.5 7)");
        final BcS2LineString lineString = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(8 14, 12 10, 6 4, 0 6)");

        final Geometry intersection = multiLineString.getIntersection(lineString);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        final GeometryCollection geometryCollection = (GeometryCollection) intersection;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(2, geometries.length);
        assertEquals("POINT(10.509490992914476 11.51080486077236)", geometries[0].toString());
        assertEquals("POINT(3.001921948317671 5.007715218614045)", geometries[1].toString());
    }

    @Test
    public void testGetIntersection_multiLineString_noIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(5 3, 6 3, 7 3)",
                "LINESTRING(-2 1, -1 1, 0 1)");
        final BcS2MultiLineString other = getCreateBcS2MultiLineString("LINESTRING(5 4, 6 4, 7 4)",
                "LINESTRING(-2 0, -1 0, 0 0)");

        final Geometry intersection = multiLineString.getIntersection(other);
        assertNotNull(intersection);
        assertFalse(intersection.isValid());
    }

    @Test
    public void testGetIntersection_multiLineString_oneIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(5 3, 6 3, 7 3)",
                "LINESTRING(-2 1, -1 1, 0 1)");
        final BcS2MultiLineString other = getCreateBcS2MultiLineString("LINESTRING(5 4, 6 4, 7 4)",
                "LINESTRING(-2 0, -1 2, 0 3)");

        final Geometry intersection = multiLineString.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        assertEquals("POINT(-1.5001523435130504 1.0000380706493395)", intersection.toString());
    }

    @Test
    public void testGetIntersection_multiLineString_twoIntersections() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(5 3, 6 3, 7 3)",
                "LINESTRING(-2 1, -1 1, 0 1)");
        final BcS2MultiLineString other = getCreateBcS2MultiLineString("LINESTRING(5 4, 6 4, 7 4)",
                "LINESTRING(-2 0, -1 1.3, 7 3.2)");

        final Geometry intersection = multiLineString.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        final GeometryCollection geometryCollection = (GeometryCollection) intersection;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(2, geometries.length);
        assertEquals("POINT(6.147268054814197 3.0000572776790584)", geometries[0].toString());
        assertEquals("POINT(-1.2308182803291359 1.0000270363882184)", geometries[1].toString());
    }

    @Test
    public void testGetIntersection_multiLineString_fourIntersections() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString("LINESTRING(5 3, 6 3, 7 3)",
                "LINESTRING(5 1, 6 1, 7 1)");
        final BcS2MultiLineString other = getCreateBcS2MultiLineString("LINESTRING(5.5 4, 5.5 0)",
                "LINESTRING(6.5 4, 6.5 0)");

        final Geometry intersection = multiLineString.getIntersection(other);
        assertNotNull(intersection);
        assertTrue(intersection.isValid());
        final GeometryCollection geometryCollection = (GeometryCollection) intersection;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(4, geometries.length);
        assertEquals("POINT(5.5 3.000114026471656)", geometries[0].toString());
        assertEquals("POINT(5.499999999999999 1.0000380706528735)", geometries[1].toString());
        assertEquals("POINT(6.5 3.000114026471657)", geometries[2].toString());
        assertEquals("POINT(6.5 1.0000380706528726)", geometries[3].toString());
    }

    private BcS2MultiLineString getCreateBcS2MultiLineString(String firstWkt, String secondWkt) {
        final BcS2LineString lineString_1 = (BcS2LineString) bcS2GeometryFactory.parse(firstWkt);
        final BcS2LineString lineString_2 = (BcS2LineString) bcS2GeometryFactory.parse(secondWkt);
        final List<BcS2LineString> lineStringList = new ArrayList<>();
        lineStringList.add(lineString_1);
        lineStringList.add(lineString_2);

        return BcS2MultiLineString.createFrom(lineStringList);
    }

    private BcS2MultiLineString createS2Polyline(String wkt) {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polyline> read = (List<S2Polyline>) reader.read(wkt);
        return new BcS2MultiLineString(read);
    }
}
