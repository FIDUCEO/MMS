package com.bc.fiduceo.db;


import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(DatabaseTestRunner.class)
public class StorageTest_SatelliteObservation_MySQL {

    private final BasicDataSource dataSource;
    private Storage storage;

    public StorageTest_SatelliteObservation_MySQL() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
    }

    @Before
    public void setUp() throws SQLException {
        storage = Storage.create(dataSource);
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException {
        storage.clear();
        storage.close();
    }

    @Test
    public void testInsert_andGet() throws SQLException, ParseException {
        final SatelliteObservation observation = crateSatelliteObservation();
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
    }

    @Test
    public void testInsert_andGet_sensorStoredInDb() throws SQLException, ParseException {
        final SatelliteObservation observation = crateSatelliteObservation();
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

    private SatelliteObservation crateSatelliteObservation() throws ParseException {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date(1430000000000L));
        observation.setStopTime(new Date(1430001000000L));
        observation.setNodeType(NodeType.ASCENDING);
        final Geometry geometry = new WKTReader().read("POLYGON((10 5,12 5,12 7,10 7,10 5))");
        observation.setGeoBounds(geometry);
        observation.setDataFile(new File("the_data.file"));
        return observation;
    }



    // @todo 2 tb/tb add test with null dates 2015-08-06
}
