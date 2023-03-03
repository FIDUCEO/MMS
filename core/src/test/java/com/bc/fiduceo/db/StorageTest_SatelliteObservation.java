/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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


import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.L3TimeAxis;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public abstract class StorageTest_SatelliteObservation {

    DatabaseConfig databaseConfig;

    private GeometryFactory geometryFactory;
    private Storage storage;

    @Before
    public void setUp() throws SQLException, IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(databaseConfig, geometryFactory);
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException {
        if (storage != null) {
            storage.clear();
            storage.close();
        }
    }

    @Test
    public void testGet_emptyDatabase() throws SQLException {
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testIsAlreadyRegistered_true() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME);
        queryParameter.setPath(TestData.DATA_FILE_PATH);
        assertTrue(storage.isAlreadyRegistered(queryParameter));
    }

    @Test
    public void testIsAlreadyRegistered_false_SensorName() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME + "other");
        queryParameter.setPath(TestData.DATA_FILE_PATH);
        assertFalse(storage.isAlreadyRegistered(queryParameter));
    }

    @Test
    public void testIsAlreadyRegistered_false_DataFilePath() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME);
        queryParameter.setPath(TestData.DATA_FILE_PATH + "other");
        assertFalse(storage.isAlreadyRegistered(queryParameter));
    }

    @Test
    public void testInsert_andGet() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);
        assertEquals(observation.getStartTime().getTime(), observationFromDb.getStartTime().getTime());
        assertEquals(observation.getStopTime().getTime(), observationFromDb.getStopTime().getTime());
        assertEquals(observation.getNodeType(), observationFromDb.getNodeType());
        assertEquals(observation.getVersion(), observationFromDb.getVersion());

        final Geometry geoBoundsFromDb = observationFromDb.getGeoBounds();
        final String geoBoundsWkt = geometryFactory.format(geoBoundsFromDb);
        assertEquals("POLYGON((12.0 4.999999999999998,12.000000000000004 7.000000000000001,9.999999999999998 7.0,9.999999999999998 4.999999999999998,12.0 4.999999999999998))", geoBoundsWkt);

        assertEquals(observation.getSensor().getName(), observationFromDb.getSensor().getName());
        assertEquals(observation.getDataFilePath().toString(), observationFromDb.getDataFilePath().toString());

        final TimeAxis[] timeAxes = observationFromDb.getTimeAxes();
        assertEquals(1, timeAxes.length);
        TestUtil.assertCorrectUTCDate(2015, 4, 25, 22, 13, 20, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2015, 4, 25, 22, 30, 0, 0, timeAxes[0].getEndTime());
        final Geometry geometry = timeAxes[0].getGeometry();
        assertEquals("LINESTRING(0.9999999999999997 4.999999999999998,0.9999999999999997 6.0,0.9999999999999997 6.999999999999999)", geometryFactory.format(geometry));
    }

    @Test
    public void testInsert_andGet_boundaryAsLinestring() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1440000000000L), TimeUtils.create(1440001000000L), "LINESTRING(10 2, 11 6, 12 7, 13 8, 14 9)", geometryFactory);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);

        final Geometry geoBoundsFromDb = observationFromDb.getGeoBounds();
        final String geoBoundsWkt = geometryFactory.format(geoBoundsFromDb);
        assertEquals("LINESTRING(10.0 2.0,11.0 6.000000000000001,12.000000000000004 7.000000000000001,13.0 7.999999999999997,14.0 9.0)", geoBoundsWkt);
    }

    @Test
    public void testInsert_andGet_boundaryAsMultipolygon() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1440000000000L), TimeUtils.create(1440001000000L), "MULTIPOLYGON(((10 2, 11 6, 12 7, 13 8, 14 9)),((0 0, 1 0, 1 1, 0 1, 0 0)))", geometryFactory);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);

        final Geometry geoBoundsFromDb = observationFromDb.getGeoBounds();
        final String geoBoundsWkt = geometryFactory.format(geoBoundsFromDb);
        assertEquals("MULTIPOLYGON(((14.0 9.0,13.0 7.999999999999997,12.000000000000004 7.000000000000001,11.0 6.000000000000001,10.0 2.0,14.0 9.0)),((0.0 0.0,1.0 0.0,0.9999999999999997 1.0,0.0 1.0,0.0 0.0)))", geoBoundsWkt);
    }

    @Test
    public void testInsert_andGet_noGeometry_noTimeAxes() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        observation.setGeoBounds(null);
        observation.setTimeAxes(null);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);
        assertEquals(observation.getStartTime().getTime(), observationFromDb.getStartTime().getTime());
        assertEquals(observation.getStopTime().getTime(), observationFromDb.getStopTime().getTime());
        assertEquals(observation.getNodeType(), observationFromDb.getNodeType());
        assertEquals(observation.getVersion(), observationFromDb.getVersion());

        assertEquals(observation.getSensor().getName(), observationFromDb.getSensor().getName());
        assertEquals(observation.getDataFilePath().toString(), observationFromDb.getDataFilePath().toString());
    }

    @Test
    public void testInsert_andGet_twoTimeAxes() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);

        final TimeAxis[] timeAxes = new TimeAxis[2];
        final TimeAxis timeAxis = TestData.createTimeAxis("LINESTRING(2 5, 2 6, 2 7)", TimeUtils.create(1440000000000L), TimeUtils.create(1450000000000L), geometryFactory);
        timeAxes[0] = observation.getTimeAxes()[0];
        timeAxes[1] = timeAxis;
        observation.setTimeAxes(timeAxes);

        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);

        final TimeAxis[] timeAxesFromDb = observationFromDb.getTimeAxes();
        assertEquals(2, timeAxesFromDb.length);

        TestUtil.assertCorrectUTCDate(2015, 4, 25, 22, 13, 20, 0, timeAxesFromDb[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2015, 4, 25, 22, 30, 0, 0, timeAxesFromDb[0].getEndTime());
        final Geometry geometry = timeAxesFromDb[0].getGeometry();
        assertEquals("LINESTRING(0.9999999999999997 4.999999999999998,0.9999999999999997 6.0,0.9999999999999997 6.999999999999999)", geometryFactory.format(geometry));
    }

    @Test
    public void testInsert_andGet_L3TimeAxis() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);


        final Geometry multiLineString = geometryFactory.parse("MULTILINESTRING((-2 3, -1 5), (-56 3, 56 4))");
        final L3TimeAxis l3TimeAxis = new L3TimeAxis(TimeUtils.create(1440000000000L), TimeUtils.create(1450000000000L), multiLineString);
        final TimeAxis[] timeAxes = new TimeAxis[] {l3TimeAxis};
        observation.setTimeAxes(timeAxes);

        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);

        final TimeAxis[] timeAxesFromDb = observationFromDb.getTimeAxes();
        assertEquals(1, timeAxesFromDb.length);

        TestUtil.assertCorrectUTCDate(2015, 8, 19, 16, 0, 0, 0, timeAxesFromDb[0].getStartTime());
        TestUtil.assertCorrectUTCDate(2015, 12, 13, 9, 46, 40, 0, timeAxesFromDb[0].getEndTime());
        final Geometry geometry = timeAxesFromDb[0].getGeometry();
        assertEquals("MULTILINESTRING((-1.9999999999999993 3.000000000000001,-0.9999999999999997 4.999999999999998),(-56.00000000000001 3.000000000000001,56.0 4.0))", geometryFactory.format(geometry));
    }

    @Test
    public void testInsert_andGet_sensorStoredInDb() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);

        final Sensor sensor = observation.getSensor();
        storage.insert(sensor);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sensor.getName(), result.get(0).getSensor().getName());
    }

    @Test
    public void testUpdate() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        observation.setStartTime(TimeUtils.create(1440000000000L));
        observation.setStopTime(TimeUtils.create(1440001000000L));
        observation.setGeoBounds(geometryFactory.parse("POLYGON ((11 5, 11 7, 13 7, 13 5, 11 5))"));
        final TimeAxis timeAxis = TestData.createTimeAxis("LINESTRING(2 5, 2 6, 2 7)", observation.getStartTime(), observation.getStopTime(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis});
        observation.setNodeType(NodeType.ASCENDING);
        observation.setVersion("newOne!");
        observation.setSensor(new Sensor("new Name"));

        storage.update(observation);

        result = storage.get();
        assertEquals(1, result.size());

        final SatelliteObservation updatedObservation = result.get(0);
        assertEquals(1440000000000L, updatedObservation.getStartTime().getTime());
        assertEquals(1440001000000L, updatedObservation.getStopTime().getTime());
        assertEquals("POLYGON((13.0 4.999999999999998,13.0 6.999999999999999,11.0 6.999999999999999,11.0 4.999999999999998,13.0 4.999999999999998))", geometryFactory.format(updatedObservation.getGeoBounds()));

        final TimeAxis[] timeAxes = observation.getTimeAxes();
        assertEquals(1, timeAxes.length);
        assertEquals(1440000000000L, timeAxes[0].getStartTime().getTime());
        assertEquals(1440001000000L, timeAxes[0].getEndTime().getTime());
        assertEquals("LINESTRING(1.9999999999999996 4.999999999999999,1.9999999999999996 6.0,1.9999999999999996 6.999999999999999)", geometryFactory.format(timeAxes[0].getGeometry()));
        assertEquals(NodeType.ASCENDING, updatedObservation.getNodeType());
        // path must be the same, this is the DB logic, so we don't check here tb 2022-06-14
        assertEquals("newOne!", updatedObservation.getVersion());
        final Sensor sensor = updatedObservation.getSensor();
        assertEquals("new Name", sensor.getName());
    }

    @Test
    public void testUpdate_notInDb() throws SQLException {
        List<SatelliteObservation> result = storage.get();
        assertEquals(0, result.size());

        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);

        storage.update(observation);

        result = storage.get();
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTime_startTime_matchObservation() throws SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = TestData.createSatelliteObservation(startTime, stopTime, geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Date searchTime = TimeUtils.create(1000400000L);
        parameter.setStartTime(searchTime);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());

        // @todo 2 tb/** add more assertions here 2016-03-06
        final SatelliteObservation observationFromDb = result.get(0);
        final TimeAxis[] timeAxes = observationFromDb.getTimeAxes();
        assertEquals(1, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1970, 1, 12, 13, 46, 40, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1970, 1, 12, 14, 3, 20, 0, timeAxes[0].getEndTime());
        final Geometry geometry = timeAxes[0].getGeometry();
        assertEquals("LINESTRING(0.9999999999999997 4.999999999999998,0.9999999999999997 6.0,0.9999999999999997 6.999999999999999)", geometryFactory.format(geometry));
    }

    @Test
    public void testSearchByTime_startTime_laterThanObservation() throws SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = TestData.createSatelliteObservation(startTime, stopTime, geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001400000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTime_stopTime_matchObservation() throws SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = TestData.createSatelliteObservation(startTime, stopTime, geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Date searchTime = TimeUtils.create(1000400000L);
        parameter.setStopTime(searchTime);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());

        // @todo 2 tb/** add more assertions here 2016-03-06
        final SatelliteObservation observationFromDb = result.get(0);
        final TimeAxis[] timeAxes = observationFromDb.getTimeAxes();
        assertEquals(1, timeAxes.length);
        TestUtil.assertCorrectUTCDate(1970, 1, 12, 13, 46, 40, 0, timeAxes[0].getStartTime());
        TestUtil.assertCorrectUTCDate(1970, 1, 12, 14, 3, 20, 0, timeAxes[0].getEndTime());
        final Geometry geometry = timeAxes[0].getGeometry();
        assertEquals("LINESTRING(0.9999999999999997 4.999999999999998,0.9999999999999997 6.0,0.9999999999999997 6.999999999999999)", geometryFactory.format(geometry));
    }

    @Test
    public void testSearchByTime_stopTime_earlierThanObservation() throws SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = TestData.createSatelliteObservation(startTime, stopTime, geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStopTime(TimeUtils.create(1000000000L - 100L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_Earlier() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L - 1000L));
        parameter.setStopTime(TimeUtils.create(1000000000L - 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_intersectSensorStart() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L - 500L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_inSensorRange() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L + 500L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_intersectSensorStop() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001000000L - 500L));
        parameter.setStopTime(TimeUtils.create(1001000000L + 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_later() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001000000L + 500L));
        parameter.setStopTime(TimeUtils.create(1001000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensor_matching() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(TestData.SENSOR_NAME);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchBySensor_notMatching() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("strange-name");

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_matching() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(TestData.SENSOR_NAME);
        parameter.setStartTime(TimeUtils.create(1000000000L - 100L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_timeNotMatching() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(TestData.SENSOR_NAME);
        parameter.setStartTime(TimeUtils.create(1000000000L - 2000L));
        parameter.setStopTime(TimeUtils.create(1000000000L - 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_sensorNotMatching() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("blablabla");
        parameter.setStartTime(TimeUtils.create(1000000000L - 100L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByVersion_notAvailableVersion() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setVersion("not_available");

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testSearchByVersion_matchingVersion() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setVersion(TestData.VERSION);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(1, satelliteObservations.size());
    }

    @Test
    public void testSearchByPath_notAvailablePath() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setPath("not_available_path");

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testSearchByPath_matchingPath() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setPath(TestData.DATA_FILE_PATH);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(1, satelliteObservations.size());
    }

    @Test
    public void testMultipleTimeAxes() throws SQLException {
        final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
        final TimeAxis timeAxis_1 = TestData.createTimeAxis("LINESTRING(1 5, 1 6, 1 7)", new Date(), new Date(), geometryFactory);
        final TimeAxis timeAxis_2 = TestData.createTimeAxis("LINESTRING(2 5, 2 6, 2 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});

        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(1, satelliteObservations.size());

        final SatelliteObservation satelliteObservation = satelliteObservations.get(0);
        final TimeAxis[] timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);
    }

    @Test
    public void testTwoObservations_MultipleTimeAxes() throws SQLException {
        SatelliteObservation observation = TestData.createSatelliteObservation(new Date(10000000L), new Date(11000000L), geometryFactory);
        TimeAxis timeAxis_1 = TestData.createTimeAxis("LINESTRING(1 5, 1 6, 1 7)", new Date(), new Date(), geometryFactory);
        TimeAxis timeAxis_2 = TestData.createTimeAxis("LINESTRING(2 5, 2 6, 2 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});

        storage.insert(observation);

        observation = TestData.createSatelliteObservation(new Date(20000000L), new Date(21000000L), geometryFactory);
        timeAxis_1 = TestData.createTimeAxis("LINESTRING(3 5, 3 6, 3 7)", new Date(), new Date(), geometryFactory);
        timeAxis_2 = TestData.createTimeAxis("LINESTRING(4 5, 4 6, 4 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});

        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(2, satelliteObservations.size());

        SatelliteObservation satelliteObservation = satelliteObservations.get(0);
        TimeAxis[] timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);

        satelliteObservation = satelliteObservations.get(1);
        timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);
    }

    @Test
    public void testThreeObservations_twoSensors_MultipleTimeAxes() throws SQLException {
        SatelliteObservation observation = TestData.createSatelliteObservation(new Date(10000000L), new Date(11000000L), geometryFactory);
        TimeAxis timeAxis_1 = TestData.createTimeAxis("LINESTRING(1 5, 1 6, 1 7)", new Date(), new Date(), geometryFactory);
        TimeAxis timeAxis_2 = TestData.createTimeAxis("LINESTRING(2 5, 2 6, 2 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});

        storage.insert(observation);

        observation = TestData.createSatelliteObservation(new Date(20000000L), new Date(21000000L), geometryFactory);
        timeAxis_1 = TestData.createTimeAxis("LINESTRING(3 5, 3 6, 3 7)", new Date(), new Date(), geometryFactory);
        timeAxis_2 = TestData.createTimeAxis("LINESTRING(4 5, 4 6, 4 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});
        observation.setSensor(new Sensor("the_second_one"));

        storage.insert(observation);

        observation = TestData.createSatelliteObservation(new Date(30000000L), new Date(31000000L), geometryFactory);
        timeAxis_1 = TestData.createTimeAxis("LINESTRING(5 5, 5 6, 5 7)", new Date(), new Date(), geometryFactory);
        timeAxis_2 = TestData.createTimeAxis("LINESTRING(6 5, 6 6, 6 7)", new Date(), new Date(), geometryFactory);
        observation.setTimeAxes(new TimeAxis[]{timeAxis_1, timeAxis_2});

        storage.insert(observation);

        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setSensorName(TestData.SENSOR_NAME);

        List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(2, satelliteObservations.size());

        SatelliteObservation satelliteObservation = satelliteObservations.get(0);
        TimeAxis[] timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);

        satelliteObservation = satelliteObservations.get(1);
        timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);

        queryParameter = new QueryParameter();
        queryParameter.setSensorName("the_second_one");

        satelliteObservations = storage.get(queryParameter);
        assertEquals(1, satelliteObservations.size());

        satelliteObservation = satelliteObservations.get(0);
        timeAxes = satelliteObservation.getTimeAxes();
        assertEquals(2, timeAxes.length);
    }

    @Test
    public void testInsertAndGet_withOffset() throws SQLException {
        SatelliteObservation observation = TestData.createSatelliteObservation(new Date(10000000000L), new Date(10000010000L), geometryFactory);
        storage.insert(observation);
        observation = TestData.createSatelliteObservation(new Date(20000000000L), new Date(20000010000L), geometryFactory);
        storage.insert(observation);
        observation = TestData.createSatelliteObservation(new Date(30000000000L), new Date(30000010000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setOffset(1);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(2, satelliteObservations.size());

        final SatelliteObservation observation_1 = satelliteObservations.get(0);
        assertEquals(20000000000L, observation_1.getStartTime().getTime());
        assertEquals(20000010000L, observation_1.getStopTime().getTime());

        final SatelliteObservation observation_2 = satelliteObservations.get(1);
        assertEquals(30000000000L, observation_2.getStartTime().getTime());
        assertEquals(30000010000L, observation_2.getStopTime().getTime());
    }

    @Test
    public void testInsertAndGet_withPaging() throws SQLException {
        SatelliteObservation observation = TestData.createSatelliteObservation(new Date(20000000000L), new Date(20000010000L), geometryFactory);
        storage.insert(observation);
        observation = TestData.createSatelliteObservation(new Date(30000000000L), new Date(30000010000L), geometryFactory);
        storage.insert(observation);
        observation = TestData.createSatelliteObservation(new Date(40000000000L), new Date(40000010000L), geometryFactory);
        storage.insert(observation);

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setPageSize(2);

        final List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        assertEquals(2, satelliteObservations.size());

        final SatelliteObservation observation_1 = satelliteObservations.get(0);
        assertEquals(20000000000L, observation_1.getStartTime().getTime());
        assertEquals(20000010000L, observation_1.getStopTime().getTime());

        final SatelliteObservation observation_2 = satelliteObservations.get(1);
        assertEquals(30000000000L, observation_2.getStartTime().getTime());
        assertEquals(30000010000L, observation_2.getStopTime().getTime());
    }

}
