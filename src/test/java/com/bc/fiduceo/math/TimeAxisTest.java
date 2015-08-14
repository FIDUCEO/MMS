package com.bc.fiduceo.math;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TimeAxisTest {

    @Test
    public void testIntersectStraightLineWithSquare() throws ParseException {
        final WKTReader wktReader = new WKTReader();
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 0,4 6)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertEquals(100000333333L, timeInterval.getStartTime().getTime());
        assertEquals(100000666666L, timeInterval.getStopTime().getTime());
    }

    @Test
    public void testIntersectStraightLineWithSquare_shifted() throws ParseException {
        final WKTReader wktReader = new WKTReader();
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-1 1,5 7)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertEquals(100000166666L, timeInterval.getStartTime().getTime());
        assertEquals(100000499999L, timeInterval.getStopTime().getTime());
    }
}
