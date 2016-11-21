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


import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(DatabaseTestRunner.class)
@Ignore
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
