package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author muhammad.bc
 */
public class S2MultiPolygonTest {
    @Test
    public void setUp() {
        S2WKTReader reader = new S2WKTReader();
        List<com.google.common.geometry.S2Polygon> read = (List<com.google.common.geometry.S2Polygon>) reader.read("MULTIPOLYGON(((30 20, 100 10)),((100 10, 300 10)),((30 20,100 10)))");
        S2MultiPolygon s2MultiPolygon = new S2MultiPolygon(read);
        Point[] coordinates = s2MultiPolygon.getCoordinates();
        assertTrue(coordinates.length > 2);
        assertNotNull(s2MultiPolygon);
        assertEquals(coordinates[0].toString(),"POINT(29.999999999999993 20.0)");
        assertEquals(coordinates[1].toString(),"POINT(100.0 10.0)");

    }
}
