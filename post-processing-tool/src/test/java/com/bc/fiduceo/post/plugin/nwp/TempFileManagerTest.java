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

package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class TempFileManagerTest {

    private TempFileManager fileManager;

    @Before
    public void setUp() {
        fileManager = new TempFileManager();

        TestUtil.createTestDirectory();
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }
    
    @Test
    public void testCreate() throws IOException {
        final File tempFile = fileManager.create("ggam", "nc");
        assertNotNull(tempFile);

        try {
            assertTrue(tempFile.isFile());
            assertTrue(tempFile.getName().contains("ggam"));
            assertTrue(tempFile.getName().contains(".nc"));

            final String tempDirPath = System.getProperty("java.io.tmpdir");
            assertTrue(tempFile.getAbsolutePath().startsWith(tempDirPath));
        } finally {
            if (!tempFile.delete()) {
                fail("unable to delete temp file");
            }
        }
    }

    @Test
    public void testCreateAndCleanup() throws IOException {
        final File file_1 = fileManager.create("ggam", "nc");
        final File file_2 = fileManager.create("ggap", "nc");
        final File file_3 = fileManager.create("cdo", "sh");
        
        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        fileManager.cleanup();

        assertFalse(file_1.isFile());
        assertFalse(file_2.isFile());
        assertFalse(file_3.isFile());
    }

    @Test
    public void testCreate_customDir() throws IOException {
        final File testDir = TestUtil.getTestDir();
        fileManager.setTempDir(testDir.getAbsolutePath());

        final File tempFile = fileManager.create("ggam", "nc");
        assertNotNull(tempFile);

        try {
            assertTrue(tempFile.isFile());

            assertTrue(tempFile.getAbsolutePath().startsWith(testDir.getAbsolutePath()));
        } finally {
            if (!tempFile.delete()) {
                fail("unable to delete temp file");
            }
        }
    }

    @Test
    public void testCreateAndCleanup_customDir() throws IOException {
        final File testDir = TestUtil.getTestDir();
        fileManager.setTempDir(testDir.getAbsolutePath());

        final File file_1 = fileManager.create("ggam", "nc");
        final File file_2 = fileManager.create("ggap", "nc");
        final File file_3 = fileManager.create("cdo", "sh");

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        fileManager.cleanup();

        assertFalse(file_1.isFile());
        assertFalse(file_2.isFile());
        assertFalse(file_3.isFile());
    }
}
