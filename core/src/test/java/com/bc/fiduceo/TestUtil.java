
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

import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestUtil {

    private static final String SYSTEM_TEMP_PROPETY = "java.io.tmpdir";
    private static final String TEST_DIRECTORY = "fiduceo_test";

    public static File getTestDataDirectory() throws IOException {
        final InputStream resourceStream = TestUtil.class.getResourceAsStream("dataDirectory.properties");
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

    public static File createTestDirectory() {
        final File testDir = getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory: " + testDir.getAbsolutePath());
        }

        return testDir;
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

    private static File getTestDir() {
        final String tempDirPath = System.getProperty(SYSTEM_TEMP_PROPETY);
        return new File(tempDirPath, TEST_DIRECTORY);
    }
}
