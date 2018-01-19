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

package com.bc.fiduceo.core;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.archive.ArchiveConfig;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SystemConfigTest {

    @Test
    public void testLoadAndGet_geometryLibrary() {
        final String useCaseXml = "<system-config>" +
                "    <geometry-library name = \"lib_name\" />" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        assertEquals("lib_name", systemConfig.getGeometryLibraryType());
    }

    @Test
    public void testLoadAndGet_invalidRootTag() {
        final String useCaseXml = "<system-thing>" +
                "    <geometry-library name = \"lib_name\" />" +
                "</system-thing>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        try {
            SystemConfig.load(inputStream);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("java.lang.RuntimeException", expected.getClass().getTypeName());
            assertEquals("Unable to initialize use case configuration: Root tag name 'system-config' expected", expected.getMessage());
        }
    }

    @Test
    public void testLoadAndGet_archiveConfig() {
        final String useCaseXml = "<system-config>" +
                "    <archive>" +
                "        <root-path>" +
                "            /usr/local/data/fiduceo" +
                "        </root-path>" +
                "        <rule sensors = \"wurst\">" +
                "            SENSOR/VERSION/YEAR" +
                "        </rule>" +
                "    </archive>" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final String expected = TestUtil.assembleFileSystemPath(new String[]{"usr", "local", "data", "fiduceo"}, true);
        assertEquals(expected, archiveConfig.getRootPath().toString());

        final Map<String, String[]> rules = archiveConfig.getRules();
        final String[] pathElements = rules.get("wurst");
        assertEquals(3, pathElements.length);
        assertEquals("VERSION", pathElements[1]);
    }

    @Test
    public void testDefaultValues() {
        final SystemConfig systemConfig = new SystemConfig();

        assertEquals("S2", systemConfig.getGeometryLibraryType());
        assertEquals(8, systemConfig.getReaderCacheSize());
    }

    @Test
    public void testLoadAndGet_ReaderCacheSize() {
        final String useCaseXml = "<system-config>" +
                "    <reader-cache-size>32</reader-cache-size>" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        assertEquals(32, systemConfig.getReaderCacheSize());
    }

    @Test
    public void testLoadAndGet_TempDir() {
        final String useCaseXml = "<system-config>" +
                "    <temp-directory>/wherever/I/Lay/my/hat</temp-directory>" +
                "</system-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final SystemConfig systemConfig = SystemConfig.load(inputStream);

        assertEquals("/wherever/I/Lay/my/hat", systemConfig.getTempDir());
    }
}
