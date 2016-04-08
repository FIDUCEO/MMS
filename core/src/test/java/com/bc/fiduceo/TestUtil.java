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

package com.bc.fiduceo;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.dbcp.BasicDataSource;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUtil {

    private static final String SYSTEM_TEMP_PROPETY = "java.io.tmpdir";
    private static final String TEST_DIRECTORY = "fiduceo_test";

    public static Path getTestDataDirectoryPath() throws IOException {
        return Paths.get(getTestDataDirectory().toURI());
    }

    public static File getTestDataDirectory() throws IOException {
        final InputStream resourceStream = TestUtil.class.getResourceAsStream("dataDirectory.properties");
        if (resourceStream == null) {
            fail("missing resource: 'dataDirectory.properties'");
        }
        final Properties properties = new Properties();
        properties.load(resourceStream);
        final String dataDirectoryProperty = properties.getProperty("dataDirectory");
        if (dataDirectoryProperty == null) {
            fail("Property 'dataDirectory' is not set.");
        }
        final File dataDirectory = new File(dataDirectoryProperty);
        if (!dataDirectory.isDirectory()) {
            fail("Property 'dataDirectory' supplied does not exist: '" + dataDirectoryProperty + "'");
        }
        return dataDirectory;
    }

    public static void storePropertiesToTemp(Properties properties, File configDir, String child) throws IOException {
        final File dataSourcePropertiesFile = new File(configDir, child);
        if (!dataSourcePropertiesFile.createNewFile()) {
            fail("unable to create test file: " + dataSourcePropertiesFile.getAbsolutePath());
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

    public static void writeDatabaseProperties_MongoDb(File configDir) throws IOException {
        final Properties properties = new Properties();
        final BasicDataSource datasource = TestUtil.getdatasourceMongoDb();
        convertToProperties(properties, datasource);

        TestUtil.storePropertiesToTemp(properties, configDir, "database.properties");
    }

    public static void writeDatabaseProperties_H2(File configDir) throws IOException {
        final Properties properties = new Properties();
        final BasicDataSource datasource = TestUtil.getDatasource_H2();
        convertToProperties(properties, datasource);

        TestUtil.storePropertiesToTemp(properties, configDir, "database.properties");
    }

    public static BasicDataSource getDatasource_H2() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        // the following line dumps all database interactions to the console window tb 2016-02-10
//        dataSource.setUrl("jdbc:h2:mem:fiduceo;TRACE_LEVEL_SYSTEM_OUT=2");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    public static BasicDataSource getdatasourceMongoDb() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("mongodb");
        dataSource.setUrl("mongodb://localhost:27017/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
        return dataSource;
    }

    public static void writeSystemProperties(File configDir) throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("archive-root", TestUtil.getTestDataDirectory().getAbsolutePath());
        properties.setProperty("geometry-library-type", "S2");
        properties.setProperty("netcdf-format", "N4");

        TestUtil.storePropertiesToTemp(properties, configDir, "system.properties");
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(second, calendar.get(Calendar.SECOND));
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, int millisecond, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month - 1, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
        assertEquals(second, calendar.get(Calendar.SECOND));
        assertEquals(millisecond, calendar.get(Calendar.MILLISECOND));
    }

    public static void assertWithinLastMinute(Date expected, Date actual) {
        final long timeDeltaInMillis = Math.abs(expected.getTime() - actual.getTime());
        assertTrue(timeDeltaInMillis < 60000);
    }

    public static File createTestDirectory() {
        deleteTestDirectory();
        final File testDir = getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory: " + testDir.getAbsolutePath());
        }
        return testDir;
    }

    public static File getTestDir() {
        final String tempDirPath = System.getProperty(SYSTEM_TEMP_PROPETY);
        return new File(tempDirPath, TEST_DIRECTORY);
    }

    public static void deleteTestDirectory() {
        final File testDir = getTestDir();
        if (testDir.isDirectory()) {
            final boolean deleted = FileUtils.deleteTree(testDir);
            if (!deleted) {
                fail("unable to delete test directory: " + testDir.getAbsolutePath());
            }
        }
    }

    public static File createFileInTestDir(String fileName) throws IOException {
        final File testDirectory = getTestDir();

        final File databaseConfigFile = new File(testDirectory, fileName);
        if (!databaseConfigFile.createNewFile()) {
            fail("Unable to create test file: " + databaseConfigFile.getAbsolutePath());
        }
        return databaseConfigFile;
    }

    public static String assembleFileSystemPath(String[] pathSegments, boolean relative){
        final StringBuilder builder = new StringBuilder();
        final String sep = File.separator;
        if (relative) {
            builder.append(sep);
        }
        for (int i = 0; i < pathSegments.length; i++){
            builder.append(pathSegments[i]);
            if (i < pathSegments.length - 1){
                builder.append(sep);
            }
        }

        return builder.toString();
    }

    public static Coordinate[] getCoordinates(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = new Coordinate(point.getLon(), point.getLat());
        }
        return coordinates;
    }

    private static void convertToProperties(Properties properties, BasicDataSource datasource) {
        properties.setProperty("driverClassName", datasource.getDriverClassName());
        properties.setProperty("url", datasource.getUrl());
        properties.setProperty("username", datasource.getUsername());
        properties.setProperty("password", datasource.getPassword());
    }
}
