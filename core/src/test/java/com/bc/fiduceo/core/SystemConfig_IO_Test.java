/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.core;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.archive.ArchiveConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class SystemConfig_IO_Test {

    private File testDir;

    @Before
    public void setUp() {
        testDir = TestUtil.getTestDir();
        if (!testDir.mkdirs()) {
            fail("unable to create test directory");
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testLoadFrom() throws IOException {
        TestUtil.writeSystemConfig(testDir);

        final SystemConfig systemConfig = SystemConfig.loadFrom(testDir);
        assertNotNull(systemConfig);

        assertEquals("S2", systemConfig.getGeometryLibraryType());
        assertEquals(12, systemConfig.getReaderCacheSize());

        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        assertEquals(TestUtil.getTestDataDirectory().getAbsolutePath(), archiveConfig.getRootPath().toString());

        assertEquals(TestUtil.getTestDir().getAbsolutePath(), systemConfig.getTempDir());
    }

    @Test
    public void testLoadFrom_missingConfigFile() throws IOException {
        try {
            SystemConfig.loadFrom(testDir);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
