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

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Position;
import org.apache.commons.dbcp.BasicDataSource;
import org.bson.Document;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDbDriver extends AbstractDriver {

    private MongoClient mongoClient;
    private GeometryFactory geometryFactory;
    private MongoDatabase database;

    @Override
    public String getUrlPattern() {
        return "mongodb";
    }

    @Override
    public void open(BasicDataSource dataSource) throws SQLException {
        final MongoClientURI clientURI = new MongoClientURI(dataSource.getUrl());
        mongoClient = new MongoClient(clientURI);
        database = mongoClient.getDatabase("FIDUCEO");
    }

    @Override
    public void close() throws SQLException {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    @Override
    public void initialize() throws SQLException {
        // nothing to initialize tb 2016-02-08
    }

    @Override
    public void clear() throws SQLException {
        final MongoCollection<Document> satelliteObservation = database.getCollection("SATELLITE_OBSERVATION");
        satelliteObservation.drop();
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void insert(SatelliteObservation satelliteObservation) throws SQLException {
        final MongoCollection<Document> observationCollection = database.getCollection("SATELLITE_OBSERVATION");
        final Document document = new Document("dataFile", satelliteObservation.getDataFile().getAbsolutePath());
        document.append("startTime", satelliteObservation.getStartTime());
        document.append("stopTime", satelliteObservation.getStopTime());
        document.append("nodeType", satelliteObservation.getNodeType().toId());

        observationCollection.insertOne(document);
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        final MongoCollection<Document> observationCollection = database.getCollection("SATELLITE_OBSERVATION");
        final List<SatelliteObservation> resultList = new ArrayList<>();

        final FindIterable<Document> documents = observationCollection.find();
        for (Document document : documents) {
            final SatelliteObservation satelliteObservation = new SatelliteObservation();

            final String dataFile = document.getString("dataFile");
            satelliteObservation.setDataFile(new File(dataFile));

            final Date startTime = document.getDate("startTime");
            satelliteObservation.setStartTime(startTime);

            final Date stopTime = document.getDate("stopTime");
            satelliteObservation.setStopTime(stopTime);

            final Integer nodeTypeId = document.getInteger("nodeType");
            satelliteObservation.setNodeType(NodeType.fromId(nodeTypeId));

            resultList.add(satelliteObservation);
        }

        return resultList;
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {
        throw new RuntimeException("not implemented");
    }

    @SuppressWarnings("unchecked")
    static com.mongodb.client.model.geojson.Geometry convertToGeoJSON(Geometry geometry) {
        if (geometry == null) {
            throw new IllegalArgumentException("geometry is null");
        }

        if (geometry instanceof Polygon) {
            final Point[] coordinates = geometry.getCoordinates();
            final ArrayList<Position> polygonPoints = new ArrayList<>();

            for (final Point coordinate : coordinates) {
                final Position position = new Position(coordinate.getLon(), coordinate.getLat());
                polygonPoints.add(position);
            }

            if (!coordinates[0].equals(coordinates[coordinates.length - 1]))  {
                final Position position = new Position(coordinates[0].getLon(), coordinates[0].getLat());
                polygonPoints.add(position);
            }
            return new com.mongodb.client.model.geojson.Polygon(polygonPoints);
        }

        throw new RuntimeException("Geometry type support not implemented");
    }
}
