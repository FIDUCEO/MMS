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
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(DatabaseTestRunner.class)
public class StorageTest_SatelliteObservation_MongoDB extends StorageTest_SatelliteObservation {

    // This test will use a local database implementation. Please make sure that you have a running MongoDb database server
    // version 3.2 or higher. The test assumes an empty schema "test" and uses the connection credentials stored
    // in the datasource description below. tb 2016-02-08

    public StorageTest_SatelliteObservation_MongoDB() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("mongodb");
        dataSource.setUrl("mongodb://localhost:27017/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
    }

    @Before
    public void setUp() throws SQLException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();
    }

    @Test
    public void testMultiGeometries_searchByIntersection_noIntersection() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("LINESTRING(-8 5, -7 5, -6 4.7)");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testMultiGeometries_searchByIntersection_intersectsFirst() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("POLYGON((-1 0.5, 1 0.5, 1 0.7, -1 0.7, -1 0.5))");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(1, satelliteObservations.size());
        // @todo 1 tb/tb check geometry entry 2016-03-01
    }

    @Test
    public void testMultiGeometries_searchByIntersection_intersectsSecond() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("POLYGON((2.5 0.5, 3.5 0.5, 3.5 0.7, 2.5 0.7, 2.5 0.5))");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(1, satelliteObservations.size());
        // @todo 1 tb/tb check geometry entry 2016-03-01
    }

    @Test
    public void testMultiGeometries_searchByIntersection_intersectsBoth() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("POLYGON((1 0, 4 0, 4 1, 1 1, 1 0))");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(1, satelliteObservations.size());
        // @todo 1 tb/tb check geometry entry 2016-03-01
    }

    @Test
    public void testMultiGeometries_overlappingGeometries_searchByIntersection_intersectsFirst() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 4 0, 4 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("POLYGON((-1 0.5, 1 0.5, 1 0.7, -1 0.7, -1 0.5))");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(1, satelliteObservations.size());
        // @todo 1 tb/tb check geometry entry 2016-03-01
    }

    @Test
    public void testMultiGeometries_overlappingGeometries_searchByIntersection_intersectsSecond() throws ParseException, SQLException {
        final Geometry geometry_1 = geometryFactory.parse("POLYGON ((0 0, 4 0, 4 1, 0 1, 0 0))");
        final Geometry geometry_2 = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final SatelliteObservation observation = createMultiGeometrySatelliteObservation(geometry_1, geometry_2);
        storage.insert(observation);

        final QueryParameter parameter = createGeoQueryParameter("POLYGON((4.5 0.5, 5.5 0.5, 5.5 0.7, 4.5 0.7, 4.5 0.5))");

        final List<SatelliteObservation> satelliteObservations = storage.get(parameter);
        assertEquals(1, satelliteObservations.size());
        // @todo 1 tb/tb check geometry entry 2016-03-01
    }

    QueryParameter createGeoQueryParameter(String wkt) {
        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse(wkt);
        parameter.setGeometry(geometry);
        return parameter;
    }

    private SatelliteObservation createMultiGeometrySatelliteObservation(Geometry geometry_1, Geometry geometry_2) throws ParseException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date());
        observation.setStopTime(new Date());
        observation.setNodeType(NodeType.ASCENDING);

        observation.setGeoBounds(new Geometry[]{geometry_1, geometry_2});
        observation.setDataFilePath("the_data.file");
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);

        final Sensor sensor = new Sensor();

        sensor.setName("a_sensor");
        observation.setSensor(sensor);

        return observation;
    }
}
