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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public abstract class StorageTest_SatelliteObservation {

    private static String SENSOR_NAME = "test_sensor";

    protected BasicDataSource dataSource;
    protected Storage storage;
    protected GeometryFactory geometryFactory;

    @Before
    public void setUp() throws SQLException, IOException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException, IOException {
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
    public void testInsert_andGet_sensorStoredInDb() throws SQLException, ParseException {
        final SatelliteObservation observation = createSatelliteObservation();

        final Sensor sensor = observation.getSensor();
        storage.insert(sensor);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sensor.getName(), result.get(0).getSensor().getName());
    }

    @Test
    public void testSearchByTime_startTime_matchObservation() throws ParseException, SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = createSatelliteObservation(startTime, stopTime);
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
    public void testSearchByTime_startTime_laterThanObservation() throws ParseException, SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = createSatelliteObservation(startTime, stopTime);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001400000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTime_stopTime_matchObservation() throws ParseException, SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = createSatelliteObservation(startTime, stopTime);
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
    public void testSearchByTime_stopTime_earlierThanObservation() throws ParseException, SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = createSatelliteObservation(startTime, stopTime);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStopTime(TimeUtils.create(1000000000L - 100L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_Earlier() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L - 1000L));
        parameter.setStopTime(TimeUtils.create(1000000000L - 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_intersectSensorStart() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L - 500L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_inSensorRange() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000000000L + 500L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_intersectSensorStop() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001000000L - 500L));
        parameter.setStopTime(TimeUtils.create(1001000000L + 500L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_searchRange_later() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001000000L + 500L));
        parameter.setStopTime(TimeUtils.create(1001000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensor_matching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(SENSOR_NAME);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchBySensor_notMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("strange-name");

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_matching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(SENSOR_NAME);
        parameter.setStartTime(TimeUtils.create(1000000000L - 100L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_timeNotMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName(SENSOR_NAME);
        parameter.setStartTime(TimeUtils.create(1000000000L - 2000L));
        parameter.setStopTime(TimeUtils.create(1000000000L - 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchBySensorAndTime_sensorNotMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setSensorName("blablabla");
        parameter.setStartTime(TimeUtils.create(1000000000L - 100L));
        parameter.setStopTime(TimeUtils.create(1000000000L + 1000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchByGeometry_polygon_geometryNotMatching() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation();
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Geometry geometry = geometryFactory.parse("POLYGON ((1 5, 1 7, 2 7, 2 5, 1 5))");
        parameter.setGeometry(geometry);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
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

    private SatelliteObservation createSatelliteObservation(Date startTime, Date stopTime) throws ParseException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(startTime);
        observation.setStopTime(stopTime);
        observation.setNodeType(NodeType.ASCENDING);
        final Geometry geometry = geometryFactory.parse("POLYGON ((10 5, 10 7, 12 7, 12 5, 10 5))");
        observation.setGeoBounds(geometry);

        observation.setDataFilePath("the_data.file");

        final LineString timeAxisGeometry = (LineString) geometryFactory.parse("LINESTRING(1 5, 1 6, 1 7)");
        final TimeAxis timeAxis = geometryFactory.createTimeAxis(timeAxisGeometry, startTime, stopTime);
        observation.setTimeAxes(new TimeAxis[]{timeAxis});

        observation.setSensor(new Sensor(SENSOR_NAME));

        return observation;
    }

    private SatelliteObservation createSatelliteObservation() throws ParseException {
        final Date startTime = TimeUtils.create(1430000000000L);
        final Date stopTime = TimeUtils.create(1430001000000L);
        return createSatelliteObservation(startTime, stopTime);
    }
}
