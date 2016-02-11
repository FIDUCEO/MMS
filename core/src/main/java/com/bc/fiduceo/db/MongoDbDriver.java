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
import com.bc.fiduceo.core.Sensor;
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
import org.esa.snap.core.util.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDbDriver extends AbstractDriver {

    private static final String SATELLITE_DATA_COLLECTION = "SATELLITE_OBSERVATION";
    public static final String DATA_FILE_KEY = "dataFile";
    public static final String START_TIME_KEY = "startTime";
    public static final String STOP_TIME_KEY = "stopTime";
    public static final String NODE_TYPE_KEY = "nodeType";
    public static final String GEO_BOUNDS_KEY = "geoBounds";
    public static final String SENSOR_KEY = "sensor";
    public static final String TIME_AXIS_START_KEY = "timeAxisStartIndex";
    public static final String TIME_AXIS_END_KEY = "timeAxisEndIndex";

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
        final MongoCollection<Document> satelliteObservation = database.getCollection(SATELLITE_DATA_COLLECTION);
        satelliteObservation.drop();
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void insert(SatelliteObservation satelliteObservation) throws SQLException {
        final MongoCollection<Document> observationCollection = database.getCollection(SATELLITE_DATA_COLLECTION);

        final Document document = new Document(DATA_FILE_KEY, satelliteObservation.getDataFile().getAbsolutePath());
        document.append(START_TIME_KEY, satelliteObservation.getStartTime());
        document.append(STOP_TIME_KEY, satelliteObservation.getStopTime());
        document.append(NODE_TYPE_KEY, satelliteObservation.getNodeType().toId());
        document.append(GEO_BOUNDS_KEY, convertToGeoJSON(satelliteObservation.getGeoBounds()));
        // @todo 2 tb/tb does not work correctly when we extend the sensor class, improve here 2016-02-09
        document.append(SENSOR_KEY, new Document("name", satelliteObservation.getSensor().getName()));
        document.append(TIME_AXIS_START_KEY, satelliteObservation.getTimeAxisStartIndex());
        document.append(TIME_AXIS_END_KEY, satelliteObservation.getTimeAxisEndIndex());

        observationCollection.insertOne(document);
    }

    @Override
    public int insert(Sensor sensor) throws SQLException {
        // we use embedded storage at the moment, no need to separately ingest the sensor tb 2016-02-09
        return -1;
    }

    @Override
    public List<SatelliteObservation> get() throws SQLException {
        return get(null);
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {
        final MongoCollection<Document> observationCollection = database.getCollection(SATELLITE_DATA_COLLECTION);
        final List<SatelliteObservation> resultList = new ArrayList<>();

        final Document queryDocument = createQueryDocument(parameter);
        final FindIterable<Document> documents = observationCollection.find(queryDocument);
        for (Document document : documents) {
            final SatelliteObservation satelliteObservation = getSatelliteObservation(document);
            resultList.add(satelliteObservation);
        }
        return resultList;
    }

    private SatelliteObservation getSatelliteObservation(Document document) {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        final String dataFile = document.getString(DATA_FILE_KEY);
        satelliteObservation.setDataFile(new File(dataFile));

        final Date startTime = document.getDate(START_TIME_KEY);
        satelliteObservation.setStartTime(startTime);

        final Date stopTime = document.getDate(STOP_TIME_KEY);
        satelliteObservation.setStopTime(stopTime);

        final Integer nodeTypeId = document.getInteger(NODE_TYPE_KEY);
        satelliteObservation.setNodeType(NodeType.fromId(nodeTypeId));

        final Document geoBounds = (Document) document.get(GEO_BOUNDS_KEY);
        final Geometry geometry = convertToGeometry(geoBounds);
        satelliteObservation.setGeoBounds(geometry);

        // @todo 2 tb/tb does not work correctly when we extend the sensor class, improve here 2016-02-09
        final Document jsonSensor = (Document) document.get(SENSOR_KEY);
        final Sensor sensor = new Sensor();
        sensor.setName(jsonSensor.getString("name"));
        satelliteObservation.setSensor(sensor);

        satelliteObservation.setTimeAxisStartIndex(document.getInteger(TIME_AXIS_START_KEY));
        satelliteObservation.setTimeAxisEndIndex(document.getInteger(TIME_AXIS_END_KEY));
        return satelliteObservation;
    }

    // static access for testing only tb 2016-02-09
    // @todo 2 tb/tb write tests!! 2016-02-11
    static Document createQueryDocument(QueryParameter parameter) {
        if (parameter == null) {
            return new Document();
        }

        final Document queryConstraints = new Document();
        final Date startTime = parameter.getStartTime();
        if (startTime != null){
            queryConstraints.append(STOP_TIME_KEY, new Document("$gt", startTime));
        }

        final Date stopTime = parameter.getStopTime();
        if (stopTime != null){
            queryConstraints.append(START_TIME_KEY, new Document("$lt", stopTime));
        }

        final String sensorName = parameter.getSensorName();
        if (StringUtils.isNotNullAndNotEmpty(sensorName)) {
            queryConstraints.append(SENSOR_KEY + ".name", new Document("$eq", sensorName));
        }

        return queryConstraints;
    }

    // static access for testing only tb 2016-02-09
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

            if (!coordinates[0].equals(coordinates[coordinates.length - 1])) {
                final Position position = new Position(coordinates[0].getLon(), coordinates[0].getLat());
                polygonPoints.add(position);
            }
            return new com.mongodb.client.model.geojson.Polygon(polygonPoints);
        }

        throw new RuntimeException("Geometry type support not implemented");
    }

    // static access for testing only tb 2016-02-09
    @SuppressWarnings("unchecked")
    Geometry convertToGeometry(Document geoDocument) {
        final String type = geoDocument.getString("type");
        if ("Polygon".equals(type)) {
            final ArrayList<Point> polygonPoints = new ArrayList<>();
            final ArrayList linearRings = (ArrayList) geoDocument.get("coordinates");
            for (Object linearRing : linearRings) {
                final ArrayList coordinates = (ArrayList) linearRing;
                for (Object coordinate : coordinates) {
                    final ArrayList<Double> point = (ArrayList<Double>) coordinate;
                    final Point point1 = geometryFactory.createPoint(point.get(0), point.get(1));
                    polygonPoints.add(point1);
                }
            }

            return geometryFactory.createPolygon(polygonPoints);

        }
        throw new RuntimeException("Geometry type support not implemented yet");
    }
}
