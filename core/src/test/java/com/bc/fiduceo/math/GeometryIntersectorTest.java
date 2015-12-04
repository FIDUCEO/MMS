
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

package com.bc.fiduceo.math;


import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.TimeAxis;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("ConstantConditions")
public class GeometryIntersectorTest {

    // @todo 1 tb/tb extend tests for multipolygon geometries and multiple time axes
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testGetIntersectionTime_noGeometricIntersection() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                "LINESTRING(2 1,2 2, 2 3, 2 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((0 5, 0 4, 0 3, 0 2, 1 2, 1 3, 1 4, 1 5, 0 5))",
                "LINESTRING(0 5, 0 4, 0 3, 0 2)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(Integer.MAX_VALUE, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                "LINESTRING(2 1,2 2, 2 3, 2 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))",
                "LINESTRING(2 6, 2 5, 2 4, 2 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1666L, timeInterval.getStartTime().getTime());
        assertEquals(2000L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending_shiftedLon() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((-4 2, -4 1, -4 0, -4 -1, -4 -2, -3 -2, -3 -1, -3 0, -3 1, -3 2, -4 2))",
                "LINESTRING(-4 2, -4 1, -4 0, -4 -1, -4 -2)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((-2.5 0, -2.5 1, -2.5 2, -2.5 3, -3.5 3, -3.5 2, -3.5 1, -3.5 0, -2.5 0))",
                "LINESTRING(-2.5 0, -2.5 1, -2.5 2, -2.5 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1500L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_noOverlappingTime() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))",
                "LINESTRING(2 6, 2 5, 2 4, 2 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(333, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_closerInTime() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 5, 2 4, 2 3, 2 2, 3 2, 3 3, 3 4, 3 5, 2 5))",
                "LINESTRING(2 5, 2 4, 2 3, 2 2)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1333L, timeInterval.getStartTime().getTime());
        assertEquals(1666L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSamePlatform() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(2000L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_intersectingTime() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((3 1, 3 2, 3 3, 4 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                "LINESTRING(3 1, 3 2, 3 3, 3 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 1, 3 2, 4 3, 5 4, 4 5, 3 4, 2 3, 1 2, 2 1))",
                "LINESTRING(2 1, 3 2, 4 3, 5 4)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1833L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_noIntersectingTime() {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((3 1, 3 2, 3 3, 4 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                "LINESTRING(3 1, 3 2, 3 3, 3 4)", 1900, 2900);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 1, 3 2, 4 3, 5 4, 4 5, 3 4, 2 3, 1 2, 2 1))",
                "LINESTRING(2 1, 3 2, 4 3, 5 4)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(67, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testCalculateTimeDelta() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, GeometryIntersector.calculateTimeDelta(interval_1, interval_2));
    }

    @Test
    public void testCalculateTimeDelta_intervalsAreReordered() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, GeometryIntersector.calculateTimeDelta(interval_2, interval_1));
    }

    private SatelliteGeometry createSatelliteGeometry(String polygonWkt, String lineWkt, int startTime, int stopTime) {
        final com.bc.fiduceo.geometry.Polygon polygon = (com.bc.fiduceo.geometry.Polygon) geometryFactory.parse(polygonWkt);
        final com.bc.fiduceo.geometry.LineString lineString = (com.bc.fiduceo.geometry.LineString) geometryFactory.parse(lineWkt);
        final TimeAxis timeAxis = geometryFactory.createTimeAxis(lineString, new Date(startTime), new Date(stopTime));
        return new SatelliteGeometry(polygon, new TimeAxis[]{timeAxis});
    }
}
