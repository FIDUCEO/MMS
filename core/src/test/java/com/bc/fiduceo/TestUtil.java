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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.geometry.Point;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.dbcp2.BasicDataSource;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class TestUtil {

    private static final String SYSTEM_TEMP_PROPETY = "java.io.tmpdir";
    private static final String TEST_DIRECTORY = "fiduceo_test";

    private static File testDataDirectory = null;

    public static File getTestDataDirectory() throws IOException {
        if (testDataDirectory == null) {
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
            testDataDirectory = dataDirectory;
        }
        return testDataDirectory;
    }

    public static File getTestDataFileAsserted(String testFilePath) throws IOException {
        final File testDataDirectory = getTestDataDirectory();
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    public static void writeDatabaseProperties_MongoDb(File configDir) throws IOException {
        final BasicDataSource datasource = TestUtil.getDataSource_MongoDb();
        writeDatabaseProperties(configDir, datasource);
    }

    public static void writeDatabaseProperties_Postgres(File configDir) throws IOException {
        final BasicDataSource datasource = TestUtil.getDataSource_Postgres();
        writeDatabaseProperties(configDir, datasource);
    }

    public static void writeDatabaseProperties_H2(File configDir) throws IOException {
        final BasicDataSource datasource = TestUtil.getDatasource_H2();
        writeDatabaseProperties(configDir, datasource);
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

    public static BasicDataSource getDataSource_MongoDb() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("mongodb");
        dataSource.setUrl("mongodb://localhost:27017");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
        return dataSource;
    }

    public static BasicDataSource getDataSource_Postgres() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/test");
        dataSource.setUsername("fiduceo");
        dataSource.setPassword("oecudif");
        return dataSource;
    }

    public static void writeSystemConfig(File configDir) throws IOException {
        final String systemConfigXML = "<system-config>" +
                                       "    <geometry-library name = \"S2\" />" +
                                       "    <archive>" +
                                       "        <root-path>" +
                                       "            " + TestUtil.getTestDataDirectory().getAbsolutePath() +
                                       "        </root-path>" +
                                       "        <rule sensors = \"drifter-sst, ship-sst, gtmba-sst, radiometer-sst, argo-sst, xbt-sst, mbt-sst, ctd-sst, animal-sst, bottle-sst\">" +
                                       "            insitu/SENSOR/VERSION" +
                                       "        </rule>" +
                                       "        <rule sensors=\"ocean-rain-sst\">\n" +
                                       "            insitu/SENSOR/VERSION\n" +
                                       "        </rule>" +
                                       "        <rule sensors=\"gruan-uleic\">\n" +
                                       "            insitu/SENSOR/VERSION\n" +
                                       "        </rule>" +
                                       "        <rule sensors = \"iasi-ma, iasi-mb\">" +
                                       "            SENSOR/VERSION/YEAR/MONTH" +
                                       "        </rule>" +
                                       "        <rule sensors = \"mod06-te, myd06-aq, airs-aq\">" +
                                       "            SENSOR/VERSION/YEAR/DAY_OF_YEAR" +
                                       "        </rule>" +
                                       "    </archive>" +
                                       "    <temp-directory>" +
                                       "            " + TestUtil.getTestDir().getAbsolutePath() +
                                       "    </temp-directory>" +
                                       "    <reader-cache-size>12</reader-cache-size>" +
                                       "</system-config>";


        final File systemConfigFile = new File(configDir, "system-config.xml");
        if (!systemConfigFile.createNewFile()) {
            fail("unable to create test file: " + systemConfigFile.getAbsolutePath());
        }

        writeStringTo(systemConfigFile, systemConfigXML);
    }

    public static void writeMmdWriterConfig(File configDir) throws IOException {
        final String config = "<mmd-writer-config>" +
                              "    <overwrite>false</overwrite>" +
                              "    <cache-size>2048</cache-size>" +
                              "    <netcdf-format>N4</netcdf-format>" +
                              "</mmd-writer-config>";

        writeMmdWriterConfigFile(configDir, config);
    }

    public static void writeMmdWriterConfig(File configDir, String additionalTags) throws IOException {
        final String config = "<mmd-writer-config>" +
                              "    <overwrite>false</overwrite>" +
                              "    <cache-size>2048</cache-size>" +
                              "    <netcdf-format>N4</netcdf-format>" +
                              additionalTags +
                              "</mmd-writer-config>";

        writeMmdWriterConfigFile(configDir, config);
    }

    public static void writeMmdWriterConfigFile(File configDir, String xml) throws IOException {
        final File propertiesFile = new File(configDir, "mmd-writer-config.xml");
        if (!propertiesFile.createNewFile()) {
            fail("unable to create test file: " + propertiesFile.getAbsolutePath());
        }

        writeStringTo(propertiesFile, xml);
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals("year: ", year, calendar.get(Calendar.YEAR));
        assertEquals("month: ", month, calendar.get(Calendar.MONTH) + 1);
        assertEquals("day: ", day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("hour: ", hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("minute: ", minute, calendar.get(Calendar.MINUTE));
        assertEquals("second: ", second, calendar.get(Calendar.SECOND));
    }

    public static void assertCorrectUTCDate(int year, int month, int day, int hour, int minute, int second, int millisecond, Date utcDate) {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.setTime(utcDate);

        assertEquals("year: ", year, calendar.get(Calendar.YEAR));
        assertEquals("month: ", month, calendar.get(Calendar.MONTH) + 1);
        assertEquals("day: ", day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("hour: ", hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("minute: ", minute, calendar.get(Calendar.MINUTE));
        assertEquals("second: ", second, calendar.get(Calendar.SECOND));
        assertEquals("millisecond: ", millisecond, calendar.get(Calendar.MILLISECOND));
    }

    public static void assertWithinLastMinute(Date expected, Date actual) {
        final long timeDeltaInMillis = Math.abs(expected.getTime() - actual.getTime());
        assertTrue(timeDeltaInMillis < 60000);
    }

    public static File createTestDirectory() {
        final File testDir = getTestDir();
        if (testDir.isDirectory()) {
            return testDir;
        }
        if (testDir.isFile() || !testDir.mkdirs()) {
            fail("unable to create test directory: " + testDir.getAbsolutePath());
        }
        return testDir;
    }

    /**
     * Returns a "fiduceo_test" directory which is a child of system temp directory
     *
     * @return new File(&lt;systemTempDir&gt;, "fiduceo_test")
     */
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

        final File testFile = new File(testDirectory, fileName);
        if (!testFile.createNewFile()) {
            fail("Unable to create test file: " + testFile.getAbsolutePath());
        }
        return testFile;
    }

    public static File copyFileDir(File sourceFile, File targetDirectory) throws IOException {
        assertTrue(sourceFile.isFile());

        final String name = sourceFile.getName();
        final File targetFile = new File(targetDirectory, name);
        if (!targetFile.createNewFile()) {
            throw new IOException("Unable to create test file: " + targetFile.getAbsolutePath());
        }

        Files.copy(sourceFile.toPath(), targetFile.toPath(), REPLACE_EXISTING);

        assertTrue(targetFile.isFile());

        return targetFile;
    }

    public static String assembleFileSystemPath(String[] pathSegments, boolean absolute) {
        final StringBuilder builder = new StringBuilder();
        final String sep = File.separator;
        if (absolute) {
            builder.append(sep);
        }
        for (int i = 0; i < pathSegments.length; i++) {
            builder.append(pathSegments[i]);
            if (i < pathSegments.length - 1) {
                builder.append(sep);
            }
        }

        return builder.toString();
    }

    public static Element createDomElement(String XML) throws JDOMException, IOException {
        final SAXBuilder saxBuilder = new SAXBuilder();
        final Document document = saxBuilder.build(new ByteArrayInputStream(XML.getBytes()));
        return document.getRootElement();
    }

    public static void writeStringTo(File outFile, String data) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            outputStream.write(data.getBytes());
            outputStream.flush();
        }
    }

    /**
     * Creates a matcher of {@link String} that matches when the examined string matches the
     * specified pattern.
     * <p/>
     * For example:
     * <pre>assertThat("Sabine", matchesPattern("..bi.."))</pre>
     *
     * @param pattern the pattern to match strings
     */
    @Factory
    public static Matcher<String> matchesPattern(final String pattern) {
        return new StringPatternMatcher(pattern);
    }

    public static Archive getArchive() throws IOException {
        final File dataDirectory = getTestDataDirectory();
        final String archiveXML = "<archive>" +
                "    <root-path>" +
                dataDirectory +
                "    </root-path>" +
                "</archive>";

        final ArchiveConfig archiveConfig = ArchiveConfig.parse(archiveXML);
        return new Archive(archiveConfig);
    }

    private static void storeProperties(Properties properties, File configDir, String child) throws IOException {
        final File propertiesFile = new File(configDir, child);
        if (!propertiesFile.createNewFile()) {
            fail("unable to create test file: " + propertiesFile.getAbsolutePath());
        }

        try (FileOutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, "");
        }
    }

    private static void writeDatabaseProperties(File configDir, BasicDataSource datasource) throws IOException {
        final Properties properties = new Properties();
        convertToProperties(properties, datasource);

        TestUtil.storeProperties(properties, configDir, "database.properties");
    }

    private static void convertToProperties(Properties properties, BasicDataSource datasource) {
        properties.setProperty("driverClassName", datasource.getDriverClassName());
        properties.setProperty("url", datasource.getUrl());
        properties.setProperty("username", datasource.getUsername());
        properties.setProperty("password", datasource.getPassword());
    }

    static class StringPatternMatcher extends TypeSafeMatcher<String> {

        private final Pattern compiledPattern;

        StringPatternMatcher(String pattern) {
            if (pattern == null) {
                throw new IllegalArgumentException("Non-null pattern required.");
            }

            this.compiledPattern = Pattern.compile(pattern);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a string matching the pattern '" + compiledPattern.pattern() + "'");
        }

        @Override
        protected boolean matchesSafely(String item) {
            return compiledPattern.matcher(item).matches();
        }
    }
}