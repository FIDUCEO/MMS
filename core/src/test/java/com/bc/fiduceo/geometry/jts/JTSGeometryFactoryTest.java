/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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


import com.bc.fiduceo.geometry.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JTSGeometryFactoryTest {

    private JtsGeometryFactory factory;
    private WKTReader wktReader;

    @Before
    public void setUp() {
        factory = new JtsGeometryFactory();
        wktReader = new WKTReader();
    }

    @Test
    public void testMapToGlobe_onlyPointsInGlobe() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((10 10, 20 10, 20 20, 10 20, 10 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((10 10, 10 20, 20 20, 20 10, 10 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedOnlyGlobe() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((-200 10, -190 10, -190 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((160 10, 160 20, 170 20, 170 10, 160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedAndCentralGlobe() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((-200 10, -170 10, -170 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -170 20, -170 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedAndCentralGlobe() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((170 10, 210 10, 210 20, 170 20, 170 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 170 10, 170 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedOnlyGlobe() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((200 10, 210 10, 210 20, 200 20, 200 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((-160 10, -160 20, -150 20, -150 10, -160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_allShiftsPresent() throws ParseException {
        final Polygon polygonInGlobe = (Polygon) wktReader.read("POLYGON((-200 10, 210 10, 210 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = factory.mapToGlobe(polygonInGlobe);
        assertEquals(3, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, 180 20, 180 10, -180 10))", mappedPolygons[1].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[2].toString());
    }

    @Test
    public void testExtractCoordinates() {
        final List<Point> pointList = new ArrayList<>();
        pointList.add(factory.createPoint(10, 11));
        pointList.add(factory.createPoint(12, 13));
        pointList.add(factory.createPoint(14, 15));

        final Coordinate[] coordinates = JtsGeometryFactory.extractCoordinates(pointList);
        assertNotNull(coordinates);
        assertEquals(3, coordinates.length);
        assertEquals(12, coordinates[1].x, 1e-8);
        assertEquals(13, coordinates[1].y, 1e-8);
    }

    @Test
    public void testExtractCoordinates_emptyList() {
        final List<Point> pointList = new ArrayList<>();

        final Coordinate[] coordinates = JtsGeometryFactory.extractCoordinates(pointList);
        assertNotNull(coordinates);
        assertEquals(0, coordinates.length);
    }

    @Test
    public void testCreatePolygon_closedPointList() {
        final List<Point> pointList = new ArrayList<>();
        pointList.add(factory.createPoint(10, 11));
        pointList.add(factory.createPoint(10, 12));
        pointList.add(factory.createPoint(14, 15));
        pointList.add(factory.createPoint(10, 11));

        final com.bc.fiduceo.geometry.Polygon polygon = factory.createPolygon(pointList);
        assertEquals("POLYGON ((10 11, 10 12, 14 15, 10 11))", polygon.toString());
    }

    @Test
    public void testCreatePolygon_openPointList() {
        final List<Point> pointList = new ArrayList<>();
        pointList.add(factory.createPoint(-2, 3));
        pointList.add(factory.createPoint(-3, 3));
        pointList.add(factory.createPoint(-1, 1));

        final com.bc.fiduceo.geometry.Polygon polygon = factory.createPolygon(pointList);
        assertEquals("POLYGON ((-2 3, -1 1, -3 3, -2 3))", polygon.toString());
    }
}
