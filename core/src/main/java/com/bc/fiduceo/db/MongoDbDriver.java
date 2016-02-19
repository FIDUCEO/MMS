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
import com.bc.fiduceo.geometry.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;
import com.mongodb.client.model.geojson.PolygonCoordinates;
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

    private static final String DATA_FILE_KEY = "dataFile";
    private static final String START_TIME_KEY = "startTime";
    private static final String STOP_TIME_KEY = "stopTime";
    private static final String NODE_TYPE_KEY = "nodeType";
    private static final String GEO_BOUNDS_KEY = "geoBounds";
    private static final String SENSOR_KEY = "sensor";
    private static final String TIME_AXIS_START_KEY = "timeAxisStartIndex";
    private static final String TIME_AXIS_END_KEY = "timeAxisEndIndex";
    private static final String SATELLITE_DATA_COLLECTION = "SATELLITE_OBSERVATION";

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
    public boolean isInitialized() {
        final MongoIterable<String> collectionNames = database.listCollectionNames();
        for (String collectionName : collectionNames) {
            if (SATELLITE_DATA_COLLECTION.equals(collectionName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void initialize() throws SQLException {
        final MongoCollection<Document> satelliteObservations = database.getCollection(SATELLITE_DATA_COLLECTION);
        satelliteObservations.createIndex(new BasicDBObject(GEO_BOUNDS_KEY, "2dsphere"));
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
    //todo: write a test to test multipolygon conversion.
    @SuppressWarnings("unchecked")
    Geometry convertToGeometry(Document geoDocument) {
        final String type = geoDocument.getString("type");
        final ArrayList coordinatesList = (ArrayList) geoDocument.get("coordinates");
        if ("Polygon".equals(type)) {
            final ArrayList<Point> polygonPoints = new ArrayList<>();
            for (Object linearRing : coordinatesList) {
                final ArrayList coordinates = (ArrayList) linearRing;
                for (Object coordinate : coordinates) {
                    final ArrayList<Double> point = (ArrayList<Double>) coordinate;
                    final Point point1 = geometryFactory.createPoint(point.get(0), point.get(1));
                    polygonPoints.add(point1);
                }
            }
            return geometryFactory.createPolygon(polygonPoints);
        } else if ("MultiPolygon".equals(type)) {
            List<Polygon> polygonList = new ArrayList<>();
            for (int i = 0; i < coordinatesList.size(); i++) {
                final List<Point> pointList = new ArrayList<>();
                final ArrayList coordinates = (ArrayList) coordinatesList.get(i);
                for (Object coordinate : coordinates) {
                    final ArrayList<Double> point = (ArrayList<Double>) coordinate;
                    for (Object object : point) {
                        ArrayList<Double> m = (ArrayList<Double>) object;
                        pointList.add(geometryFactory.createPoint(m.get(0), m.get(1)));
                    }
                }
                polygonList.add(geometryFactory.createPolygon(pointList));
            }
            return geometryFactory.createMultiPolygon(polygonList);
        }
        throw new RuntimeException("Geometry type support not implemented yet");
    }

    private static List<PolygonCoordinates> gePolygonCoordinates(MultiPolygon multiPolygon) {
        List<Polygon> s2PolygonList = (List<Polygon>) multiPolygon.getInner();
        List<PolygonCoordinates> polygonCoordinatesList = new ArrayList<>();
        for (Polygon s2Polygon : s2PolygonList) {
            ArrayList<Position> positions = extractPointsFromGeometry(s2Polygon.getCoordinates());

            if (!positions.get(0).equals(positions.get(positions.size() - 1))) {
                positions.add(positions.get(0));
            }
            polygonCoordinatesList.add(new PolygonCoordinates(positions));
        }
        return polygonCoordinatesList;
    }

    // static access for testing only tb 2016-02-09
    // @todo 2 tb/tb write tests!! 2016-02-11
    static Document createQueryDocument(QueryParameter parameter) {
        if (parameter == null) {
            return new Document();
        }

        final Document queryConstraints = new Document();
        final Date startTime = parameter.getStartTime();
        if (startTime != null) {
            queryConstraints.append(STOP_TIME_KEY, new Document("$gt", startTime));
        }

        final Date stopTime = parameter.getStopTime();
        if (stopTime != null) {
            queryConstraints.append(START_TIME_KEY, new Document("$lt", stopTime));
        }

        final String sensorName = parameter.getSensorName();
        if (StringUtils.isNotNullAndNotEmpty(sensorName)) {
            queryConstraints.append(SENSOR_KEY + ".name", new Document("$eq", sensorName));
        }

        final Geometry geometry = parameter.getGeometry();
        if (geometry != null) {
            queryConstraints.append(GEO_BOUNDS_KEY, new Document("$geoIntersects",
                    new Document("$geometry", convertToGeoJSON(geometry))));
        }

        return queryConstraints;
    }

    // static access for testing only tb 2016-02-09
    @SuppressWarnings("unchecked")
    static com.mongodb.client.model.geojson.Geometry convertToGeoJSON(Geometry geometry) {
        if (geometry == null) {
            throw new IllegalArgumentException("geometry is null");
        }

        final Point[] coordinates = geometry.getCoordinates();
        final ArrayList<Position> geometryPoints = extractPointsFromGeometry(coordinates);
        if (geometry instanceof Polygon) {
            if (!coordinates[0].equals(coordinates[coordinates.length - 1])) {
                final Position position = new Position(coordinates[0].getLon(), coordinates[0].getLat());
                geometryPoints.add(position);
            }
            return new com.mongodb.client.model.geojson.Polygon(geometryPoints);
        } else if (geometry instanceof LineString) {
            return new com.mongodb.client.model.geojson.LineString(geometryPoints);
        } else if (geometry instanceof Point) {
            return new com.mongodb.client.model.geojson.Point(geometryPoints.get(0));
        } else if (geometry instanceof MultiPolygon) {
            List<PolygonCoordinates> polygonCoordinates = gePolygonCoordinates((MultiPolygon) geometry);
            return new com.mongodb.client.model.geojson.MultiPolygon(polygonCoordinates);
        }

        throw new RuntimeException("Geometry type support not implemented");
    }

    private static ArrayList<Position> extractPointsFromGeometry(Point[] coordinates) {
        final ArrayList<Position> polygonPoints = new ArrayList<>();


        for (final Point coordinate : coordinates) {
            final Position position = new Position(coordinate.getLon(), coordinate.getLat());
            polygonPoints.add(position);
        }
        return polygonPoints;
    }
}
