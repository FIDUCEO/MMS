
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


import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class StorageTest {

    private final BasicDataSource dataSource;

    public StorageTest() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
    }

    @Test
    public void testCreate_and_close() throws SQLException {
        final Storage storage = Storage.create(dataSource);
        assertNotNull(storage);

        storage.close();
    }

    @Test
    public void testStorageIsSingleton() throws SQLException {
        final Storage storage_1 = Storage.create(dataSource);
        assertNotNull(storage_1);

        final Storage storage_2 = Storage.create(dataSource);
        assertNotNull(storage_2);

        try {
            assertSame(storage_1, storage_2);
        } finally {
            storage_1.close();
        }
    }

    @Test
    public void testCallingCloseTwice_noExceptionThrown() throws SQLException {
        final Storage storage = Storage.create(dataSource);

        storage.close();
        storage.close();
    }

    @Test
    public void testInitWithUnregisteredDriverUrlThrows() throws SQLException {
        final BasicDataSource weirdDataSource = new BasicDataSource();
        weirdDataSource.setUrl("stupid:unregistered:data_base:driver");

        try {
            Storage.create(weirdDataSource);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
