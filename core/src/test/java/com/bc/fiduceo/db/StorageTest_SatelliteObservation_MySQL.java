package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.junit.runner.RunWith;

@RunWith(DatabaseTestRunner.class)
public class StorageTest_SatelliteObservation_MySQL extends StorageTest_SatelliteObservation {

    // This test will use a local database implementation. Please make sure that you have a running MySQL database server
    // version 5.6 or higher. The test assumes an empty schema "test" and uses the connection credentials stored
    // in the datasource description below. tb 2015-08-10

    public StorageTest_SatelliteObservation_MySQL() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
    }
}
