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

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.util.TimeUtils;
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
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
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
        assertEquals(polygonCoordinatesList.size(), 2);

        assertEquals(polygonCoordinatesList.get(0).getExterior().size(), 5);
        assertEquals(polygonCoordinatesList.get(1).getExterior().size(), 4);

        assertEquals(polygonCoordinatesList.get(0).getExterior().get(0).toString(), "Position{values=[20.0, 0.0]}");
        assertEquals(polygonCoordinatesList.get(0).getExterior().get(1).toString(), "Position{values=[49.99999999999999, 0.0]}");

        assertEquals(polygonCoordinatesList.get(1).getExterior().get(0).toString(), "Position{values=[20.0, 70.0]}");
        assertEquals(polygonCoordinatesList.get(1).getExterior().get(1).toString(), "Position{values=[49.99999999999999, 70.0]}");
    }

    @Test
    public void testConvertToGeoJSON_GeometryCollection() {
        final Geometry polygonGeometry = geometryFactory.parse("POLYGON((-8 -2, -8 -1, -6 -1, -6 -2, -8 -2))");
        final MultiPolygon multiPolygon = getMultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90)))");

        final Geometry[] geometries = new Geometry[2];
        geometries[0] = polygonGeometry;
        geometries[1] = multiPolygon;

        GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(geometries);
        com.mongodb.client.model.geojson.Geometry mongoGeometry = MongoDbDriver.convertToGeoJSON(geometryCollection);
        assertNotNull(mongoGeometry);

        List<? extends com.mongodb.client.model.geojson.Geometry> convertedGeometries = ((com.mongodb.client.model.geojson.GeometryCollection) mongoGeometry).getGeometries();


        assertEquals(2, convertedGeometries.size());
        assertEquals("Polygon{exterior=[Position{values=[-6.0, -2.0]}, Position{values=[-6.0, -1.0]}, Position{values=[-7.999999999999998, -1.0]}, Position{values=[-7.999999999999998, -1.9999999999999996]}, Position{values=[-6.0, -2.0]}]}",
                convertedGeometries.get(0).toString());

        assertEquals("MultiPolygon{coordinates=[PolygonCoordinates{exterior=[Position{values=[20.0, 0.0]}, " +
                "Position{values=[49.99999999999999, 0.0]}, Position{values=[50.0, 20.0]}, " +
                "Position{values=[20.0, 49.99999999999999]}, Position{values=[20.0, 0.0]}]}, " +
                "PolygonCoordinates{exterior=[Position{values=[20.0, 70.0]}, " +
                "Position{values=[49.99999999999999, 70.0]}, " +
                "Position{values=[49.99999999999999, 90.0]}, " +
                "Position{values=[20.0, 70.0]}]}]}", convertedGeometries.get(1).toString());
    }

    @Test
    public void testConvertToGeoJSON_GeometryCollection_oneEntry() {
        final Geometry polygonGeometry = geometryFactory.parse("POLYGON((-6 -2, -6 -1, -4 -1, -4 -2, -6 -2))");
        final Geometry[] geometries = new Geometry[1];
        geometries[0] = polygonGeometry;

        GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(geometries);
        com.mongodb.client.model.geojson.Geometry mongoGeometry = MongoDbDriver.convertToGeoJSON(geometryCollection);
        assertNotNull(mongoGeometry);
        assertTrue(mongoGeometry instanceof com.mongodb.client.model.geojson.Polygon);
        assertEquals("{ \"type\" : \"Polygon\", \"coordinates\" : [[[-4.0, -2.0], [-4.000000000000001, -1.0], [-6.0, -1.0], [-6.0, -2.0], [-4.0, -2.0]]] }",
                mongoGeometry.toJson());
    }

    @Test
    public void testConvertToGeometry_polygon() {
        final double[] lons = {-12.0, -12.0, -11.0, -11.0, -12.0};
        final double[] lats = {8.0, 9.0, 9.0, 8.0, 8.0};
        final Document jsonPolygon = createGeoJsonPolygon(lons, lats);

        final Geometry geometry = driver.convertToGeometry(jsonPolygon);
        assertNotNull(geometry);
        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(7.999999999999998, -12.000000000000002)\n" +
                "(9.0, -12.0)\n" +
                "(9.0, -10.999999999999998)\n" +
                "(7.999999999999998, -10.999999999999998)\n" +
                ">\n", geometry.toString());
    }

    @Test
    public void testConvertToGeometry_multiPolygon() {
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
        final Geometry geometry = driver.convertToGeometry(jsonMultiPolygon);
        assertNotNull(geometry);
        assertEquals(9, geometry.getCoordinates().length);
        MultiPolygon multiPolygon = (MultiPolygon) geometry;
        List<Polygon> polygonsList = multiPolygon.getPolygons();

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
    public void testConvertToGeometry_geometryCollection() {
        final double[] lons_1 = {-12.0, -12.0, -11.0, -11.0, -12.0};
        final double[] lats_1 = {8.0, 9.0, 9.0, 8.0, 8.0};
        final Document jsonPolygon_1 = createGeoJsonPolygon(lons_1, lats_1);

        final double[] lons_2 = {6.0, 7.0, 7.0, 6.0, 6.0};
        final double[] lats_2 = {1.0, 1.0, 2.0, 2.0, 1.0};
        final Document jsonPolygon_2 = createGeoJsonPolygon(lons_2, lats_2);

        final Document jsonGeometryCollection = new Document("type", "GeometryCollection");
        final List<Document> geometryList = new ArrayList<>();
        geometryList.add(jsonPolygon_1);
        geometryList.add(jsonPolygon_2);
        jsonGeometryCollection.append("geometries", geometryList);

        final Geometry geometry = driver.convertToGeometry(jsonGeometryCollection);
        assertTrue(geometry instanceof GeometryCollection);
        final GeometryCollection geometryCollection = (GeometryCollection) geometry;
        final Geometry[] geometries = geometryCollection.getGeometries();
        assertEquals(2, geometries.length);
        assertEquals("POLYGON((-12.000000000000002 7.999999999999998,-12.0 9.0,-10.999999999999998 9.0,-10.999999999999998 7.999999999999998,-12.000000000000002 7.999999999999998))",
                geometryFactory.format(geometries[0]));
        assertEquals("POLYGON((6.0 1.0,6.999999999999999 1.0,6.999999999999999 2.0,6.0 2.0,6.0 1.0))",
                geometryFactory.format(geometries[1]));
    }

    @Test
    public void testConvertToGeometry_lineString() {
        final double[] lons = {-12.0, -11.0, -10.0};
        final double[] lats = {8.0, 9.0, 10.0};

        final Document jsonLineString = createGeoJsonLineString(lons, lats);

        final Geometry geometry = driver.convertToGeometry(jsonLineString);
        assertTrue(geometry instanceof LineString);
        assertEquals("LINESTRING(-12.000000000000002 7.999999999999998,-10.999999999999998 9.0,-9.999999999999998 10.0)", geometryFactory.format(geometry));
    }

    @Test
    public void testConvertToGeometry_unsupportedGeometry() {
        final Document bretzelType = new Document("type", "Bretzel");

        try {
            driver.convertToGeometry(bretzelType);
            fail("RuntimeException expecetd");
        } catch (RuntimeException expecetd) {
        }
    }

    @Test
    public void testGetPolygonCoordinates() {
        MultiPolygon multiPolygon = getMultiPolygon("MULTIPOLYGON (((20 0, 50 0, 50 20, 20 50)),((20 70, 50 70, 50 90)))");
        List<PolygonCoordinates> polygonCoordinates = MongoDbDriver.gePolygonCoordinates(multiPolygon);
        assertNotNull(polygonCoordinates);

        assertEquals(polygonCoordinates.size(), 2);

        assertEquals(polygonCoordinates.get(0).getExterior().size(), 5);
        assertEquals(polygonCoordinates.get(1).getExterior().size(), 4);

        assertEquals(polygonCoordinates.get(0).getExterior().get(0).toString(), "Position{values=[20.0, 0.0]}");
        assertEquals(polygonCoordinates.get(0).getExterior().get(1).toString(), "Position{values=[49.99999999999999, 0.0]}");

        assertEquals(polygonCoordinates.get(1).getExterior().get(0).toString(), "Position{values=[20.0, 70.0]}");
        assertEquals(polygonCoordinates.get(1).getExterior().get(1).toString(), "Position{values=[49.99999999999999, 70.0]}");
    }

    @Test
    public void testCreateQueryDocument_NullDoc() {
        Document queryDocument = MongoDbDriver.createQueryDocument(null);
        assertNotNull(queryDocument);
    }

    @Test
    public void testConvertToDocument_noTimeAxes() {
        final TimeAxis[] timeAxes = new TimeAxis[0];

        final Document document = MongoDbDriver.convertToDocument(timeAxes);
        assertNotNull(document);

        final ArrayList<Document> timeAxesDocument = (ArrayList<Document>) document.get("timeAxes");
        assertEquals(0, timeAxesDocument.size());
    }

    @Test
    public void testConvertToDocument_oneTimeAxis() {
        final LineString lineString = (LineString) geometryFactory.parse("LINESTRING(0 1, 1 2, 2 3)");
        final Date startDate = TimeUtils.parseDOYBeginOfDay("2015-012");
        final Date endDate = TimeUtils.parseDOYEndOfDay("2015-013");
        final TimeAxis timeAxis = geometryFactory.createTimeAxis(lineString, startDate, endDate);

        final Document document = MongoDbDriver.convertToDocument(new TimeAxis[]{timeAxis});
        assertNotNull(document);

        final ArrayList<Document> timeAxesDocument = (ArrayList<Document>) document.get("timeAxes");
        assertEquals(1, timeAxesDocument.size());

        final Document axis_one = timeAxesDocument.get(0);
        final Date startTime = axis_one.getDate("startTime");
        TestUtil.assertCorrectUTCDate(2015, 1, 12, 0, 0, 0, 0, startTime);

        final Date endTime = axis_one.getDate("endTime");
        TestUtil.assertCorrectUTCDate(2015, 1, 13, 23, 59, 59, 999, endTime);

        final com.mongodb.client.model.geojson.LineString geometry = (com.mongodb.client.model.geojson.LineString) axis_one.get("geometry");
        assertNotNull(geometry);
        final List<Position> coordinates = geometry.getCoordinates();
        assertEquals(3, coordinates.size());
        final List<Double> coordinateValues = coordinates.get(1).getValues();
        assertEquals(1, coordinateValues.get(0), 1e-8);
        assertEquals(2, coordinateValues.get(1), 1e-8);
    }

    @Test
    public void testCreateQueryDocument() {
        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName("amsub_n15");
        queryParameter.setStartTime(TimeUtils.parseDOYBeginOfDay("2015-300"));
        queryParameter.setStopTime(TimeUtils.parseDOYBeginOfDay("2015-302"));
        queryParameter.setVersion("ver2.8");
        queryParameter.setPath("/some/where/over/the/rainbow");

        final Document queryDocument = MongoDbDriver.createQueryDocument(queryParameter);
        assertNotNull(queryDocument);

        final Document startTimeDoc = (Document) queryDocument.get("startTime");
        final Date startTime = startTimeDoc.getDate("$lt");
        assertEquals(queryParameter.getStopTime(), startTime);

        final Document stopTimeDoc = (Document) queryDocument.get("stopTime");
        final Date stopTime = stopTimeDoc.getDate("$gt");
        assertEquals(queryParameter.getStartTime(), stopTime);

        final Document sensorDoc = (Document) queryDocument.get("sensor.name");
        final String sensorType = sensorDoc.getString("$eq");
        assertEquals(queryParameter.getSensorName(), sensorType);

        final Document versionDoc = (Document) queryDocument.get("version");
        final String version = versionDoc.getString("$eq");
        assertEquals("ver2.8", version);

        final Document dataFileDoc = (Document) queryDocument.get("dataFile");
        final String dataFile = dataFileDoc.getString("$eq");
        assertEquals("/some/where/over/the/rainbow", dataFile);
    }

    @Test
    public void testParseAddress() {
        assertEquals("localhost", MongoDbDriver.parseAddress("mongodb://localhost:33456/nasenmann"));
        assertEquals("192.29.25.134", MongoDbDriver.parseAddress("mongodb://192.29.25.134:33456/nasenmann"));
    }

    @Test
    public void testParsePort() {
        assertEquals("33456", MongoDbDriver.parsePort("mongodb://localhost:33456/nasenmann"));
        assertEquals("19876", MongoDbDriver.parsePort("mongodb://192.29.25.134:19876/nasenmann"));

        assertEquals("2647", MongoDbDriver.parsePort("mongodb://192.29.25.134:2647"));
    }

    private MultiPolygon getMultiPolygon(String wkt) {
        return (MultiPolygon) geometryFactory.parse(wkt);
    }

    private Document createGeoJsonPolygon(double[] lons, double[] lats) {
        final Document jsonPolygon = new Document("type", "Polygon");
        final ArrayList<ArrayList<ArrayList<Double>>> linearRings = new ArrayList<>();
        final ArrayList<ArrayList<Double>> pointList = new ArrayList<>();

        for (int i = 0; i < lons.length; i++) {
            pointList.add(createJsonPoint(lons[i], lats[i]));
        }

        linearRings.add(pointList);
        jsonPolygon.append("coordinates", linearRings);
        return jsonPolygon;
    }

    private Document createGeoJsonLineString(double[] lons, double[] lats) {
        final Document jsonLineString = new Document("type", "LineString");
        final ArrayList<ArrayList<Double>> pointList = new ArrayList<>();

        for (int i = 0; i < lons.length; i++) {
            pointList.add(createJsonPoint(lons[i], lats[i]));
        }

        jsonLineString.append("coordinates", pointList);
        return jsonLineString;
    }

    private ArrayList<Double> createJsonPoint(double lon, double lat) {
        final ArrayList<Double> jsonPoint = new ArrayList<>();
        jsonPoint.add(lon);
        jsonPoint.add(lat);
        return jsonPoint;
    }
}
