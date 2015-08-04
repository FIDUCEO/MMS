package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class StorageTest_SatelliteObservation {

    private final BasicDataSource dataSource;
    private Storage storage;

    public StorageTest_SatelliteObservation() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        dataSource.setUrl("jdbc:derby:memory:fiduceo");
    }

    @Before
    public void setUp() throws SQLException {
        storage = Storage.create(dataSource);
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException {
        storage.clear();
        storage.clear();
    }

    @Test
    public void testInsert() {

    }
}
