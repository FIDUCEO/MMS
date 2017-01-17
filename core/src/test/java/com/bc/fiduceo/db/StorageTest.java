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


import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public abstract class StorageTest {

    BasicDataSource dataSource;

    private final GeometryFactory geometryFactory;

    StorageTest() {
        geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testCreate_and_close() throws SQLException {
        final Storage storage = Storage.create(dataSource, geometryFactory);
        assertNotNull(storage);

        storage.close();
    }

    @Test
    public void testCallingCloseTwice_noExceptionThrown() throws SQLException {
        final Storage storage = Storage.create(dataSource, geometryFactory);

        storage.close();
        storage.close();
    }

    @Test
    public void testInitWithUnregisteredDriverUrlThrows() throws SQLException {
        final BasicDataSource weirdDataSource = new BasicDataSource();
        weirdDataSource.setUrl("stupid:unregistered:data_base:driver");

        try {
            Storage.create(weirdDataSource, geometryFactory);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsInitialized_storageClosed() throws SQLException {
        final Storage storage = Storage.create(dataSource, geometryFactory);

        storage.clear();
        storage.close();

        assertFalse(storage.isInitialized());
    }

    @Test
    public void testIsInitialized_storageCleared() throws SQLException {
        final Storage storage = Storage.create(dataSource, geometryFactory);

        storage.clear();

        assertFalse(storage.isInitialized());
    }

    @Test
    public void testIsInitialized_initialized() throws SQLException {
        final Storage storage = Storage.create(dataSource, geometryFactory);
        try {
            storage.initialize();

            assertTrue(storage.isInitialized());
        } finally {
            storage.clear();
            storage.close();
        }
    }
}
