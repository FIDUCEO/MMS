package com.bc.fiduceo.db;

import org.apache.commons.dbcp.BasicDataSource;

public class StorageTest_SatelliteObservation_PostGIS extends StorageTest_SatelliteObservation {

    // This test will use a local database implementation. Please make sure that you have a running PostgreSQL database server
    // version 9.4 or higher with PostGIS extension version 2.1 or higher. The test assumes an empty schema "test" and
    // uses the connection credentials stored in the datasource description below. tb 2015-08-31

    public StorageTest_SatelliteObservation_PostGIS() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
    }
}
