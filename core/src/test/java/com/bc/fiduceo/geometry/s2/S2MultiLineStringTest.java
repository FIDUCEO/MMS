package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Polyline;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author muhammad.bc
 */
public class S2MultiLineStringTest {

    private S2MultiLineString createS2Polylline(String wkt) {
        S2WKTReader reader = new S2WKTReader();
        List<S2Polyline> read = (List<S2Polyline>) reader.read(wkt);
        return new S2MultiLineString(read);
    }


    @Test
    public void testGetCoordinateS2MultiLineString() {
        S2MultiLineString s2MultiLineString = createS2Polylline("MULTILINESTRING((10 18, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
        Point[] coordinates = s2MultiLineString.getCoordinates();
        assertEquals(coordinates.length, 7);
        assertEquals(coordinates[0].getLon(), 9.999999999999998, 1e-8);
        assertEquals(coordinates[0].getLat(), 18.0, 1e-8);

        assertEquals(coordinates[6].getLon(), 29.999999999999993, 1e-8);
        assertEquals(coordinates[6].getLat(), 10.0, 1e-8);
    }
}
