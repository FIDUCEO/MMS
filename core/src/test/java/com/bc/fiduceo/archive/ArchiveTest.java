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

package com.bc.fiduceo.archive;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.TimeUtils;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.*;

public class ArchiveTest {

    private final static String processingVersion = "1.0";
    private final static String sensorType = "amsub";

    private Archive archive;
    private Path root;
    private String separator;

    @Before
    public void setUp() throws IOException {
        final FileSystem fs = Jimfs.newFileSystem();
        root = fs.getPath("archiveRoot");
        Files.createDirectory(root);

        final ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setRootPath(root);
        archive = new Archive(archiveConfig);

        separator = FileSystems.getDefault().getSeparator();
    }

    @After
    public void tearDown() throws Exception {
        root.getFileSystem().close();
    }

    @Test
    public void testGetWithStartAndEndDate_defaultPath() throws Exception {
        // preparation
        final Path stPath = Files.createDirectory(root.resolve(sensorType));
        final Path pvPath = Files.createDirectory(stPath.resolve(processingVersion));
        final Path yearPath = Files.createDirectory(pvPath.resolve("2015"));
        final Path monPath = Files.createDirectory(yearPath.resolve("01"));

        for (int day = 21; day < 26; day++) {
            final Path directory = Files.createDirectory(monPath.resolve(String.format("%02d", day)));
            for (int i = 0; i < day - 20; i++) {
                Files.createFile(directory.resolve(String.format("productFile%d.nc", i)));
            }
        }

        final Date startDate = getDate("2015-021");
        final Date endDate = getDate("2015-025");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(15, productPaths.length);

        String expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2015", "01", "21", "productFile0.nc"}, false);
        assertEquals(expected, productPaths[0].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2015", "01", "24", "productFile3.nc"}, false);
        assertEquals(expected, productPaths[9].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2015", "01", "25", "productFile4.nc"}, false);
        assertEquals(expected, productPaths[14].toString());
    }

    @Test
    public void testGetWithStartAndEndDate_defaultPath_crossingMonthBoundary() throws Exception {
        // preparation
        final Path stPath = Files.createDirectory(root.resolve(sensorType));
        final Path pvPath = Files.createDirectory(stPath.resolve(processingVersion));
        final Path yearPath = Files.createDirectory(pvPath.resolve("2014"));
        final Path mayPath = Files.createDirectory(yearPath.resolve("05"));
        final Path junePath = Files.createDirectory(yearPath.resolve("06"));

        for (int day = 30; day <= 31; day++) {
            final Path directory = Files.createDirectory(mayPath.resolve(String.format("%02d", day)));
            for (int i = 0; i < 4; i++) {
                Files.createFile(directory.resolve(String.format("productFile%d_%d.nc", day, i)));
            }
        }
        for (int day = 1; day < 4; day++) {
            final Path directory = Files.createDirectory(junePath.resolve(String.format("%02d", day)));
            for (int i = 0; i < 4; i++) {
                Files.createFile(directory.resolve(String.format("productFile%d_%d.nc", day, i)));
            }
        }

        final Date startDate = getDate("2014-150");
        final Date endDate = getDate("2014-156");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(20, productPaths.length);
        String expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2014", "05", "30", "productFile30_0.nc"}, false);
        assertEquals(expected, productPaths[0].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2014", "05", "31", "productFile31_3.nc"}, false);
        assertEquals(expected, productPaths[7].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2014", "06", "01", "productFile1_0.nc"}, false);
        assertEquals(expected, productPaths[8].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "amsub", "1.0", "2014", "06", "03", "productFile3_3.nc"}, false);
        assertEquals(expected, productPaths[19].toString());
    }

    @Test
    public void testGetWithStartAndEndDate_configuredPath() throws Exception {
        // path: <root>/<version>/<sensor>/<year>/<month>
        // preparation
        final Path versionDir = Files.createDirectory(root.resolve(processingVersion));
        final Path sensorDir = Files.createDirectory(versionDir.resolve(sensorType));
        final Path yearPath = Files.createDirectory(sensorDir.resolve("2016"));
        final Path monPath = Files.createDirectory(yearPath.resolve("05"));

        for (int day = 11; day < 14; day++) {
            for (int i = 0; i < 15; i++) {
                Files.createFile(monPath.resolve(String.format("productFile%d_%d.nc", day, i)));
            }
        }
        final Date startDate = getDate("2016-132");
        final Date endDate = getDate("2016-135");

        final ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setRootPath(root);
        final String[] pathElements = {"VERSION", "SENSOR", "YEAR", "MONTH"};
        final HashMap<String, String[]> rules = new HashMap<>();
        rules.put(sensorType, pathElements);
        archiveConfig.setRules(rules);

        final Archive configuredArchive = new Archive(archiveConfig);

        // action
        final Path[] productPaths = configuredArchive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(45, productPaths.length);

        String expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "1.0", "amsub", "2016", "05", "productFile11_0.nc"}, false);
        assertEquals(expected, productPaths[0].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "1.0", "amsub", "2016", "05", "productFile12_14.nc"}, false);
        assertEquals(expected, productPaths[21].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "1.0", "amsub", "2016", "05", "productFile13_9.nc"}, false);
        assertEquals(expected, productPaths[44].toString());
    }

    @Test
    public void testGetWithStartAndEndDate_configuredPath_withCustomElements() throws Exception {
        // path: <root>/<version>/<sensor>/<year>/<month>
        // preparation
        final Path insituDir = Files.createDirectory(root.resolve("in-situ"));
        final Path versionDir = Files.createDirectory(insituDir.resolve(processingVersion));
        final Path sensorDir = Files.createDirectory(versionDir.resolve(sensorType));

        for (int i = 0; i < 15; i++) {
            Files.createFile(sensorDir.resolve(String.format("productFile%d.nc", i)));
        }

        final Date startDate = getDate("2016-132");
        final Date endDate = getDate("2016-135");

        final ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setRootPath(root);
        final String[] pathElements = {"in-situ", "VERSION", "SENSOR"};
        final HashMap<String, String[]> rules = new HashMap<>();
        rules.put(sensorType, pathElements);
        archiveConfig.setRules(rules);

        final Archive configuredArchive = new Archive(archiveConfig);

        // action
        final Path[] productPaths = configuredArchive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(15, productPaths.length);

        String expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "productFile0.nc"}, false);
        assertEquals(expected, productPaths[0].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "productFile3.nc"}, false);
        assertEquals(expected, productPaths[8].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "productFile9.nc"}, false);
        assertEquals(expected, productPaths[14].toString());
    }

    @Test
    public void testGetWithStartAndEndDate_configuredPath_withDayOfYear() throws Exception {
        // path: <root>/<version>/<sensor>/<year>/<day_of_year>
        // preparation
        final Path insituDir = Files.createDirectory(root.resolve("in-situ"));
        final Path versionDir = Files.createDirectory(insituDir.resolve(processingVersion));
        final Path sensorDir = Files.createDirectory(versionDir.resolve(sensorType));
        final Path yearDir = Files.createDirectory(sensorDir.resolve("2016"));
        final Path dayOfYearDir = Files.createDirectory(yearDir.resolve("008"));

        for (int i = 0; i < 15; i++) {
            Files.createFile(dayOfYearDir.resolve(String.format("productFile%d.nc", i)));
        }

        final Date startDate = getDate("2016-008");
        final Date endDate = getDate("2016-011");

        final ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setRootPath(root);
        final String[] pathElements = {"in-situ", "VERSION", "SENSOR", "YEAR", "DAY_OF_YEAR"};
        final HashMap<String, String[]> rules = new HashMap<>();
        rules.put(sensorType, pathElements);
        archiveConfig.setRules(rules);

        final Archive configuredArchive = new Archive(archiveConfig);

        // action
        final Path[] productPaths = configuredArchive.get(startDate, endDate, processingVersion, sensorType);
        // validation
        assertNotNull(productPaths);
        assertEquals(15, productPaths.length);

        String expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "2016", "008", "productFile0.nc"}, false);
        assertEquals(expected, productPaths[0].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "2016", "008", "productFile3.nc"}, false);
        assertEquals(expected, productPaths[8].toString());

        expected = TestUtil.assembleFileSystemPath(new String[]{"archiveRoot", "in-situ", "1.0", "amsub", "2016", "008", "productFile9.nc"}, false);
        assertEquals(expected, productPaths[14].toString());
    }

    @Test
    public void testGet_withNotExistingDir() throws IOException {
        final Date startDate = getDate("2015-021");
        final Date endDate = getDate("2015-025");

        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        assertNotNull(productPaths);
        assertEquals(0, productPaths.length);
    }

    @Test
    public void testGetWithStartAndEndDate_withEmpty_and_missingDir() throws IOException {

        //Preparation
        final Path stPath = Files.createDirectory(root.resolve(sensorType));
        final Path pvPath = Files.createDirectory(stPath.resolve(processingVersion));
        final Path yearPath = Files.createDirectory(pvPath.resolve("2015"));
        final Path monPath = Files.createDirectory(yearPath.resolve("01"));
        stPath.resolve(processingVersion).resolve("2015").resolve("01");

        Path _dir_01 = Files.createDirectory(monPath.resolve("01"));
        Files.createFile(_dir_01.resolve(String.format("productFile_%s.nc", "01")));

        Path _dir_04 = Files.createDirectory(monPath.resolve("04"));
        Files.createFile(_dir_04.resolve(String.format("productFile_%s.nc", "04")));

        Path _dir_06 = Files.createDirectory(monPath.resolve("06"));
        Files.createFile(_dir_06.resolve(String.format("productFile_%s.nc", "06")));

        final Date startDate = getDate("2015-01");
        final Date endDate = getDate("2015-10");

        // action
        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);

        // validation
        assertNotNull(productPaths);
        assertEquals(3, productPaths.length);
        assertEquals("archiveRoot\\amsub\\1.0\\2015\\01\\01\\productFile_01.nc".replace("\\", separator), productPaths[0].toString());
        assertEquals("archiveRoot\\amsub\\1.0\\2015\\01\\04\\productFile_04.nc".replace("\\", separator), productPaths[1].toString());
        assertEquals("archiveRoot\\amsub\\1.0\\2015\\01\\06\\productFile_06.nc".replace("\\", separator), productPaths[2].toString());
    }

    private Date getDate(String dateString) {
        return TimeUtils.parseDOYBeginOfDay(dateString);
    }


    @Test
    public void testValidProductPathCreation() {
        // preparation

        // action
        final Path productPath = archive.createValidProductPath(processingVersion, sensorType, 2001, 4, 3);

        // validation
        assertEquals("archiveRoot\\amsub\\1.0\\2001\\04\\03".replace("\\", separator), productPath.toString());
    }

    @Test
    public void testCreateDefaultPathElements() {
        final String[] defaultPathElements = Archive.createDefaultPathElements();
        assertNotNull(defaultPathElements);
        assertEquals(5, defaultPathElements.length);

        assertEquals("SENSOR", defaultPathElements[0]);
        assertEquals("VERSION", defaultPathElements[1]);
        assertEquals("YEAR", defaultPathElements[2]);
        assertEquals("MONTH", defaultPathElements[3]);
        assertEquals("DAY", defaultPathElements[4]);
    }

    @Test
    public void testGetPathElements_default() {
        final String[] pathElements = archive.getPathElements("unknown_sensor");

        assertEquals(5, pathElements.length);

        assertEquals("SENSOR", pathElements[0]);
        assertEquals("VERSION", pathElements[1]);
    }

    @Test
    public void testConstruction_withConfig() {
        final ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setRootPath(Paths.get("C:/data/storage"));

        final HashMap<String, String[]> rules = new HashMap<>();
        final String[] pathElements = {"bla", "SENSOR", "blubb", "VERSION"};
        rules.put("the_sensor", pathElements);
        archiveConfig.setRules(rules);

        final Archive archive = new Archive(archiveConfig);
        String expected = TestUtil.assembleFileSystemPath(new String[]{"C:", "data", "storage"}, false);
        assertEquals(expected, archive.getRootPath().toString());

        String[] sensorElements = archive.getPathElements("the_sensor");
        assertEquals(4, sensorElements.length);
        assertEquals("bla", sensorElements[0]);
        assertEquals("SENSOR", sensorElements[1]);

        sensorElements = archive.getPathElements("get_default_sensor");
        assertEquals(5, sensorElements.length);
        assertEquals("VERSION", sensorElements[1]);
        assertEquals("YEAR", sensorElements[2]);
    }

    @Test
    public void testGetVersion() {
        final ArchiveConfig archiveConfig = new ArchiveConfig();
        final HashMap<String, String[]> rules = new HashMap<>();
        final String[] pathElements = {"SENSOR", "VERSION", "YEAR", "MONTH", "DAY"};
        rules.put("mod021km-te", pathElements);
        archiveConfig.setRules(rules);
        archiveConfig.setRootPath(root);
        final Archive localArchive = new Archive(archiveConfig);

        assertEquals("v12", localArchive.getVersion("mod021km-te", root.resolve("mod021km-te/v12/2008/11/19")));
    }

    @Test
    public void testRelativeElements() {
        final String sep = File.separator;
        final Path root = Paths.get("C:" + sep + "data" + sep + "root");
        final Path archivePath = Paths.get("C:" + sep + "data" + sep + "root" + sep + "sensor" + sep + "version" + sep + "2008");

        final String[] elements = Archive.relativeElements(archivePath, root);
        assertEquals(3, elements.length);
        assertEquals("sensor", elements[0]);
        assertEquals("version", elements[1]);
        assertEquals("2008", elements[2]);
    }

    @Test
    public void testToRelative() throws IOException {
        final String[] pathElements = new String[]{"sensor", "year", "day_of_year", "heffalump.data"};

        Path absolutePath = root;
        for (final String pathElement : pathElements) {
            absolutePath = absolutePath.resolve(pathElement);
        }

        final Path relativePath = archive.toRelative(absolutePath);

        final Path expectedPath = Paths.get("sensor", "year", "day_of_year", "heffalump.data");
        assertEquals(expectedPath.toString(), relativePath.toString());
    }

    @Test
    public void testToRelative_notInArchive() throws IOException {
        final String[] pathElements = new String[]{"sensor", "year", "day_of_year", "heffalump.data"};

        Path absolutePath = Paths.get("elsewhere");
        for (final String pathElement : pathElements) {
            absolutePath = absolutePath.resolve(pathElement);
        }

        try {
            archive.toRelative(absolutePath);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testToAbsolute() {
        final String sep = File.separator;
        final Path relativePath = Paths.get("sensor", "year", "DOY", "data.file");

        final Path absolutePath = archive.toAbsolute(relativePath);

        assertEquals("archiveRoot" + sep + "sensor" + sep + "year" + sep + "DOY" + sep + "data.file",
                absolutePath.toString());
    }
}