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
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
}
