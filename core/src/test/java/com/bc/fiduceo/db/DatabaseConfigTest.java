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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class DatabaseConfigTest {

    private File testDirectory;
    private DatabaseConfig databaseConfig;

    @Before
    public void setUp() {
        testDirectory = TestUtil.createTestDirectory();
        databaseConfig = new DatabaseConfig();
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testLoadAndGetDataSource() throws IOException, SQLException {
        final File databaseConfigFile = TestUtil.createFileInTestDir("database.properties");

        final PrintWriter printWriter = new PrintWriter(databaseConfigFile);
        printWriter.write("driverClassName = driver-class\n");
        printWriter.write("url = database-url\n");
        printWriter.write("username = user-name\n");
        printWriter.write("password = pass-word");
        printWriter.close();

        databaseConfig.loadFrom(testDirectory);

        final BasicDataSource dataSource = databaseConfig.getDataSource();
        assertNotNull(dataSource);
        assertEquals("driver-class", dataSource.getDriverClassName());
        assertEquals("database-url", dataSource.getUrl());
        assertEquals("user-name", dataSource.getUsername());
        assertEquals("pass-word", dataSource.getPassword());
    }

    @Test
    public void testLoadAndGetTimeout() throws IOException {
        final File databaseConfigFile = TestUtil.createFileInTestDir("database.properties");

        final PrintWriter printWriter = new PrintWriter(databaseConfigFile);
        printWriter.write("timeout = 654");
        printWriter.close();

        databaseConfig.loadFrom(testDirectory);

        assertEquals(654, databaseConfig.getTimeoutInSeconds());
    }

    @Test
    public void testLoadAndGetTimeout_default() throws IOException {
        final File databaseConfigFile = TestUtil.createFileInTestDir("database.properties");

        final PrintWriter printWriter = new PrintWriter(databaseConfigFile);
        printWriter.write("driverClassName = driver-class\n");
        printWriter.write("url = database-url\n");
        printWriter.write("username = user-name\n");
        printWriter.write("password = pass-word");
        printWriter.close();

        databaseConfig.loadFrom(testDirectory);

        assertEquals(120, databaseConfig.getTimeoutInSeconds());
    }

    @Test
    public void testLoad_throwsWhenFileNotPresent() throws IOException {
        try {
            databaseConfig.loadFrom(testDirectory);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetDatasource_throwsWhenNotLoaded() throws SQLException {
        try {
            databaseConfig.getDataSource();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
