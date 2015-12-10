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

package com.bc.fiduceo.ingest;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class IngestionToolIntegrationTest {

    private File testDirectory;
    private File configDir;

    @Before
    public void setUp() {
        testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testIngest_notInputParameter() throws ParseException, IOException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        final String[] args = new String[0];
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_help() throws ParseException, IOException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        String[] args = new String[]{"-h"};
        IngestionToolMain.main(args);

        args = new String[]{"--help"};
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_missingSystemProperties() throws ParseException, IOException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs.aqua"};

        writeDatabaseProperties();

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingDatabaseProperties() throws ParseException, IOException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs.aqua"};

        writeSystemProperties();

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_AIRS() throws ParseException, IOException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs.aqua"};

        writeSystemProperties();
        writeDatabaseProperties();

        IngestionToolMain.main(args);
    }

    private void writeDatabaseProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("driverClassName", "org.h2.Driver");
        properties.setProperty("url", "jdbc:h2:mem:fiduceo");
        properties.setProperty("username", "ignore");
        properties.setProperty("password", "ignore");

        storePropertieToTemp(properties, "database.properties");
    }

    private void writeSystemProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("archive_root", TestUtil.getTestDataDirectory().getAbsolutePath());

        storePropertieToTemp(properties, "system.properties");
    }

    private void storePropertieToTemp(Properties properties, String child) throws IOException {
        final File dataSourcePropertiesFile = new File(configDir, child);
        if (!dataSourcePropertiesFile.createNewFile()) {
            fail("Unable to create test file: " + dataSourcePropertiesFile.getAbsolutePath());
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(dataSourcePropertiesFile);
            properties.store(outputStream, "");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
