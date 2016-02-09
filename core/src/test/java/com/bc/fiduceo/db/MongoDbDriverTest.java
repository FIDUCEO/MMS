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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MongoDbDriverTest {

    @Test
    public void testGetUrlPattern() {
        final MongoDbDriver driver = new MongoDbDriver();

        assertEquals("mongodb", driver.getUrlPattern());
    }

    @Test
    public void testConvertToGeoJSON_noInput() {
        try {
            MongoDbDriver.convertToGeoJSON(null);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected) {
        }
    }

    @Test
    public void testConvertToGeoJSON_polygon() {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Geometry polygon = geometryFactory.parse("POLYGON((-8 -2, -8 -1, -6 -1, -6 -2, -8 -2))");

        final com.mongodb.client.model.geojson.Geometry geoJSON = MongoDbDriver.convertToGeoJSON(polygon);
        assertNotNull(geoJSON);
        assertEquals("{ \"type\" : \"Polygon\", \"coordinates\" : [[[-6.0, -2.0], [-6.0, -1.0], [-7.999999999999998, -1.0], [-7.999999999999998, -1.9999999999999996], [-6.0, -2.0]]] }",
                geoJSON.toJson());
    }
}
