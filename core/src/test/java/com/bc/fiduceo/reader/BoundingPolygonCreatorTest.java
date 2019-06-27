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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class BoundingPolygonCreatorTest {

    private BoundingPolygonCreator boundingPolygonCreator;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Interval interval = new Interval(2, 2);

        boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);
    }


    @Test
    public void testClosePolygon_emptyList() {
        final ArrayList<Point> coordinates = new ArrayList<>();

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(0, coordinates.size());
    }

    @Test
    public void testClosePolygon() {
        final ArrayList<Point> coordinates = new ArrayList<>();
        coordinates.add(geometryFactory.createPoint(0, 0));
        coordinates.add(geometryFactory.createPoint(1, 1));
        coordinates.add(geometryFactory.createPoint(1, 3));

        BoundingPolygonCreator.closePolygon(coordinates);

        assertEquals(4, coordinates.size());
        final Point closingCoordinate = coordinates.get(3);
        assertEquals(0, closingCoordinate.getLon(), 1e-8);
        assertEquals(0, closingCoordinate.getLat(), 1e-8);
    }

    @Test
    public void testThrowsOnInvalidInterval() {
        Interval interval = new Interval(0, 8);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        interval = new Interval(12, 0);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }

        interval = new Interval(-3, 0);
        try {
            new BoundingPolygonCreator(interval, geometryFactory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreateBoundingGeometry_AIRS() {
        final Array latitudes = Array.factory(AIRS_LATITUDES);
        final Array longitudes = Array.factory(AIRS_LONGITUDES);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(9, coordinates.length);

        // checks left side downwards
        assertEquals(138.19514475348302, coordinates[0].getLon(), 1e-8);
        assertEquals(71.15288152754994, coordinates[0].getLat(), 1e-8);

        assertEquals(137.32780413935305, coordinates[1].getLon(), 1e-8);
        assertEquals(71.32088787959934, coordinates[1].getLat(), 1e-8);

        assertEquals(136.90199908664985, coordinates[2].getLon(), 1e-8);
        assertEquals(71.41032171663477, coordinates[2].getLat(), 1e-8);

        // checks right side upwards
        assertEquals(138.53923435004424, coordinates[4].getLon(), 1e-8);
        assertEquals(72.21432551071354, coordinates[4].getLat(), 1e-8);

        assertEquals(139.43625059118625, coordinates[5].getLon(), 1e-8);
        assertEquals(72.03976926305718, coordinates[5].getLat(), 1e-8);

        assertEquals(139.86561480588978, coordinates[6].getLon(), 1e-8);
        assertEquals(71.9452820772289, coordinates[6].getLat(), 1e-8);
    }

    @Test
    public void testCreateBoundingGeometryClockwise_AIRS() {
        final Array latitudes = Array.factory(AIRS_LATITUDES);
        final Array longitudes = Array.factory(AIRS_LONGITUDES);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(9, coordinates.length);

        // checks left side downwards
        assertEquals(138.19514475348302, coordinates[0].getLon(), 1e-8);
        assertEquals(71.15288152754994, coordinates[0].getLat(), 1e-8);

        assertEquals(138.77287682180165, coordinates[1].getLon(), 1e-8);
        assertEquals(71.4359164390965, coordinates[1].getLat(), 1e-8);

        assertEquals(139.86561480588978, coordinates[2].getLon(), 1e-8);
        assertEquals(71.9452820772289, coordinates[2].getLat(), 1e-8);

        // checks right side upwards
        assertEquals(138.53923435004424, coordinates[4].getLon(), 1e-8);
        assertEquals(72.21432551071354, coordinates[4].getLat(), 1e-8);

        assertEquals(138.01571817610454, coordinates[5].getLon(), 1e-8);
        assertEquals(71.96597011172345, coordinates[5].getLat(), 1e-8);

        assertEquals(136.90199908664985, coordinates[6].getLon(), 1e-8);
        assertEquals(71.41032171663477, coordinates[6].getLat(), 1e-8);
    }

    @Test
    public void testCreateBoundingGeometry_AVHRR_intervalVaried() {
        final Array latitudes = Array.factory(AVHRR_LATITUDES);
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);

        final Interval interval = new Interval(2, 3);
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitudes, latitudes);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof Polygon);

        final Point[] coordinates = boundingGeometry.getCoordinates();
        assertEquals(9, coordinates.length);

        // checks left side downwards
        assertEquals(-114.985, coordinates[0].getLon(), 1e-8);
        assertEquals(6.43, coordinates[0].getLat(), 1e-8);

        assertEquals(-115.003006, coordinates[1].getLon(), 1e-8);
        assertEquals(6.344, coordinates[1].getLat(), 1e-8);

        // checks bottom side left->right
        assertEquals(-115.020996, coordinates[2].getLon(), 1e-8);
        assertEquals(6.258, coordinates[2].getLat(), 1e-8);

        assertEquals(-114.616, coordinates[3].getLon(), 1e-8);
        assertEquals(6.204, coordinates[3].getLat(), 1e-8);

        assertEquals(-114.421005, coordinates[4].getLon(), 1e-8);
        assertEquals(6.178, coordinates[4].getLat(), 1e-8);
    }

    @Test
    public void testCreateBoundingGeometrySplitted_splitInTwo() throws IOException {
        final Array latitudes = Array.factory(AVHRR_LATITUDES);
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, 2, false);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof MultiPolygon);

        final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        // check upper part
        Point[] coordinates = polygons.get(0).getCoordinates();
        assertEquals(9, coordinates.length);

        assertEquals(-114.985, coordinates[0].getLon(), 1e-8);
        assertEquals(6.43, coordinates[0].getLat(), 1e-8);

        assertEquals(-114.996994, coordinates[1].getLon(), 1e-8);
        assertEquals(6.373, coordinates[1].getLat(), 1e-8);

        assertEquals(-115.003006, coordinates[2].getLon(), 1e-8);
        assertEquals(6.344, coordinates[2].getLat(), 1e-8);

        assertEquals(-114.77901, coordinates[7].getLon(), 1e-8);
        assertEquals(6.402, coordinates[7].getLat(), 1e-8);

        // check lower part
        coordinates = polygons.get(1).getCoordinates();
        assertEquals(9, coordinates.length);

        assertEquals(-115.003006, coordinates[0].getLon(), 1e-8);
        assertEquals(6.344, coordinates[0].getLat(), 1e-8);

        assertEquals(-115.015, coordinates[1].getLon(), 1e-8);
        assertEquals(6.287, coordinates[1].getLat(), 1e-8);

        assertEquals(-115.020996, coordinates[2].getLon(), 1e-8);
        assertEquals(6.258, coordinates[2].getLat(), 1e-8);

        assertEquals(-114.797, coordinates[7].getLon(), 1e-8);
        assertEquals(6.317, coordinates[7].getLat(), 1e-8);
    }

    @Test
    public void testCreateBoundingGeometrySplitted_clockwise_splitInTwo() throws IOException {
        final Array latitudes = Array.factory(AVHRR_LATITUDES);
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, 2, true);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof MultiPolygon);

        final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(2, polygons.size());

        // check upper part
        Point[] coordinates = polygons.get(0).getCoordinates();
        assertEquals(9, coordinates.length);

        assertEquals(-114.985, coordinates[0].getLon(), 1e-8);
        assertEquals(6.43, coordinates[0].getLat(), 1e-8);

        assertEquals(-114.77901, coordinates[1].getLon(), 1e-8);
        assertEquals(6.402, coordinates[1].getLat(), 1e-8);

        assertEquals(-114.384995, coordinates[2].getLon(), 1e-8);
        assertEquals(6.35, coordinates[2].getLat(), 1e-8);

        assertEquals(-114.996994, coordinates[7].getLon(), 1e-8);
        assertEquals(6.373, coordinates[7].getLat(), 1e-8);

        // check lower part
        coordinates = polygons.get(1).getCoordinates();
        assertEquals(9, coordinates.length);

        assertEquals(-115.003006, coordinates[0].getLon(), 1e-8);
        assertEquals(6.344, coordinates[0].getLat(), 1e-8);

        assertEquals(-114.797, coordinates[1].getLon(), 1e-8);
        assertEquals(6.317, coordinates[1].getLat(), 1e-8);

        assertEquals(-114.403, coordinates[2].getLon(), 1e-8);
        assertEquals(6.264, coordinates[2].getLat(), 1e-8);

        assertEquals(-115.015, coordinates[7].getLon(), 1e-8);
        assertEquals(6.287, coordinates[7].getLat(), 1e-8);
    }

    @Test
    public void testCreateBoundingGeometrySplitted_splitInThree() throws IOException {
        final Array latitudes = Array.factory(AVHRR_LATITUDES);
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, 3, false);
        assertNotNull(boundingGeometry);
        assertTrue(boundingGeometry instanceof MultiPolygon);

        final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
        final List<Polygon> polygons = multiPolygon.getPolygons();
        assertEquals(3, polygons.size());

        // check upper part
        Point[] coordinates = polygons.get(0).getCoordinates();
        assertEquals(7, coordinates.length);

        assertEquals(-114.985, coordinates[0].getLon(), 1e-8);
        assertEquals(6.43, coordinates[0].getLat(), 1e-8);

        assertEquals(-114.996994, coordinates[1].getLon(), 1e-8);
        assertEquals(6.373, coordinates[1].getLat(), 1e-8);

        assertEquals(-114.591, coordinates[2].getLon(), 1e-8);
        assertEquals(6.318, coordinates[2].getLat(), 1e-8);

        // check middle part
        coordinates = polygons.get(1).getCoordinates();
        assertEquals(7, coordinates.length);

        assertEquals(-114.996994, coordinates[0].getLon(), 1e-8);
        assertEquals(6.373, coordinates[0].getLat(), 1e-8);

        assertEquals(-115.009, coordinates[1].getLon(), 1e-8);
        assertEquals(6.315, coordinates[1].getLat(), 1e-8);

        assertEquals(-114.604004, coordinates[2].getLon(), 1e-8);
        assertEquals(6.261, coordinates[2].getLat(), 1e-8);

        // check lower part
        coordinates = polygons.get(2).getCoordinates();
        assertEquals(7, coordinates.length);

        assertEquals(-115.009, coordinates[0].getLon(), 1e-8);
        assertEquals(6.315, coordinates[0].getLat(), 1e-8);

        assertEquals(-115.020996, coordinates[1].getLon(), 1e-8);
        assertEquals(6.258, coordinates[1].getLat(), 1e-8);

        assertEquals(-114.616, coordinates[2].getLon(), 1e-8);
        assertEquals(6.204, coordinates[2].getLat(), 1e-8);
    }

    @Test
    public void testCreateTimeAxisGeometry_AVHRR() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);
        final Array latitudes = Array.factory(AVHRR_LATITUDES);

        final LineString lineString = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        assertEquals("LINESTRING(-114.578995 6.376000000000001,-114.591 6.318,-114.604004 6.261,-114.616 6.204)", geometryFactory.format(lineString));
    }

    @Test
    public void testCreateTimeAxisGeometry_AVHRR_intervalVaried() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);
        final Array latitudes = Array.factory(AVHRR_LATITUDES);

        final Interval interval = new Interval(2, 4);
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);

        final LineString lineString = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        assertEquals("LINESTRING(-114.578995 6.376000000000001,-114.604004 6.261,-114.616 6.204)", geometryFactory.format(lineString));
    }

    @Test
    public void testCreateTimeAxisGeometrySplitted_twoSegments() throws IOException {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);
        final Array latitudes = Array.factory(AVHRR_LATITUDES);

        final Geometry geometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, 2);
        assertTrue(geometry instanceof GeometryCollection);
        final GeometryCollection geometryCollection = (GeometryCollection) geometry;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(2, geometries.length);

        assertEquals("LINESTRING(-114.578995 6.376000000000001,-114.591 6.318,-114.597 6.290000000000001)", geometryFactory.format(geometries[0]));
        assertEquals("LINESTRING(-114.597 6.290000000000001,-114.61 6.2330000000000005,-114.616 6.204)", geometryFactory.format(geometries[1]));
    }

    @Test
    public void testCreateTimeAxisGeometrySplitted_threeSegments() throws IOException {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);
        final Array latitudes = Array.factory(AVHRR_LATITUDES);

        final Geometry geometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, 3);
        assertTrue(geometry instanceof GeometryCollection);
        final GeometryCollection geometryCollection = (GeometryCollection) geometry;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(3, geometries.length);

        assertEquals("LINESTRING(-114.578995 6.376000000000001,-114.591 6.318)", geometryFactory.format(geometries[0]));
        assertEquals("LINESTRING(-114.591 6.318,-114.604004 6.261)", geometryFactory.format(geometries[1]));
        assertEquals("LINESTRING(-114.604004 6.261,-114.616 6.204)", geometryFactory.format(geometries[2]));
    }

    @Test
    public void testCreateTimeAxisGeometry_AIRS() {
        final Array longitudes = Array.factory(AIRS_LONGITUDES);
        final Array latitudes = Array.factory(AIRS_LATITUDES);

        final LineString lineString = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        assertEquals("LINESTRING(139.3232587268979 71.69661607793569,138.4586123358709 71.87850964766172,138.01571817610454 71.96597011172345)", geometryFactory.format(lineString));
    }

    @Test
    public void testCreateTimeAxisGeometry_AIRS_intervalVaried() {
        final Array longitudes = Array.factory(AIRS_LONGITUDES);
        final Array latitudes = Array.factory(AIRS_LATITUDES);

        final Interval interval = new Interval(2, 3);
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(interval, geometryFactory);

        final LineString lineString = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        assertEquals("LINESTRING(139.3232587268979 71.69661607793569,138.01571817610454 71.96597011172345)", geometryFactory.format(lineString));
    }

    @Test
    public void testExtractValidIntervals_noGaps() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES);
        final double fillValue = -32768.0;

        final Interval[] validIntervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        assertEquals(1, validIntervals.length);
        assertEquals(0, validIntervals[0].getX());
        assertEquals(6, validIntervals[0].getY());
    }

    @Test
    public void testExtractValidIntervals_oneGap() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES_ONE_GAP);
        final double fillValue = -32768.0;

        final Interval[] validIntervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        assertEquals(2, validIntervals.length);
        assertEquals(0, validIntervals[0].getX());
        assertEquals(4, validIntervals[0].getY());

        assertEquals(7, validIntervals[1].getX());
        assertEquals(12, validIntervals[1].getY());
    }

    @Test
    public void testExtractValidIntervals_twoGaps() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES_TWO_GAPS);
        final double fillValue = -32768.0;

        final Interval[] validIntervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        assertEquals(3, validIntervals.length);
        assertEquals(0, validIntervals[0].getX());
        assertEquals(4, validIntervals[0].getY());

        assertEquals(7, validIntervals[1].getX());
        assertEquals(11, validIntervals[1].getY());

        assertEquals(14, validIntervals[2].getX());
        assertEquals(18, validIntervals[2].getY());
    }

    @Test
    public void testExtractValidIntervals_noGaps_startWithFill() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES_START_WITH_FILL);
        final double fillValue = -32768.0;

        final Interval[] validIntervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        assertEquals(1, validIntervals.length);
        assertEquals(2, validIntervals[0].getX());
        assertEquals(8, validIntervals[0].getY());
    }

    @Test
    public void testExtractValidIntervals_noGaps_endWithFill() {
        final Array longitudes = Array.factory(AVHRR_LONGITUDES_END_WITH_FILL);
        final double fillValue = -32768.0;

        final Interval[] validIntervals = boundingPolygonCreator.extractValidIntervals(longitudes, fillValue);
        assertEquals(1, validIntervals.length);
        assertEquals(0, validIntervals[0].getX());
        assertEquals(6, validIntervals[0].getY());
    }

    @Test
    public void testIsFillValue() {
        assertTrue(BoundingPolygonCreator.isFillValue(-1000.0, -1000.0));
        assertFalse(BoundingPolygonCreator.isFillValue(34.8, -1000.0));
    }

    private static final double[][] AIRS_LONGITUDES = new double[][]{
            {138.19514475348302, 138.77287682180165, 139.3232587268979, 139.86561480588978},
            {137.7680766938059, 138.34196788102574, 138.888842745419, 139.43625059118625},
            {137.32780413935305, 137.90682957068157, 138.4586123358709, 138.9939729311918},
            {136.90199908664985, 137.46778019306842, 138.01571817610454, 138.53923435004424}};

    private static final double[][] AIRS_LATITUDES = new double[][]{
            {71.15288152754994, 71.4359164390965, 71.69661607793569, 71.9452820772289},
            {71.23974580787146, 71.52412094894252, 71.78608894421787, 72.03976926305718},
            {71.32088787959934, 71.61122828082071, 71.87850964766172, 72.12942839534938},
            {71.41032171663477, 71.69739504897453, 71.96597011172345, 72.21432551071354}};

    private static final double[][] AVHRR_LONGITUDES = new double[][]{
            {-114.985, -114.77901, -114.578995, -114.384995},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.996994, -114.791, -114.591, -114.397},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.015, -114.809006, -114.61, -114.41499},
            {-115.020996, -114.815, -114.616, -114.421005}
    };

    private static final double[][] AVHRR_LATITUDES = new double[][]{
            {6.43, 6.402, 6.376, 6.35},
            {6.401, 6.374, 6.347, 6.321},
            {6.373, 6.345, 6.318, 6.292},
            {6.344, 6.317, 6.29, 6.264},
            {6.315, 6.288, 6.261, 6.235},
            {6.287, 6.26, 6.233, 6.207},
            {6.258, 6.231, 6.204, 6.178}
    };

    private static final double[][] AVHRR_LONGITUDES_ONE_GAP = new double[][]{
            {-114.985, -114.77901, -114.578995, -114.384995},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.996994, -114.791, -114.591, -114.397},
            {-114.996994, -114.791, -114.591, -114.397},
            {-114.996994, -114.791, -114.591, -114.397},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.015, -114.809006, -114.61, -114.41499},
            {-115.020996, -114.815, -114.616, -114.421005}
    };

    private static final double[][] AVHRR_LONGITUDES_TWO_GAPS = new double[][]{
            {-114.985, -114.77901, -114.578995, -114.384995},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.996994, -114.791, -114.591, -114.397},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-115.015, -114.809006, -114.61, -114.41499},
            {-115.020996, -114.815, -114.616, -114.421005},
            {-115.020996, -114.815, -114.616, -114.421005},
            {-115.020996, -114.815, -114.616, -114.421005},
            {-115.020996, -114.815, -114.616, -114.421005}
    };

    private static final double[][] AVHRR_LONGITUDES_START_WITH_FILL = new double[][]{
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-114.985, -114.77901, -114.578995, -114.384995},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.996994, -114.791, -114.591, -114.397},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.015, -114.809006, -114.61, -114.41499},
            {-115.020996, -114.815, -114.616, -114.421005}
    };

    private static final double[][] AVHRR_LONGITUDES_END_WITH_FILL = new double[][]{
            {-114.985, -114.77901, -114.578995, -114.384995},
            {-114.991, -114.785, -114.58501, -114.39101},
            {-114.996994, -114.791, -114.591, -114.397},
            {-115.003006, -114.797, -114.597, -114.403},
            {-115.009, -114.80299, -114.604004, -114.409},
            {-115.015, -114.809006, -114.61, -114.41499},
            {-115.020996, -114.815, -114.616, -114.421005},
            {-32768.0, -32768.0, -32768.0, -32768.0},
            {-32768.0, -32768.0, -32768.0, -32768.0}
    };
}
