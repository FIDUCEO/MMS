package com.bc.fiduceo.math;

import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import com.vividsolutions.jts.io.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TimeAxisS2Test {

    private S2WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new S2WKTReader();
    }

    @Test
    public void testGetIntersectionTime_noIntersection() {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(0 -2,4 -2)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNull(timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare() {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-2 0,4 6)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000334421L, 100000666394L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare_shifted() throws ParseException {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-1 1,5 7)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000167518L, 100000499493L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineStart_inside() throws ParseException {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(3 1,6 -2)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100000333299L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineEnd_inside() throws ParseException {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 5,1 1)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        // @todo 2 tb/tb improve calculation of end-time when line ends inside of the polgon 2015-11-20
        assertTimeIntervalEquals(100000749695L, 100000999999L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_line_inside() throws ParseException {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 1,4 1)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_SegmentedLineWithParallelogram() throws ParseException {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((2 -2, 7 -2, 9 -5, 4 -5, 2 -2))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 -6, 2 -4, 4 -3, 6 -3,8 -2)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000384991L, 100000903592L, timeInterval);
    }

    @Test
    public void testCreateSubLineTo_twoPoints_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(5 3, 8 3)");
        final S2Point point = (S2Point) wktReader.read("POINT(6 3)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.01742937278317369, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_twoPoints_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(5 3, 8 3)");
        final S2Point point = (S2Point) wktReader.read("POINT(7 3)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.034858741930189, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_secondSegment_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-0.66667 14)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.06717468411266216, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_secondSegment_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-0.3333 15)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(3, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.08551444403325324, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_firstSegment_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-2.66667 11.3333)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(2, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.00814958946426777, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_firstSegment_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-1.5 12.6667)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(2, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.03876475972081292, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }

    private void assertEqualPoints(S2Point point_1, S2Point point_2) {
        assertEquals(0, point_1.compareTo(point_2));
    }
}
