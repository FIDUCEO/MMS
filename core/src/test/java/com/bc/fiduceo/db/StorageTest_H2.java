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


import org.apache.commons.dbcp.BasicDataSource;

public class StorageTest_H2 extends StorageTest {

    public StorageTest_H2() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        // the following line dumps all database interactions to the console window tb 2016-02-10
//        dataSource.setUrl("jdbc:h2:mem:fiduceo;TRACE_LEVEL_SYSTEM_OUT=2");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
    }
}
