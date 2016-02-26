/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;
import com.vividsolutions.jts.io.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BcS2TimeAxisTest {

    private S2WKTReader wktReader;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        wktReader = new S2WKTReader();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testGetIntersectionTime_noIntersection() {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(0 -2,4 -2)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNull(timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare() {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-2 0,4 6)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000334421L, 100000666394L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare_shifted() throws ParseException {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-1 1,5 7)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000167518L, 100000499493L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineStart_inside() throws ParseException {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(3 1,6 -2)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100000333299L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineEnd_inside() throws ParseException {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 5,1 1)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        // @todo 2 tb/tb improve calculation of end-time when line ends inside of the polgon 2015-11-20
        assertTimeIntervalEquals(100000749695L, 100000999999L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_line_inside() throws ParseException {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 1,4 1)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_SegmentedLineWithParallelogram() throws ParseException {
        final Polygon polygon = (Polygon) geometryFactory.parse("POLYGON((2 -2, 7 -2, 9 -5, 4 -5, 2 -2))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(1 -6, 2 -4, 4 -3, 6 -3,8 -2)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000384991L, 100000903592L, timeInterval);
    }

    // @todo 3 tb/tb add more tests with more complex geometries, import real satellite data boundaries and check! 2015-11-23

    @Test
    public void testGetTime_PointOnLine() throws ParseException {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(0 0, 4 0)");
        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) geometryFactory.parse("POINT(2 0)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000499999L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments_PointOnLine() throws ParseException {
        final S2Polyline lineString = (S2Polyline) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) geometryFactory.parse("POINT(-2 3)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000500315L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments() throws ParseException {
        final S2Polyline lineString = (S2Polyline) wktReader.read("LINESTRING(2 5, 1 3, -1 1, -3 0)");
        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) geometryFactory.parse("POINT(3 2)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000433196L, time.getTime());   // @todo 2 tb/tb check! this is more than I expected 2015-11-23
    }

    @Test
    public void testGetTime_twoSegments_noProjection() throws ParseException {
        final S2Polyline lineString = (S2Polyline) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final Point point = (Point) geometryFactory.parse("POINT(-7 2)");
        final Date time = timeAxis.getTime(point);
        assertNull(time);
    }

    @Test
    public void testCreateSubLineTo_twoPoints_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(5 3, 8 3)");
        final S2Point point = (S2Point) wktReader.read("POINT(6 3)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.01742937278317369, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_twoPoints_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(5 3, 8 3)");
        final S2Point point = (S2Point) wktReader.read("POINT(7 3)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.034858741930189, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_secondSegment_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-0.66667 14)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.06717468411266216, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_secondSegment_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-0.3333 15)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(3, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.08551444403325324, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_firstSegment_closeToStart() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-2.66667 11.3333)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(2, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.00814958946426777, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    @Test
    public void testCreateSubLineTo_threePoints_firstSegment_closeToEnd() {
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-3 11, -1 13, 0 16)");
        final S2Point point = (S2Point) wktReader.read("POINT(-1.5 12.6667)");

        final BcS2TimeAxis timeAxis = new BcS2TimeAxis(polyline, new Date(), new Date());
        final S2Polyline subLineTo = timeAxis.createSubLineTo(point);

        assertEquals(2, subLineTo.numVertices());
        assertEqualPoints(subLineTo.vertex(0), polyline.vertex(0));
        assertEquals(0.03876475972081292, subLineTo.getArclengthAngle().radians(), 1e-8);
    }

    // @todo 1 tb/tb add tests for getProjectionTime 2015-12-04

    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }

    private void assertEqualPoints(S2Point point_1, S2Point point_2) {
        assertEquals(0, point_1.compareTo(point_2));
    }
}
