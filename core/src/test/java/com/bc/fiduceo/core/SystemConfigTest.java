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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class SystemConfigTest {

    private File testDirectory;

    @Before
    public void setUp() {
        testDirectory = TestUtil.createTestDirectory();
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testLoadAndGetParameter() throws IOException {
        final File systemConfigFile = TestUtil.createFileInTestDir("system.properties");

        final Properties properties = new Properties();
        properties.setProperty("archive-root", testDirectory.getAbsolutePath());
        properties.setProperty("geometry-library-type", "S2");
        final FileOutputStream outputStream = new FileOutputStream(systemConfigFile);
        properties.store(outputStream, "");
        outputStream.close();

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(testDirectory);

        assertEquals(testDirectory.getAbsolutePath(), systemConfig.getArchiveRoot());
        assertEquals("S2", systemConfig.getGeometryLibraryType());
    }
}
