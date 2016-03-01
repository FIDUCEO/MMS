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


import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.GeometryCollection;
import org.apache.commons.dbcp.BasicDataSource;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestSpike_MultipleGeometries_MongoDb {

    protected Storage storage;
    protected GeometryFactory geometryFactory;
    private MongoCollection<Document> collection;

    @Before
    public void setUp() throws SQLException, IOException {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("mongodb");
        dataSource.setUrl("mongodb://localhost:27017/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");

        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();

        final MongoDbDriver driver = (MongoDbDriver) storage.getDriver();
        final MongoDatabase database = driver.getDatabase();
        collection = database.getCollection("SATELLITE_OBSERVATION");
    }

    @After
    public void tearDown() throws SQLException, IOException {
        if (storage != null) {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testInsertMultiplePolygonGeometries() {
        final Polygon polygon_1 = (Polygon) geometryFactory.parse("POLYGON((-1 0, 1 0, 1 3, -1 3, -1 0))");
        final Polygon polygon_2 = (Polygon) geometryFactory.parse("POLYGON((2 0, 3 0, 3 3, 2 3, 2 0))");

        final Document inputDocument = new Document("entry_name", "the_test_entity");
        final GeometryCollection geometryCollection = createGeometryCollection(polygon_1, polygon_2);
        inputDocument.append("geo_field", geometryCollection);

        collection.insertOne(inputDocument);

        final FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            final Document geoBounds = (Document) document.get("geo_field");
        }
    }

    @Test
    public void testInsertMultiplePolygonGeometries_geoSearch_intersectsNone() {
        final Polygon searchGeometry = (Polygon) geometryFactory.parse("POLYGON((-10 0, -9 0, -9 3, -10 3, -10 0))");
        final Polygon polygon_1 = (Polygon) geometryFactory.parse("POLYGON((-1 0, 1 0, 1 3, -1 3, -1 0))");
        final Polygon polygon_2 = (Polygon) geometryFactory.parse("POLYGON((2 0, 3 0, 3 3, 2 3, 2 0))");

        final Document inputDocument = new Document("entry_name", "the_test_entity");
        final GeometryCollection geometryCollection = createGeometryCollection(polygon_1, polygon_2);
        inputDocument.append("geo_field", geometryCollection);

        collection.insertOne(inputDocument);

        final Document queryConstraints = new Document();
        queryConstraints.append("geo_field", new Document("$geoIntersects",
                new Document("$geometry", MongoDbDriver.convertToGeoJSON(searchGeometry))));

        final FindIterable<Document> documents = collection.find(queryConstraints);
        for (Document document : documents) {
            final Document geoBounds = (Document) document.get("geo_field");
        }
    }

    @Test
    public void testInsertMultiplePolygonGeometries_geoSearch_intersectsFirst() {
        final Polygon searchGeometry = (Polygon) geometryFactory.parse("POLYGON((-1 0, 0 0, 0 1, -1 1, -1 0))");
        final Polygon polygon_1 = (Polygon) geometryFactory.parse("POLYGON((-1 0, 1 0, 1 3, -1 3, -1 0))");
        final Polygon polygon_2 = (Polygon) geometryFactory.parse("POLYGON((2 0, 3 0, 3 3, 2 3, 2 0))");

        final Document inputDocument = new Document("entry_name", "the_test_entity");
        final GeometryCollection geometryCollection = createGeometryCollection(polygon_1, polygon_2);
        inputDocument.append("geo_field", geometryCollection);

        collection.insertOne(inputDocument);

        final Document queryConstraints = new Document();
        queryConstraints.append("geo_field", new Document("$geoIntersects",
                new Document("$geometry", MongoDbDriver.convertToGeoJSON(searchGeometry))));

        final FindIterable<Document> documents = collection.find(queryConstraints);
        for (Document document : documents) {
            final Document geoBounds = (Document) document.get("geo_field");
        }
    }

    @Test
    public void testInsertMultiplePolygonGeometries_overlapping_geoSearch_intersectsFirst() {
        final Polygon searchGeometry = (Polygon) geometryFactory.parse("POLYGON((-1 0, 0 0, 0 1, -1 1, -1 0))");
        final Polygon polygon_1 = (Polygon) geometryFactory.parse("POLYGON((-1 0, 1 0, 1 3, -1 3, -1 0))");
        final Polygon polygon_2 = (Polygon) geometryFactory.parse("POLYGON((0.8 0, 3 0, 3 1, 0.8 1, 0.8 0))");

        final Document inputDocument = new Document("entry_name", "the_test_entity");
        final GeometryCollection geometryCollection = createGeometryCollection(polygon_1, polygon_2);
        inputDocument.append("geo_field", geometryCollection);

        collection.insertOne(inputDocument);

        final Document queryConstraints = new Document();
        queryConstraints.append("geo_field", new Document("$geoIntersects",
                new Document("$geometry", MongoDbDriver.convertToGeoJSON(searchGeometry))));

        final FindIterable<Document> documents = collection.find(queryConstraints);
        for (Document document : documents) {
            final Document geoBounds = (Document) document.get("geo_field");
        }
    }

    GeometryCollection createGeometryCollection(Polygon polygon_1, Polygon polygon_2) {
        final List<Geometry> geometryList = new ArrayList<>();
        geometryList.add(MongoDbDriver.convertToGeoJSON(polygon_1));
        geometryList.add(MongoDbDriver.convertToGeoJSON(polygon_2));
        return new GeometryCollection(geometryList);
    }
}
