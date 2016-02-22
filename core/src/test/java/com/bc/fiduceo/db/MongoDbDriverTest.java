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

package com.bc.fiduceo.db;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.s2.S2GeometryFactory;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MongoDbDriverTest {

    private MongoDbDriver driver;
    private GeometryFactory geometryFactory;


    @Before
    public void setUp() {
        driver = new MongoDbDriver();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        driver.setGeometryFactory(geometryFactory);
    }

    @Test
    public void testGetUrlPattern() {
        assertEquals("mongodb", driver.getUrlPattern());
    }

    @Test
    public void testConvertToGeoJSON_noInput() {
        try {
            MongoDbDriver.convertToGeoJSON(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testConvertToGeoJSON_polygon() {
        final Geometry polygon = geometryFactory.parse("POLYGON((-8 -2, -8 -1, -6 -1, -6 -2, -8 -2))");

        final com.mongodb.client.model.geojson.Geometry geoJSON = MongoDbDriver.convertToGeoJSON(polygon);
        assertNotNull(geoJSON);
        assertEquals("{ \"type\" : \"Polygon\", \"coordinates\" : [[[-6.0, -2.0], [-6.0, -1.0], [-7.999999999999998, -1.0], [-7.999999999999998, -1.9999999999999996], [-6.0, -2.0]]] }",
                geoJSON.toJson());
    }

    @Test
    public void testConvertToGeoJSON_lineString() {
        final Geometry polygon = geometryFactory.parse("LINESTRING(1 2, 2 4, 3 -1)");

        final com.mongodb.client.model.geojson.Geometry geoJSON = MongoDbDriver.convertToGeoJSON(polygon);
        assertNotNull(geoJSON);
        assertEquals("{ \"type\" : \"LineString\", \"coordinates\" : [[0.9999999999999998, 2.0], [2.0, 4.0], [3.0000000000000004, -1.0]] }",
                geoJSON.toJson());
    }

    @Test
    public void testConvertToGeoJSON_point() {
        final Geometry polygon = geometryFactory.parse("POINT(2 3)");

        final com.mongodb.client.model.geojson.Geometry geoJSON = MongoDbDriver.convertToGeoJSON(polygon);
        assertNotNull(geoJSON);
        assertEquals("{ \"type\" : \"Point\", \"coordinates\" : [1.9999999999999996, 3.0000000000000004] }",
                geoJSON.toJson());
    }

    @Test
    public void testConvertToGeoJSON_MultiPolygon() {
        MultiPolygon multiPolygon = getMultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90)))");
        com.mongodb.client.model.geojson.MultiPolygon mongoMultiPolygon = (com.mongodb.client.model.geojson.MultiPolygon) MongoDbDriver.convertToGeoJSON(multiPolygon);
        assertNotNull(mongoMultiPolygon);
        List<PolygonCoordinates> polygonCoordinatesList = mongoMultiPolygon.getCoordinates();
        assertEquals(polygonCoordinatesList.size(),2);

        assertEquals(polygonCoordinatesList.get(0).getExterior().size(),5);
        assertEquals(polygonCoordinatesList.get(1).getExterior().size(),4);

        assertEquals(polygonCoordinatesList.get(0).getExterior().get(0).toString(),"Position{values=[20.0, 0.0]}");
        assertEquals(polygonCoordinatesList.get(0).getExterior().get(1).toString(),"Position{values=[49.99999999999999, 0.0]}");

        assertEquals(polygonCoordinatesList.get(1).getExterior().get(0).toString(),"Position{values=[20.0, 70.0]}");
        assertEquals(polygonCoordinatesList.get(1).getExterior().get(1).toString(),"Position{values=[49.99999999999999, 70.0]}");
    }

    @Test
    public void testConvertToGeometry_polygon() {
        final Document jsonPolygon = new Document("type", "Polygon");
        final ArrayList<ArrayList<ArrayList<Double>>> linearRings = new ArrayList<>();
        final ArrayList<ArrayList<Double>> pointList = new ArrayList<>();

        final ArrayList<Double> point_0 = new ArrayList<>();
        point_0.add(-12.0);
        point_0.add(8.0);
        pointList.add(point_0);

        final ArrayList<Double> point_1 = new ArrayList<>();
        point_1.add(-12.0);
        point_1.add(9.0);
        pointList.add(point_1);

        final ArrayList<Double> point_2 = new ArrayList<>();
        point_2.add(-11.0);
        point_2.add(9.0);
        pointList.add(point_2);

        final ArrayList<Double> point_3 = new ArrayList<>();
        point_3.add(-11.0);
        point_3.add(8.0);
        pointList.add(point_3);

        final ArrayList<Double> point_4 = new ArrayList<>();
        point_4.add(-12.0);
        point_4.add(8.0);
        pointList.add(point_4);
        linearRings.add(pointList);
        jsonPolygon.append("coordinates", linearRings);


        final Geometry geometry = driver.convertToGeometry(jsonPolygon);
        assertNotNull(geometry);
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(7.999999999999998, -12.000000000000002)\n" +
                "(9.0, -12.0)\n" +
                "(9.0, -10.999999999999998)\n" +
                "(7.999999999999998, -10.999999999999998)\n" +
                "(7.999999999999998, -12.000000000000002)\n" +
                ">\n", geometry.toString());
    }

    @Test
    public void testConvertToGeometry_multipolygon() {
        final S2WKTReader s2WKTReader = new S2WKTReader();

        Document jsonMultiPolygon = new Document("type", "MultiPolygon");
        List<S2Polygon> s2PolygonList = (List<S2Polygon>) s2WKTReader.read("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90)))");
        final ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> last = new ArrayList<>();

        final ArrayList<ArrayList<ArrayList<Double>>> polygonList = new ArrayList<>();
        for (S2Polygon s2Polygon : s2PolygonList) {
            for (int i = 0; i < s2Polygon.numLoops(); i++) {
                S2Loop loop = s2Polygon.loop(i);
                final ArrayList<ArrayList<Double>> pointsList = new ArrayList<>();
                for (int j = 0; j < loop.numVertices(); j++) {
                    final ArrayList<Double> vertexList = new ArrayList<>();
                    S2Point vertex = loop.vertex(j);
                    vertexList.add(vertex.getX());
                    vertexList.add(vertex.getY());
                    pointsList.add(vertexList);
                }
                polygonList.add(pointsList);
            }
        }
        last.add(polygonList);
        jsonMultiPolygon.append("coordinates", last);
        Geometry geometry = driver.convertToGeometry(jsonMultiPolygon);
        assertNotNull(geometry);
        assertEquals(geometry.getCoordinates().length, 7);
        MultiPolygon multiPolygon = (MultiPolygon) geometry;
        List<Polygon> polygonsList = (List<Polygon>) multiPolygon.getInner();

        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(0.34202014332566866, 0.9396926207859083)\n" +
                "(0.7660444431189782, 0.6427876096865394)\n" +
                "(0.7198463103929542, 0.6040227735550537)\n" +
                "(0.21984631039295416, 0.6040227735550537)\n" +
                ">\n", polygonsList.get(0).toString());


        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(0.11697777844051101, 0.32139380484326974)\n" +
                "(0.2620026302293851, 0.2198463103929543)\n" +
                "(4.6906693763513654E-17, 3.935938943670993E-17)\n" +
                ">\n", polygonsList.get(1).toString());

    }

    @Test
    public void testGetPolygonCoordinates() {
        MultiPolygon multiPolygon = getMultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90)))");
        List<PolygonCoordinates> polygonCoordinates = MongoDbDriver.gePolygonCoordinates(multiPolygon);
        assertNotNull(polygonCoordinates);

        assertEquals(polygonCoordinates.size(),2);

        assertEquals(polygonCoordinates.get(0).getExterior().size(),5);
        assertEquals(polygonCoordinates.get(1).getExterior().size(),4);

        assertEquals(polygonCoordinates.get(0).getExterior().get(0).toString(),"Position{values=[20.0, 0.0]}");
        assertEquals(polygonCoordinates.get(0).getExterior().get(1).toString(),"Position{values=[49.99999999999999, 0.0]}");

        assertEquals(polygonCoordinates.get(1).getExterior().get(0).toString(),"Position{values=[20.0, 70.0]}");
        assertEquals(polygonCoordinates.get(1).getExterior().get(1).toString(),"Position{values=[49.99999999999999, 70.0]}");
    }

    private MultiPolygon getMultiPolygon(String wkt) {
        final S2WKTReader s2WKTReader = new S2WKTReader();
        List<S2Polygon> s2PolygonList = (List<S2Polygon>) s2WKTReader.read(wkt);
        List<Polygon> polygonList = new ArrayList<>();
        S2GeometryFactory s2GeometryFactory = new S2GeometryFactory();
        polygonList.add(s2GeometryFactory.createPolygon(s2PolygonList.get(0)));
        polygonList.add(s2GeometryFactory.createPolygon(s2PolygonList.get(1)));
        return s2GeometryFactory.createMultiPolygon(polygonList);
    }
}
