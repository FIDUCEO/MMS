
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


import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TimeUtils;
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class StorageTest_SatelliteObservation {

    protected BasicDataSource dataSource;
    protected Storage storage;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws SQLException {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        storage = Storage.create(dataSource, geometryFactory);
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
    public void testInsert_andGet() throws SQLException, ParseException {
        // @todo 1 tb/tb continue with this 2016-01-11

        final SatelliteObservation observation = createSatelliteObservation();


        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        final SatelliteObservation observationFromDb = result.get(0);
        assertEquals(observation.getStartTime().getTime(), observationFromDb.getStartTime().getTime());
        assertEquals(observation.getStopTime().getTime(), observationFromDb.getStopTime().getTime());
        assertEquals(observation.getNodeType(), observationFromDb.getNodeType());
        assertEquals(observation.getGeoBounds().toString(), observationFromDb.getGeoBounds().toString());
        assertEquals(observation.getSensor().getName(), observationFromDb.getSensor().getName());
        assertEquals(observation.getDataFile().getAbsolutePath(), observationFromDb.getDataFile().getAbsolutePath());
        assertEquals(observation.getTimeAxisStartIndex(), observationFromDb.getTimeAxisStartIndex());
        assertEquals(observation.getTimeAxisEndIndex(), observationFromDb.getTimeAxisEndIndex());
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
    public void testSearchByTimeRange_startTime_matchObservation() throws ParseException, SQLException {
        final Date startTime = TimeUtils.create(1000000000L);
        final Date stopTime = TimeUtils.create(1001000000L);
        final SatelliteObservation observation = createSatelliteObservation(startTime, stopTime);
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        final Date searchTime = TimeUtils.create(1000400000L);
        parameter.setStartTime(searchTime);

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchByTimeRange_startTime_laterThanObservation() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1001400000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        assertEquals(0, result.size());
    }


    @Test
    public void testSearchByTimeRange_searchTimeInObservationRange() throws ParseException, SQLException {
        final SatelliteObservation observation = createSatelliteObservation(TimeUtils.create(1000000000L), TimeUtils.create(1001000000L));
        storage.insert(observation);                                                 //1000400000L

        final QueryParameter parameter = new QueryParameter();
        parameter.setStartTime(TimeUtils.create(1000400000L));
        parameter.setStopTime(TimeUtils.create(1000700000L));

        final List<SatelliteObservation> result = storage.get(parameter);
        //assertEquals(1, result.size());

        final List<SatelliteObservation> satelliteObservations = storage.get();
        final SatelliteObservation satelliteObservation = satelliteObservations.get(0);
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
        final com.bc.fiduceo.geometry.Geometry geometry = geometryFactory.parse("POLYGON ((10 5, 10 7, 12 7, 12 5, 10 5))");
        observation.setGeoBounds(geometry);
        observation.setDataFile(new File("the_data.file"));
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);

        final Sensor sensor = new Sensor();
        sensor.setName("test_sensor");
        observation.setSensor(sensor);

        return observation;
    }
}
