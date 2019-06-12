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
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.geometry.TimeAxis;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongodb.client.model.geojson.Position;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bson.Document;
import org.esa.snap.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class MongoDbDriver extends AbstractDriver {

    private static final String DATA_FILE_KEY = "dataFile";
    private static final String START_TIME_KEY = "startTime";
    private static final String STOP_TIME_KEY = "stopTime";
    private static final String NODE_TYPE_KEY = "nodeType";
    private static final String GEO_BOUNDS_KEY = "geoBounds";
    private static final String SENSOR_KEY = "sensor";
    private static final String SATELLITE_DATA_COLLECTION = "SATELLITE_OBSERVATION";
    private static final String TIME_AXES_KEY = "timeAxes";
    private static final String VERSION_KEY = "version";
    private static final String DATABASE_NAME = "FIDUCEO";

    private MongoClient mongoClient;
    private GeometryFactory geometryFactory;
    private MongoDatabase database;

    @Override
    public String getUrlPattern() {
        return "mongodb";
    }

    @Override
    public void open(BasicDataSource dataSource) {
        final String address = parseAddress(dataSource.getUrl());
        final String port = parsePort(dataSource.getUrl());
        final ServerAddress serverAddress = new ServerAddress(address, Integer.parseInt(port));

        final MongoClientOptions clientOptions = MongoClientOptions.builder().
                connectTimeout(120000).
                socketTimeout(120000).
                serverSelectionTimeout(120000).build();

        final String username = dataSource.getUsername();
        final String password = dataSource.getPassword();
        if (StringUtils.isNotNullAndNotEmpty(password) && StringUtils.isNotNullAndNotEmpty(username)) {
            final MongoCredential credential = MongoCredential.createCredential(username, DATABASE_NAME, password.toCharArray());
            final List<MongoCredential> credentialsList = new ArrayList<>();
            credentialsList.add(credential);
            mongoClient = new MongoClient(serverAddress, credentialsList, clientOptions);
        } else {
            mongoClient = new MongoClient(serverAddress, clientOptions);
        }
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    @Override
    public void close() {
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
    public void initialize() {
        final MongoCollection<Document> satelliteObservations = database.getCollection(SATELLITE_DATA_COLLECTION);
        satelliteObservations.createIndex(new BasicDBObject(START_TIME_KEY, 1));
        satelliteObservations.createIndex(new BasicDBObject(STOP_TIME_KEY, 1));
        satelliteObservations.createIndex(new BasicDBObject(SENSOR_KEY + ".name", 1));
    }

    @Override
    public void clear() {
        final MongoCollection<Document> satelliteObservation = database.getCollection(SATELLITE_DATA_COLLECTION);
        satelliteObservation.drop();
    }

    @Override
    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void insert(SatelliteObservation satelliteObservation) {
        final MongoCollection<Document> observationCollection = database.getCollection(SATELLITE_DATA_COLLECTION);

        final Document document = new Document(DATA_FILE_KEY, satelliteObservation.getDataFilePath().toString());
        document.append(START_TIME_KEY, satelliteObservation.getStartTime());
        document.append(STOP_TIME_KEY, satelliteObservation.getStopTime());
        document.append(NODE_TYPE_KEY, satelliteObservation.getNodeType().toId());

        final Geometry geoBounds = satelliteObservation.getGeoBounds();
        if (geoBounds != null) {
            document.append(GEO_BOUNDS_KEY, convertToGeoJSON(geoBounds));
        }

        // @todo 2 tb/tb does not work correctly when we extend the sensor class, improve here 2016-02-09
        document.append(SENSOR_KEY, new Document("name", satelliteObservation.getSensor().getName()));

        final TimeAxis[] timeAxes = satelliteObservation.getTimeAxes();
        if (timeAxes != null) {
            document.append(TIME_AXES_KEY, convertToDocument(timeAxes));
        }

        document.append(VERSION_KEY, satelliteObservation.getVersion());

        observationCollection.insertOne(document);
    }

    @Override
    public void updatePath(SatelliteObservation satelliteObservation, String newPath) {
        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setStartTime(satelliteObservation.getStartTime());
        queryParameter.setStopTime(satelliteObservation.getStopTime());
        queryParameter.setSensorName(satelliteObservation.getSensor().getName());
        queryParameter.setVersion(satelliteObservation.getVersion());
        queryParameter.setPath(satelliteObservation.getDataFilePath().toString());

        final Document queryDocument = createQueryDocument(queryParameter);

        final MongoCollection<Document> observationCollection = database.getCollection(SATELLITE_DATA_COLLECTION);
        observationCollection.updateOne(queryDocument, new Document("$set", new Document(DATA_FILE_KEY, newPath)));
    }

    @Override
    public int insert(Sensor sensor) {
        // we use embedded storage at the moment, no need to separately ingest the sensor tb 2016-02-09
        return -1;
    }

    @Override
    public List<SatelliteObservation> get() {
        return get(null);
    }

    @Override
    public List<SatelliteObservation> get(QueryParameter parameter) {
        final MongoCollection<Document> observationCollection = database.getCollection(SATELLITE_DATA_COLLECTION);
        final List<SatelliteObservation> resultList = new ArrayList<>();

        final Document queryDocument = createQueryDocument(parameter);
        int offset = 0;
        int pageSize = -1;
        if (parameter != null) {
            offset = parameter.getOffset();
            pageSize = parameter.getPageSize();
        }
        FindIterable<Document> documents;
        if (pageSize >= 0) {
            documents = observationCollection.find(queryDocument).skip(offset).limit(pageSize);
        } else {
            documents = observationCollection.find(queryDocument).skip(offset);
        }
        for (Document document : documents) {
            final SatelliteObservation satelliteObservation = getSatelliteObservation(document);
            resultList.add(satelliteObservation);
        }
        return resultList;
    }

    private SatelliteObservation getSatelliteObservation(Document document) {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();

        final String dataFile = document.getString(DATA_FILE_KEY);
        satelliteObservation.setDataFilePath(dataFile);

        final String version = document.getString(VERSION_KEY);
        satelliteObservation.setVersion(version);

        final Date startTime = document.getDate(START_TIME_KEY);
        satelliteObservation.setStartTime(startTime);

        final Date stopTime = document.getDate(STOP_TIME_KEY);
        satelliteObservation.setStopTime(stopTime);

        final Integer nodeTypeId = document.getInteger(NODE_TYPE_KEY);
        satelliteObservation.setNodeType(NodeType.fromId(nodeTypeId));

        final Document geoBounds = (Document) document.get(GEO_BOUNDS_KEY);
        if (geoBounds != null) {
            final Geometry geometry = convertToGeometry(geoBounds);
            satelliteObservation.setGeoBounds(geometry);
        }

        // @todo 2 tb/tb does not work correctly when we extend the sensor class, improve here 2016-02-09
        final Document jsonSensor = (Document) document.get(SENSOR_KEY);
        final Sensor sensor = new Sensor(jsonSensor.getString("name"));
        satelliteObservation.setSensor(sensor);

        final Document jsonTimeAxes = (Document) document.get(TIME_AXES_KEY);
        if (jsonTimeAxes != null) {
            final TimeAxis[] timeAxes = convertToTimeAxes(jsonTimeAxes);
            satelliteObservation.setTimeAxes(timeAxes);
        }

        return satelliteObservation;
    }

    // static access for testing only tb 2016-02-09
    Geometry convertToGeometry(Document geoDocument) {
        final String type = geoDocument.getString("type");
        if ("MultiPolygon".equals(type)) {
            return convertMultiPolygon(geoDocument);
        } else if ("Polygon".equals(type)) {
            return convertPolygon(geoDocument);
        } else if ("GeometryCollection".equals(type)) {
            return convertGeometryCollection(geoDocument);
        } else if ("LineString".equals(type)) {
            return convertLineString(geoDocument);
        }
        throw new RuntimeException("Geometry type support not implemented yet: " + type);
    }

    private Geometry convertGeometryCollection(Document geoDocument) {
        final List<Document> geometryList = (List<Document>) geoDocument.get("geometries");
        final List<Geometry> convertedGeometriesList = new ArrayList<>();
        for (final Document geoListDocument : geometryList) {
            convertedGeometriesList.add(convertToGeometry(geoListDocument));
        }
        return geometryFactory.createGeometryCollection(convertedGeometriesList.toArray(new Geometry[0]));
    }

    private Geometry convertMultiPolygon(Document geoDocument) {
        List<Polygon> polygonList = new ArrayList<>();
        ArrayList polycoordinates = (ArrayList) geoDocument.get("coordinates");
        for (Object polycoordinate : polycoordinates) {
            final ArrayList coordinates = (ArrayList) polycoordinate;
            for (Object coordinate : coordinates) {
                final ArrayList<Double> point = (ArrayList<Double>) coordinate;
                List<Point> pointList = new ArrayList<>();
                for (Object object : point) {
                    ArrayList<Double> m = (ArrayList<Double>) object;
                    pointList.add(geometryFactory.createPoint(m.get(0), m.get(1)));
                }
                polygonList.add(geometryFactory.createPolygon(pointList));
            }
        }
        return geometryFactory.createMultiPolygon(polygonList);
    }

    private Geometry convertPolygon(Document geoDocument) {
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

    private Geometry convertLineString(Document geoDocument) {
        final ArrayList<Point> lineStringPoints = new ArrayList<>();
        final List<ArrayList<Double>> coordinates = (List<ArrayList<Double>>) geoDocument.get("coordinates");
        for (ArrayList<Double> point : coordinates) {
            final Point point1 = geometryFactory.createPoint(point.get(0), point.get(1));
            lineStringPoints.add(point1);
        }
        return geometryFactory.createLineString(lineStringPoints);
    }

    // @todo 2 tb/** make static and add tests 2016-04-21
    TimeAxis[] convertToTimeAxes(Document jsonTimeAxes) {
        final List<Document> timeAxesDocuments = (List<Document>) jsonTimeAxes.get("timeAxes");
        final TimeAxis[] timeAxes = new TimeAxis[timeAxesDocuments.size()];
        for (int i = 0; i < timeAxesDocuments.size(); i++) {
            final Document timeAxisDocument = timeAxesDocuments.get(i);
            final Date startTime = timeAxisDocument.getDate("startTime");
            final Date endTime = timeAxisDocument.getDate("endTime");
            final LineString geometry = (LineString) convertToGeometry((Document) timeAxisDocument.get("geometry"));
            timeAxes[i] = geometryFactory.createTimeAxis(geometry, startTime, endTime);
        }
        return timeAxes;
    }

    // package access for testing only tb 2016-04-20
    @SuppressWarnings("unchecked")
    static List<PolygonCoordinates> gePolygonCoordinates(MultiPolygon multiPolygon) {
        List<Polygon> polygonList = multiPolygon.getPolygons();
        List<PolygonCoordinates> polygonCoordinatesList = new ArrayList<>();
        for (Polygon polygon : polygonList) {
            ArrayList<Position> positions = extractPointsFromGeometry(polygon.getCoordinates());

            if (!positions.get(0).equals(positions.get(positions.size() - 1))) {
                positions.add(positions.get(0));
            }
            polygonCoordinatesList.add(new PolygonCoordinates(positions));
        }
        return polygonCoordinatesList;
    }

    // package access for testing only tb 2016-02-09
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

        final String version = parameter.getVersion();
        if (StringUtils.isNotNullAndNotEmpty(version)) {
            queryConstraints.append(VERSION_KEY, new Document("$eq", version));
        }

        final String path = parameter.getPath();
        if (StringUtils.isNotNullAndNotEmpty(path)) {
            queryConstraints.append(DATA_FILE_KEY, new Document("$eq", path));
        }

        return queryConstraints;
    }

    // static access for testing only tb 2016-02-09
    @SuppressWarnings("unchecked")
    static com.mongodb.client.model.geojson.Geometry convertToGeoJSON(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            return convertGeometryCollectionToGeoJSON((GeometryCollection) geometry);
        }

        final Point[] coordinates = geometry.getCoordinates();
        final ArrayList<Position> geometryPoints = extractPointsFromGeometry(coordinates);
        if (geometry instanceof MultiPolygon) {
            List<PolygonCoordinates> polygonCoordinates = gePolygonCoordinates((MultiPolygon) geometry);
            return new com.mongodb.client.model.geojson.MultiPolygon(polygonCoordinates);
        } else if (geometry instanceof Polygon) {
            if (!coordinates[0].equals(coordinates[coordinates.length - 1])) {
                final Position position = new Position(coordinates[0].getLon(), coordinates[0].getLat());
                geometryPoints.add(position);
            }
            return new com.mongodb.client.model.geojson.Polygon(geometryPoints);
        } else if (geometry instanceof LineString) {
            return new com.mongodb.client.model.geojson.LineString(geometryPoints);
        } else if (geometry instanceof Point) {
            return new com.mongodb.client.model.geojson.Point(geometryPoints.get(0));
        }

        throw new RuntimeException("Geometry type support not implemented");
    }

    // package access for testing only tb 2016-03-04
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    static Document convertToDocument(TimeAxis[] timeAxes) {
        final List<Document> timeAxesList = new ArrayList<>();
        for (final TimeAxis axis : timeAxes) {
            final Document axisDocument = new Document("startTime", axis.getStartTime());
            axisDocument.append("endTime", axis.getEndTime());
            axisDocument.append("geometry", convertToGeoJSON(axis.getGeometry()));
            timeAxesList.add(axisDocument);
        }
        return new Document("timeAxes", timeAxesList);
    }

    private static com.mongodb.client.model.geojson.Geometry convertGeometryCollectionToGeoJSON(GeometryCollection geometryCollection) {
        final Geometry[] geometries = geometryCollection.getGeometries();
        if (geometries.length == 1) {
            return convertToGeoJSON(geometries[0]);
        }

        final List<com.mongodb.client.model.geojson.Geometry> geometryList = new ArrayList<>();
        for (final Geometry geometry : geometries) {
            geometryList.add(convertToGeoJSON(geometry));
        }
        return new com.mongodb.client.model.geojson.GeometryCollection(geometryList);
    }

    private static ArrayList<Position> extractPointsFromGeometry(Point[] coordinates) {
        final ArrayList<Position> polygonPoints = new ArrayList<>();

        for (final Point coordinate : coordinates) {
            final Position position = new Position(coordinate.getLon(), coordinate.getLat());
            polygonPoints.add(position);
        }
        return polygonPoints;
    }

    // package access for testing only tb 2016-04-21
    static String parseAddress(String databaseUrl) {
        final int slashIndex = databaseUrl.indexOf("//");
        final int colonIndex = databaseUrl.indexOf(":", slashIndex);
        return databaseUrl.substring(slashIndex + 2, colonIndex);
    }

    // package access for testing only tb 2016-04-21
    static String parsePort(String databaseUrl) {
        int slashIndex = databaseUrl.indexOf("//");
        final int colonIndex = databaseUrl.indexOf(":", slashIndex);
        slashIndex = databaseUrl.indexOf("/", colonIndex);
        if (slashIndex > 0) {
            return databaseUrl.substring(colonIndex + 1, slashIndex);
        } else {
            return databaseUrl.substring(colonIndex + 1);
        }
    }
}
