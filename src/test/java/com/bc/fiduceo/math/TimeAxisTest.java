package com.bc.fiduceo.math;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TimeAxisTest {

    private WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
    }

    @Test
    public void testIntersectStraightLineWithSquare() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 0,4 6)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000333333L, 100000666666L, timeInterval);
    }

    @Test
    public void testIntersectStraightLineWithSquare_shifted() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-1 1,5 7)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000166666L, 100000499999L, timeInterval);
    }

    @Test
    public void testIntersectStraightLineWithRectangle_lineStart_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(3 1,6 -2)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100000333333L, timeInterval);
    }

    @Test
    public void testIntersectStraightLineWithRectangle_lineEnd_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 5,1 1)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000750000L, 100001000000L, timeInterval);
    }

    @Test
    public void testIntersectStraightLineWithRectangle_line_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 1,4 1)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100001000000L, timeInterval);
    }

    @Test
    public void testIntersectSegmentedLineWithParallelogram() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((2 -2, 7 -2, 9 -5, 4 -5, 2 -2))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 -6, 2 -4, 4 -3, 6 -3,8 -2)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.intersect(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000385165L, 100000903707L, timeInterval);
    }

    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }
}
