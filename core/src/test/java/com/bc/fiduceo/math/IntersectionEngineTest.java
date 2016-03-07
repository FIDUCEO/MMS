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


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("ConstantConditions")
public class IntersectionEngineTest {

    // @todo 1 tb/tb extend tests for multipolygon geometries and multiple time axes
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testGetIntersectionTime_noGeometricIntersection() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                                                                              "LINESTRING(2.5 1,2.5 2, 2.5 3, 2.5 4)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((0 5, 0 4, 0 3, 0 2, 1 2, 1 3, 1 4, 1 5, 0 5))",
                                                                              "LINESTRING(0 5, 0 4, 0 3, 0 2)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(0, intersectingIntervals.length);
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                                                                              "LINESTRING(2.5 1,2.5 2, 2.5 3, 2.5 4)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.1 6, 2.1 5, 2.1 4, 2.1 3, 2.9 3, 2.9 4, 2.9 5, 2.9 6, 2.1 6))",
                                                                              "LINESTRING(2.4 6, 2.4 5, 2.4 4, 2.4 3)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1666L, timeInterval.getStartTime().getTime());
        assertEquals(1999L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((2.0999999999999996 3.000000000000001,2.9 3.0000000000000004,2.9 4.0,2.9 4.000054654657567,2.1 4.000054654657567,2.0999999999999996 4.0,2.0999999999999996 3.000000000000001))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending_shiftedLon() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((-4 2, -4 1, -4 0, -4 -1, -4 -2, -3 -2, -3 -1, -3 0, -3 1, -3 2, -4 2))",
                                                                              "LINESTRING(-4 2, -4 1, -4 0, -4 -1, -4 -2)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((-2.5 0, -2.5 1, -2.5 2, -2.5 3, -3.5 3, -3.5 2, -3.5 1, -3.5 0, -2.5 0))",
                                                                              "LINESTRING(-2.5 0, -2.5 1, -2.5 2, -2.5 3)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1500L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((-3.500000000000001 2.000076094919874,-3.5 2.0,-3.5 1.0,-3.5000000000000004 0.0,-3.0000000000000004 0.0,-3.0000000000000004 1.0,-3.0000000000000004 2.0,-3.500000000000001 2.000076094919874))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_noOverlappingTime() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                                                                              "LINESTRING(2.5 4,2.5 3, 2.5 2, 2.5 1)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.1 6, 2.1 5, 2.1 4, 2.1 3, 2.9 3, 2.9 4, 2.9 5, 2.9 6, 2.1 6))",
                                                                              "LINESTRING(2.4 6, 2.4 5, 2.4 4, 2.4 3)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(333, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((2.0999999999999996 3.000000000000001,2.9 3.0000000000000004,2.9 4.0,2.9 4.000054654657567,2.1 4.000054654657567,2.0999999999999996 4.0,2.0999999999999996 3.000000000000001))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_closerInTime() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                                                                              "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.2 5, 2.2 4, 2.2 3, 2.2 2, 3.2 2, 3.2 3, 3.2 4, 3.2 5, 2.2 5))",
                                                                              "LINESTRING(2 5, 2 4, 2 3, 2 2)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1333L, timeInterval.getStartTime().getTime());
        assertEquals(1666L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((2.2 2.0,3.000000000000001 2.000048700638269,3.0000000000000004 3.000000000000001,3.0000000000000004 4.0,2.2 4.000097164003286,2.2 4.0,2.2 3.0000000000000004,2.2 2.0))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_onSamePlatform() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                                                                              "LINESTRING(2.5 4,2.5 3, 2.5 2, 2.5 1)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.1 4, 2.1 3, 2.1 2, 2.1 1, 2.9 1, 2.9 2, 2.9 3, 2.9 4, 2.1 4))",
                                                                              "LINESTRING(2.4 4,2.4 3, 2.4 2, 2.4 1)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1999L, timeInterval.getStopTime().getTime());  // S2 rounding errors tb 2016-03-06
        assertEquals(0, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((2.9 2.0,2.9 3.0000000000000004,2.9 4.0,2.0999999999999996 4.0,2.0999999999999996 3.000000000000001,2.0999999999999996 2.0,2.099999999999999 1.0000137053794702,2.8999999999999995 1.0000137053794704,2.9 2.0))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_intersectingTime() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                                                                              "LINESTRING(2.5 1, 2.5 2, 2.5 3, 2.5 4)", 1000, 2000);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.1 1, 3.1 2, 4.1 3, 5.1 4, 4.1 5, 3.1 4, 2.1 3, 1.1 2, 2.1 1))",
                                                                              "LINESTRING(1.6 1.5, 2.6 2.5, 3.6 3.5, 4.6 4.5)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1633L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((3.0000000000000004 3.000000000000001,3.0000000000000004 3.9001493344833835,2.0999999999999996 3.000000000000001,1.9999999999999996 2.900108250663452,2.0 1.9999999999999996,1.9999999999999993 1.100056217529023,2.0999863046352645 1.000013703710992,2.1000136987000086 1.0000137070482977,3.0 1.900067153632825,3.0000000000000004 2.0,3.0000000000000004 3.000000000000001))", geometryFactory.format(geometry));
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_noIntersectingTime() {
        final SatelliteObservation satelliteGeometry_1 = createSatelliteObservation("POLYGON((3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                                                                              "LINESTRING(2.5 1, 2.5 2, 2.5 3, 2.5 4)", 1900, 2900);
        final SatelliteObservation satelliteGeometry_2 = createSatelliteObservation("POLYGON((2.1 1, 3.1 2, 4.1 3, 5.1 4, 4.1 5, 3.1 4, 2.1 3, 1.1 2, 2.1 1))",
                                                                              "LINESTRING(1.6 1.5, 2.6 2.5, 3.6 3.5, 4.6 4.5)", 1000, 2000);

        final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1, intersectingIntervals.length);

        final TimeInfo timeInfo = intersectingIntervals[0].getTimeInfo();
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(267, timeInfo.getMinimalTimeDelta());

        final Geometry geometry = intersectingIntervals[0].getGeometry();
        assertEquals("POLYGON((3.0000000000000004 3.000000000000001,3.0000000000000004 3.9001493344833835,2.0999999999999996 3.000000000000001,1.9999999999999996 2.900108250663452,2.0 1.9999999999999996,1.9999999999999993 1.100056217529023,2.0999863046352645 1.000013703710992,2.1000136987000086 1.0000137070482977,3.0 1.900067153632825,3.0000000000000004 2.0,3.0000000000000004 3.000000000000001))", geometryFactory.format(geometry));
    }

    @Test
    public void testCalculateTimeDelta() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, IntersectionEngine.calculateTimeDelta(interval_1, interval_2));
    }

    @Test
    public void testCalculateTimeDelta_intervalsAreReordered() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, IntersectionEngine.calculateTimeDelta(interval_2, interval_1));
    }

    private SatelliteObservation createSatelliteObservation(String polygonWkt, String lineWkt, int startTime, int stopTime) {
        final Polygon polygon = (Polygon) geometryFactory.parse(polygonWkt);
        final LineString lineString = (LineString) geometryFactory.parse(lineWkt);
        final TimeAxis timeAxis = geometryFactory.createTimeAxis(lineString, new Date(startTime), new Date(stopTime));
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setGeoBounds(polygon);
        observation.setTimeAxes(new TimeAxis[]{timeAxis});
        return observation;
    }
}
