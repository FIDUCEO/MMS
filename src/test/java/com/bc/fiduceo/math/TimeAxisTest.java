package com.bc.fiduceo.math;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TimeAxisTest {

    private WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
    }

    @Test
    public void testGetIntersectionTime_noIntersection() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(0 -2,4 -2)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNull(timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 0,4 6)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000333333L, 100000666666L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare_shifted() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-1 1,5 7)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000166666L, 100000499999L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineStart_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(3 1,6 -2)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100000333333L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineEnd_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 5,1 1)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000750000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_line_inside() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 1,4 1)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_SegmentedLineWithParallelogram() throws ParseException {
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((2 -2, 7 -2, 9 -5, 4 -5, 2 -2))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 -6, 2 -4, 4 -3, 6 -3,8 -2)");

        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000385165L, 100000903707L, timeInterval);
    }

    // @todo 3 tb/tb add more tests with more complex geometries, import real satellite data boundaries and check! 2015-08-14

    @Test
    public void testGetTime_PointOnLine() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(0 0, 4 0)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) wktReader.read("POINT(2 0)");
        final Date time = timeAxis.getTime(point.getCoordinate());
        assertEquals(1000000500000L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments_PointOnLine() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) wktReader.read("POINT(-2 3)");
        final Date time = timeAxis.getTime(point.getCoordinate());
        assertEquals(1000000500000L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(2 5, 1 3, -1 1, -3 0)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) wktReader.read("POINT(3 2)");
        final Date time = timeAxis.getTime(point.getCoordinate());
        assertEquals(1000000306287L, time.getTime());
    }

    @Test
    public void testGetTime_twoSegments_noProjection() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) wktReader.read("POINT(-7 2)");
        final Date time = timeAxis.getTime(point.getCoordinate());
        assertNull(time);
    }

    @Test
    public void testGetIntersectionTime_line() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, 0 3, -3 4, -5 4)");
        final LineString polygonSide = (LineString) wktReader.read("LINESTRING(-2 0, -4 2)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000107520L, 1000000847943L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_line_inverseDirection() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, 0 3, -3 4, -5 4)");
        final LineString polygonSide = (LineString) wktReader.read("LINESTRING(-4 2, -2 0)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000107520L, 1000000847943L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_lineWithMorePoints() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 -2, 0 -3, 2 -4, 4 -6, 6 -8, 7 -10, 8 -12)");
        final LineString polygonSide = (LineString) wktReader.read("LINESTRING(-2 -7, 0 -8, 2 -10, 3 -12)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000153143L, 1000000846856L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_lineWithMorePoints_inverseDirection() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 -2, 0 -3, 2 -4, 4 -6, 6 -8, 7 -10, 8 -12)");
        final LineString polygonSide = (LineString) wktReader.read("LINESTRING(3 -12, 2 -10, 0 -8, -2 -7)");
        final TimeAxis timeAxis = new TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000153143L, 1000000846856L, timeInterval);
    }


    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }
}
