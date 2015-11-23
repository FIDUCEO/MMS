package com.bc.geometry.s2;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class S2WKTWriterTest {

    private ArrayList<S2Point> vertices;

    @Before
    public void setUp() {
        vertices = new ArrayList<>();
    }

    @Test
    public void testWriteLineString_empty() {
        try {
            S2WKTWriter.write(new S2Polyline(vertices));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWriteLineString_oneVertex() {
        vertices.add(new S2Point());

        try {
            S2WKTWriter.write(new S2Polyline(vertices));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testWriteLineString_twoVertices() {
        vertices.add(createS2Point(-8, 10));
        vertices.add(createS2Point(-7.23, 10.8));

       final String wkt= S2WKTWriter.write(new S2Polyline(vertices));
        assertEquals("LINESTRING(-7.999999999999998 10.0,-7.229999999999999 10.799999999999999)", wkt);
    }

    @Test
    public void testWriteLineString_threeVertices() {
        vertices.add(createS2Point(2, -39));
        vertices.add(createS2Point(1, -39.5));
        vertices.add(createS2Point(0, -40));

        final String wkt= S2WKTWriter.write(new S2Polyline(vertices));
        assertEquals("LINESTRING(1.9999999999999996 -39.0,1.0 -39.5,0.0 -40.0)", wkt);
    }

    private static S2Point createS2Point(double lon, double lat) {
        return S2LatLng.fromDegrees(lat, lon).toPoint();
    }
}
