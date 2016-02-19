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
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.s2.S2GeometryFactory;
import com.bc.fiduceo.reader.AMSU_MHS_L1B_Reader;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(DatabaseTestRunner.class)
public abstract class StorageTest_SatelliteObservation_S2 {

    // This test will use a local database implementation. Please make sure that you have a running MongoDb database server
    // version 3.2 or higher. The test assumes an empty schema "test" and uses the connection credentials stored
    // in the datasource description below. tb 2016-02-08

    private static String SENSOR_NAME = "test_sensor";
    protected BasicDataSource dataSource;
    protected Storage storage;
    private GeometryFactory geometryFactory;
    private AMSU_MHS_L1B_Reader reader;
    private Geometry geometry;


    @Before
    public void setUp() throws SQLException, IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();

        reader = new AMSU_MHS_L1B_Reader();
        File testDataDirectory = TestUtil.getTestDataDirectory();
        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
    }

    @After
    public void tearDown() throws SQLException, IOException {
        if (storage != null) {
            storage.clear();
            storage.close();

            reader.close();
        }
    }

    @Test
    public void testGet_emptyDatabase() throws SQLException {
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testInsert_andGet() throws SQLException, ParseException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);
        assertEquals(observation.getStartTime().getTime(), observationFromDb.getStartTime().getTime());
        assertEquals(observation.getStopTime().getTime(), observationFromDb.getStopTime().getTime());
        assertEquals(observation.getNodeType(), observationFromDb.getNodeType());
        assertEquals(observationFromDb.getGeoBounds().getCoordinates().length, 5);
        assertEquals(observation.getGeoBounds().getCoordinates().length, 4);
        assertEquals(observation.getSensor().getName(), observationFromDb.getSensor().getName());
        assertEquals(observation.getDataFile().getAbsolutePath(), observationFromDb.getDataFile().getAbsolutePath());
        assertEquals(observation.getTimeAxisStartIndex(), observationFromDb.getTimeAxisStartIndex());
        assertEquals(observation.getTimeAxisEndIndex(), observationFromDb.getTimeAxisEndIndex());
    }


    @Test
    public void testInsert_andGet_FromReader() throws SQLException, ParseException, IOException {

        final Date startTime = TimeUtils.create(1430000000000L);
        final Date stopTime = TimeUtils.create(1430001000000L);
        SatelliteObservation observation = createSatelliteObservationFromReader(startTime, stopTime);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);
        assertEquals(observation.getStartTime().getTime(), observationFromDb.getStartTime().getTime());
        assertEquals(observation.getStopTime().getTime(), observationFromDb.getStopTime().getTime());
        assertEquals(observation.getNodeType(), observationFromDb.getNodeType());
        assertEquals(observation.getDataFile().getAbsolutePath(), observationFromDb.getDataFile().getAbsolutePath());
        assertEquals(observation.getSensor().getName(), observationFromDb.getSensor().getName());
        assertEquals(observation.getTimeAxisStartIndex(), observationFromDb.getTimeAxisStartIndex());
        assertEquals(observation.getTimeAxisEndIndex(), observationFromDb.getTimeAxisEndIndex());

        assertEquals(observation.getGeoBounds().getCoordinates().length, 104);
        assertEquals(observationFromDb.getGeoBounds().getCoordinates().length, 106);
    }

    @Test
    public void testSearchByGeometry_polygon_geometryMatching_instersects() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("POLYGON ((11 3, 11 8, 11.2 8, 11.2 3, 11 3))");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByGeometry_polygon_geometryMatching_contains() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("POLYGON ((11 5.5, 11 6, 11.5 6, 11.5 5.5, 11 5.5))");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByGeometry_lineString_geometryNotMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("LINESTRING (-8 -12, -9 -14, -11 -17)");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByGeometry_lineString_geometryIntersecting() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("LINESTRING (8 6, 11 6.5, 14 7 )");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByGeometry_lineString_geometryContained() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("LINESTRING (10.5 6, 11 6.5, 11.5 6.2)");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByGeometry_point_geometryNotMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("POINT (-22 38)");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByGeometry_point_geometryContained() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("POINT (11 6.5)");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    private SatelliteObservation createSatelliteObservation() throws ParseException {
        final Date startTime = TimeUtils.create(1430000000000L);
        final Date stopTime = TimeUtils.create(1430001000000L);
        return createSatelliteObservation(startTime, stopTime);
    }

    private SatelliteObservation createSatelliteObservation(Date startTime, Date stopTime) throws ParseException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(startTime);
        observation.setStopTime(stopTime);
        observation.setNodeType(NodeType.ASCENDING);
        final Geometry geometry = geometryFactory.parse("POLYGON ((10 5, 10 7, 12 7, 12 5,10 5))");
        observation.setGeoBounds(geometry);
        observation.setDataFile(new File("the_data.file"));
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);

        final Sensor sensor = new Sensor();

        sensor.setName(SENSOR_NAME);
        observation.setSensor(sensor);

        return observation;
    }

    private SatelliteObservation createSatelliteObservationFromReader(Date startTime, Date stopTime) throws ParseException, IOException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(startTime);
        observation.setStopTime(stopTime);
        observation.setNodeType(NodeType.ASCENDING);
        observation.setDataFile(new File("the_data.file"));
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);

        final Sensor sensor = new Sensor();

        sensor.setName(SENSOR_NAME);
        observation.setSensor(sensor);

        AcquisitionInfo acquisitionInfo = reader.read();
        S2GeometryFactory geometryFactory = new S2GeometryFactory();


        if (acquisitionInfo.getMultiPolygons().size() > 0) {
            geometry = geometryFactory.createMultiPolygon(acquisitionInfo.getMultiPolygons());
        } else {
            geometry = geometryFactory.createPolygon(acquisitionInfo.getCoordinates());
        }
        observation.setGeoBounds(geometry);

        return observation;
    }
}
