
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


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.bc.fiduceo.core.NodeType;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class StorageTest_SatelliteObservation {

    protected BasicDataSource dataSource;
    protected Storage storage;

    @Before
    public void setUp() throws SQLException {
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException {
        storage.clear();
        storage.close();
    }

    @Test
    public void testGet_emptyDatabase() throws SQLException {
        final List<SatelliteObservation> satelliteObservations = storage.get();
        assertEquals(0, satelliteObservations.size());
    }

    @Test
    public void testInsert_andGet() throws SQLException, ParseException {
        final SatelliteObservation observation = createSatelliteObservation();
        final Sensor sensor = new Sensor();
        sensor.setName("test_sensor");
        observation.setSensor(sensor);

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
        final Sensor sensor = new Sensor();
        sensor.setName("test_sensor");
        observation.setSensor(sensor);

        storage.insert(sensor);
        storage.insert(observation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sensor.getName(), result.get(0).getSensor().getName());
    }

    protected SatelliteObservation createSatelliteObservation() throws ParseException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date(1430000000000L));
        observation.setStopTime(new Date(1430001000000L));
        observation.setNodeType(NodeType.ASCENDING);
        final Geometry geometry = new WKTReader().read("POLYGON((10 5,12 5,12 7,10 7,10 5))");
        observation.setGeoBounds(geometry);
        observation.setDataFile(new File("the_data.file"));
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);
        return observation;
    }
}
