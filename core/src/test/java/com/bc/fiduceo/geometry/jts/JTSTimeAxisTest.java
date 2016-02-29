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

package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.math.TimeInterval;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JTSTimeAxisTest {

    private WKTReader wktReader;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testGetIntersectionTime_noIntersection() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(0 -2,4 -2)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNull(timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 0,4 6)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000333333L, 100000666666L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare_shifted() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-1 1,5 7)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000166666L, 100000499999L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineStart_inside() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(3 1,6 -2)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100000333333L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_lineEnd_inside() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 5,1 1)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000750000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithRectangle_line_inside() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((0 0, 0 2, 5 2, 5 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 1,4 1)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000000000L, 100001000000L, timeInterval);
    }

    @Test
    public void testGetIntersectionTime_SegmentedLineWithParallelogram() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse("POLYGON((2 -2, 7 -2, 9 -5, 4 -5, 2 -2))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 -6, 2 -4, 4 -3, 6 -3,8 -2)");

        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(100000385165L, 100000903707L, timeInterval);
    }

    // @todo 3 tb/tb add more tests with more complex geometries, import real satellite data boundaries and check! 2015-08-14

    @Test
    public void testGetTime_PointOnLine() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(0 0, 4 0)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final com.bc.fiduceo.geometry.Point point = (com.bc.fiduceo.geometry.Point) geometryFactory.parse("POINT(2 0)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000500000L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments_PointOnLine() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final com.bc.fiduceo.geometry.Point point = (com.bc.fiduceo.geometry.Point) geometryFactory.parse("POINT(-2 3)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000500000L, time.getTime());
    }

    @Test
    public void testGetTime_threeSegments() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(2 5, 1 3, -1 1, -3 0)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final com.bc.fiduceo.geometry.Point point = (com.bc.fiduceo.geometry.Point) geometryFactory.parse("POINT(3 2)");
        final Date time = timeAxis.getTime(point);
        assertEquals(1000000306287L, time.getTime());
    }

    @Test
    public void testGetTime_twoSegments_noProjection() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, -1 2, -3 4, -5 4)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final com.bc.fiduceo.geometry.Point point = (com.bc.fiduceo.geometry.Point) geometryFactory.parse("POINT(-7 2)");
        final Date time = timeAxis.getTime(point);
        assertNull(time);
    }

    @Test
    public void testGetProjectionTime_line() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, 0 3, -3 4, -5 4)");
        final com.bc.fiduceo.geometry.LineString polygonSide = (com.bc.fiduceo.geometry.LineString) geometryFactory.parse("LINESTRING(-2 0, -4 2)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000107520L, 1000000847943L, timeInterval);
    }

    @Test
    public void testGetProjectionTime_line_inverseDirection() throws ParseException {
        final LineString lineString = (LineString) wktReader.read("LINESTRING(1 2, 0 3, -3 4, -5 4)");
        final com.bc.fiduceo.geometry.LineString polygonSide = (com.bc.fiduceo.geometry.LineString) geometryFactory.parse("LINESTRING(-4 2, -2 0)");
        final JTSTimeAxis timeAxis = new JTSTimeAxis(lineString, new Date(1000000000000L), new Date(1000001000000L));

        final TimeInterval timeInterval = timeAxis.getProjectionTime(polygonSide);
        assertNotNull(timeInterval);
        assertTimeIntervalEquals(1000000107520L, 1000000847943L, timeInterval);
    }

    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }
}
