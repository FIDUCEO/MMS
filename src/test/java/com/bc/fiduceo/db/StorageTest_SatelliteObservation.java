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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StorageTest_SatelliteObservation {

    private final BasicDataSource dataSource;
    private Storage storage;

    public StorageTest_SatelliteObservation() {
        dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
//        dataSource.setUrl("jdbc:derby:memory:YO");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
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
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(new Date(1430000000000L));
        observation.setStopTime(new Date(1430001000000L));
        observation.setNodeType(NodeType.ASCENDING);
        final Geometry geometry = new WKTReader().read("POLYGON((10 5,12 5,12 7,10 7,10 5))");
        observation.setGeoBounds(geometry);
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
    }



    // @todo 2 tb/tb add test with null dates 2015-08-06
    // @todo 1 tb/tb test with existing sensor 2015-08-10
}
