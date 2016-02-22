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
public class StorageTest_SatelliteObservation_MongoDB_S2 extends StorageTest_SatelliteObservation {

    // This test will use a local database implementation. Please make sure that you have a running MongoDb database server
    // version 3.2 or higher. The test assumes an empty schema "test" and uses the connection credentials stored
    // in the datasource description below. tb 2016-02-08


    @Before
    public void setUp() throws SQLException, IOException {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("mongodb");
        dataSource.setUrl("mongodb://localhost:27017/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");


        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(dataSource, geometryFactory);
        storage.initialize();

        reader = new AMSU_MHS_L1B_Reader();
        File testDataDirectory = TestUtil.getTestDataDirectory();
        File file = new File(testDataDirectory, "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5");
        reader.open(file);
    }


    @After
    public void tearDown() throws IOException, SQLException {
        reader.close();
        storage.clear();
        storage.close();
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


    private SatelliteObservation createSatelliteObservationFromReader(Date startTime, Date stopTime) throws ParseException, IOException {
        Geometry geometry;
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(startTime);
        observation.setStopTime(stopTime);
        observation.setNodeType(NodeType.ASCENDING);
        observation.setDataFile(new File("the_data.file"));
        observation.setTimeAxisStartIndex(23);
        observation.setTimeAxisEndIndex(27);

        final Sensor sensor = new Sensor();

        String SENSOR_NAME = "test_sensor";
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
