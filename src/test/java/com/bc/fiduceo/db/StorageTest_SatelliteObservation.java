package com.bc.fiduceo.db;


import com.bc.fiduceo.core.SatelliteObservation;
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
    public void testInsert_andGet() throws SQLException {
        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        satelliteObservation.setStartTime(new Date(1430000000000L));

        storage.insert(satelliteObservation);

        final List<SatelliteObservation> result = storage.get();
        assertNotNull(result);
        assertEquals(1, result.size());

        // @todo 1 tb/tb add checks for values 2015-08-06
    }



    // @todo 2 tb/tb add test with null dates 2015-08-06
}
