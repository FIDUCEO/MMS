
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

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.math.TimeAxisJTS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeometryUtilsTest_JTS {

    private WKTReader wktReader;
    private GeometryFactory factory;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
        factory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testNormalizePolygon_tooSmallArray() {
        Coordinate[] coordinates = new Coordinate[0];
        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(0, coordinates.length);

        coordinates = new Coordinate[1];
        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(1, coordinates.length);
    }

    @Test
    public void testNormalizePolygon_noNormaization() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(10, 10);
        coordinates[1] = new Coordinate(10, 20);
        coordinates[2] = new Coordinate(20, 20);
        coordinates[3] = new Coordinate(20, 10);
        coordinates[4] = new Coordinate(10, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(10, coordinates[0].x, 1e-8);
        assertEquals(10, coordinates[1].x, 1e-8);
        assertEquals(20, coordinates[2].x, 1e-8);
        assertEquals(20, coordinates[3].x, 1e-8);
        assertEquals(10, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeEast() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(170, 10);
        coordinates[1] = new Coordinate(170, 20);
        coordinates[2] = new Coordinate(-175, 20);
        coordinates[3] = new Coordinate(-175, 10);
        coordinates[4] = new Coordinate(170, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(170, coordinates[0].x, 1e-8);
        assertEquals(170, coordinates[1].x, 1e-8);
        assertEquals(185, coordinates[2].x, 1e-8);
        assertEquals(185, coordinates[3].x, 1e-8);
        assertEquals(170, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeWest() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(-170, 10);
        coordinates[1] = new Coordinate(-170, 20);
        coordinates[2] = new Coordinate(175, 20);
        coordinates[3] = new Coordinate(175, 10);
        coordinates[4] = new Coordinate(-170, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(190, coordinates[0].x, 1e-8);
        assertEquals(190, coordinates[1].x, 1e-8);
        assertEquals(175, coordinates[2].x, 1e-8);
        assertEquals(175, coordinates[3].x, 1e-8);
        assertEquals(190, coordinates[4].x, 1e-8);
    }

    @Test
    public void testMapToGlobe_onlyPointsInGlobe() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((10 10, 20 10, 20 20, 10 20, 10 10))");

        final  com.bc.fiduceo.geometry.Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((10 10, 10 20, 20 20, 20 10, 10 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedOnlyGlobe() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((-200 10, -190 10, -190 20, -200 20, -200 10))");

        final com.bc.fiduceo.geometry.Polygon [] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((160 10, 160 20, 170 20, 170 10, 160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedAndCentralGlobe() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((-200 10, -170 10, -170 20, -200 20, -200 10))");

        final  com.bc.fiduceo.geometry.Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -170 20, -170 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedOnlyGlobe() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((200 10, 210 10, 210 20, 200 20, 200 10))");

        final com.bc.fiduceo.geometry.Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((-160 10, -160 20, -150 20, -150 10, -160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedAndCentralGlobe() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((170 10, 210 10, 210 20, 170 20, 170 10))");

        final com.bc.fiduceo.geometry.Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 170 10, 170 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_allShiftsPresent() throws ParseException {
        final com.bc.fiduceo.geometry.Polygon polygonInGlobe = (com.bc.fiduceo.geometry.Polygon) factory.parse("POLYGON((-200 10, 210 10, 210 20, -200 20, -200 10))");

        final com.bc.fiduceo.geometry.Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(3, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, 180 20, 180 10, -180 10))", mappedPolygons[1].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[2].toString());
    }

    @Test
    public void testCreateTimeAxis() throws ParseException {
        final Polygon polygon = (Polygon) factory.parse("POLYGON((10 30, 10 20, 10 10, 20 10, 30 10, 30 20, 30 30, 20 30, 10 30))");

        final TimeAxis timeAxis =  GeometryUtils.createTimeAxis(polygon, 0, 2, new Date(1000), new Date(2000));
        assertNotNull(timeAxis);
        assertEquals(1000, timeAxis.getTime(factory.createPoint(10, 30)).getTime());
        assertEquals(1500, timeAxis.getTime(factory.createPoint(10, 20)).getTime());
        assertEquals(2000, timeAxis.getTime(factory.createPoint(10, 10)).getTime());
    }

    @Test
    public void testPrepareForStorage_onePolygon_oneAxis_descending() {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final List<Point> coordinateList = createCoordinateList(new double[]{10, 10, 10, 30, 30, 30, 10}, new double[]{30, 20, 10, 10, 20, 30, 30});
        acquisitionInfo.setCoordinates(coordinateList);
        acquisitionInfo.setTimeAxisStartIndices(new int[] {0});
        acquisitionInfo.setTimeAxisEndIndices(new int[] {2});
        acquisitionInfo.setSensingStart(new Date(100000));
        acquisitionInfo.setSensingStop(new Date(200000));
        acquisitionInfo.setNodeType(NodeType.DESCENDING);

        final SatelliteGeometry satelliteGeometry = GeometryUtils.prepareForStorage(acquisitionInfo);
        assertNotNull(satelliteGeometry);

        final com.bc.fiduceo.geometry.Geometry geometry = satelliteGeometry.getGeometry();
        assertNotNull(geometry);
        assertEquals("POLYGON ((10 30, 10 20, 10 10, 30 10, 30 20, 30 30, 10 30))", geometry.toString());

        final TimeAxis[] timeAxes = satelliteGeometry.getTimeAxes();
        assertNotNull(timeAxes);
        assertEquals(1, timeAxes.length);
        assertEquals(150000, timeAxes[0].getTime(factory.createPoint(10, 20)).getTime());
    }

    private List<Point> createCoordinateList(double[] lons, double[] lats) {
        final ArrayList<Point> coordinates = new ArrayList<>(lons.length);

        for (int i = 0; i < lons.length; i++){
            coordinates.add(factory.createPoint(lons[i], lats[i]));
        }

        return coordinates;
    }
}
