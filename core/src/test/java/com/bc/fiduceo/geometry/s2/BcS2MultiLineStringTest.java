package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author muhammad.bc
 */
public class BcS2MultiLineStringTest {

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
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString();
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
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString();
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
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString();
        assertNotNull(multiLineString);
        assertFalse(multiLineString.isEmpty());
    }

    @Test
    public void testGetIntersection() {
        final BcS2MultiLineString multiLineString = getCreateBcS2MultiLineString();
        final BcS2Point point = new BcS2Point(new S2LatLng());

        try {
            multiLineString.getIntersection(point);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private BcS2MultiLineString getCreateBcS2MultiLineString() {
        final BcS2GeometryFactory bcS2GeometryFactory = new BcS2GeometryFactory();
        final BcS2LineString lineString_1 = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(10 10, 10 11, 11 12)");
        final BcS2LineString lineString_2 = (BcS2LineString) bcS2GeometryFactory.parse("LINESTRING(2 3, 3 5, 3.5 7)");
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
