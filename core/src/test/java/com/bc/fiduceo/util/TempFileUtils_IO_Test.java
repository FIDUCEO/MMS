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

package com.bc.fiduceo.util;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class TempFileUtils_IO_Test {

    private TempFileUtils tempFileUtils;
    private File testDir;

    @Before
    public void setUp() {
        tempFileUtils = new TempFileUtils();
        testDir = TestUtil.createTestDirectory();
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testConstruct_default() {
        final String systemTemp = System.getProperty("java.io.tmpdir");
        final File expected = new File(systemTemp);

        final File tempDir = tempFileUtils.getTempDir();
        assertEquals(expected.getAbsolutePath(), tempDir.getAbsolutePath());
    }

    @Test
    public void testConstruct_parameter() {
        final TempFileUtils tempFileUtils = new TempFileUtils(testDir.getAbsolutePath());

        final File tempDir = tempFileUtils.getTempDir();
        assertEquals(tempDir.getAbsolutePath(), tempDir.getAbsolutePath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testConstruct_parameter_notExisting() {
        final File testDir = TestUtil.getTestDir();
        TestUtil.deleteTestDirectory(); // need to remove it to force exception, we created it in setUp() tb 2018-01-22

        try {
            final TempFileUtils tempFileUtils = new TempFileUtils(testDir.getAbsolutePath());
            tempFileUtils.getTempDir();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testCreate() throws IOException {
        final File tempFile = tempFileUtils.create("ggam", "nc");
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
        final File file_1 = tempFileUtils.create("ggam", "nc");
        final File file_2 = tempFileUtils.create("ggap", "nc");
        final File file_3 = tempFileUtils.create("cdo", "sh");

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        tempFileUtils.cleanup();

        assertFalse(file_1.isFile());
        assertFalse(file_2.isFile());
        assertFalse(file_3.isFile());
    }

    @Test
    public void testCreateAndCleanup_deleteSkipped() throws IOException {
        final File file_1 = tempFileUtils.create("ggam", "nc");
        final File file_2 = tempFileUtils.create("ggap", "nc");
        final File file_3 = tempFileUtils.create("cdo", "sh");

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        tempFileUtils.keepAfterCleanup(true);
        tempFileUtils.cleanup();

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());
        // will be cleaned in teardown tb 2018-01-22
    }

    @Test
    public void testCreateAndDelete() throws IOException {
        final File file_1 = tempFileUtils.create("ggam", "nc");
        final File file_2 = tempFileUtils.create("ggap", "nc");
        final File file_3 = tempFileUtils.create("cdo", "sh");

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        tempFileUtils.delete(file_2);

        assertTrue(file_1.isFile());
        assertFalse(file_2.isFile());
        assertTrue(file_3.isFile());
    }

    @Test
    public void testCreate_customDir() throws IOException {
        File tempFile = null;
        try {
            final TempFileUtils tempFileUtils = new TempFileUtils(testDir.getAbsolutePath());
            tempFile = tempFileUtils.create("ggap", "nc");

            assertTrue(tempFile.isFile());

            assertTrue(tempFile.getAbsolutePath().startsWith(testDir.getAbsolutePath()));
        } finally {
            if (tempFile != null) {
                if (!tempFile.delete()) {
                    fail("unable to delete temp file");
                }
            }
        }
    }

    @Test
    public void testCreateAndCleanup_customDir() throws IOException {
        final File testDir = TestUtil.getTestDir();
        final TempFileUtils tempFileUtils = new TempFileUtils(testDir.getAbsolutePath());

        final File file_1 = tempFileUtils.create("ggam", "nc");
        final File file_2 = tempFileUtils.create("ggap", "nc");
        final File file_3 = tempFileUtils.create("cdo", "sh");

        assertTrue(file_1.isFile());
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());

        tempFileUtils.cleanup();

        assertFalse(file_1.isFile());
        assertFalse(file_2.isFile());
        assertFalse(file_3.isFile());
    }
}
