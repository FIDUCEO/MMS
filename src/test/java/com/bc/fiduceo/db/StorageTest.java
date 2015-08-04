package com.bc.fiduceo.db;


import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

public class StorageTest {

    @Test
    public void testCreate_and_close() throws SQLException {
        final Storage storage = Storage.create();
        assertNotNull(storage);

        storage.close();
    }
}
