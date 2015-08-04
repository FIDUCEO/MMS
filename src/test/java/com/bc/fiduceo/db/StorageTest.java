package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class StorageTest {

    private final BasicDataSource dataSource;

    public StorageTest() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        dataSource.setUrl("jdbc:derby:bc/fiduceo");
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
    public void testCallingCloseTwice() throws SQLException {
        final Storage storage = Storage.create(dataSource);

        storage.close();

        storage.close();
    }
}
