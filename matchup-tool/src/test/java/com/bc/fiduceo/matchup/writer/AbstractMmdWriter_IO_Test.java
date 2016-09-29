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

package com.bc.fiduceo.matchup.writer;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.tool.ToolContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class AbstractMmdWriter_IO_Test {

    private File testDirectory;
    private ToolContext context;
    private MmdWriterConfig writerConfig;

    @Before
    public void setUp() {
        testDirectory = TestUtil.createTestDirectory();

        context = new ToolContext();
        context.setStartDate(new Date(8000000000L));
        context.setEndDate(new Date(8060000000L));
        writerConfig = new MmdWriterConfig();
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testCreateMmdFile() throws IOException {
        final UseCaseConfig useCaseConfig = createUseCaseConfig(testDirectory.getAbsolutePath());
        context.setUseCaseConfig(useCaseConfig);

        final File expected = new File(testDirectory, "uc-test_sensor-1_sensor-2_1970-093_1970-094.nc");

        final Path mmdFile = AbstractMmdWriter.createMmdFile(context, writerConfig);
        assertEquals(expected.getAbsolutePath(), mmdFile.toString());
        assertTrue(expected.isFile());
    }

    @Test
    public void testCreateMmdFile_inSubDirectory() throws IOException {
        final File subDir = new File(testDirectory, "sub-dir");
        final UseCaseConfig useCaseConfig = createUseCaseConfig(subDir.getAbsolutePath());
        context.setUseCaseConfig(useCaseConfig);

        final File expected = new File(subDir, "uc-test_sensor-1_sensor-2_1970-093_1970-094.nc");

        final Path mmdFile = AbstractMmdWriter.createMmdFile(context, writerConfig);
        assertEquals(expected.getAbsolutePath(), mmdFile.toString());
        assertTrue(expected.isFile());
    }

    @Test
    public void testCreateMmdFile_overwriteExisting() throws IOException {
        final UseCaseConfig useCaseConfig = createUseCaseConfig(testDirectory.getAbsolutePath());
        context.setUseCaseConfig(useCaseConfig);

        writerConfig.setOverwrite(true);

        final File expected = new File(testDirectory, "uc-test_sensor-1_sensor-2_1970-093_1970-094.nc");
        if (!expected.createNewFile()) {
            fail("unable to create test file");
        }
        final long lastModified = expected.lastModified();

        final Path mmdFile = AbstractMmdWriter.createMmdFile(context, writerConfig);
        assertEquals(expected.getAbsolutePath(), mmdFile.toString());
        assertTrue(expected.isFile());

        assertTrue(lastModified < expected.lastModified());
    }

    @Test
    public void testCreateMmdFile_existing_no_overwrite() throws IOException {
        final UseCaseConfig useCaseConfig = createUseCaseConfig(testDirectory.getAbsolutePath());
        context.setUseCaseConfig(useCaseConfig);

        writerConfig.setOverwrite(false);

        final File existingFile = new File(testDirectory, "uc-test_sensor-1_sensor-2_1970-093_1970-094.nc");
        if (!existingFile.createNewFile()) {
            fail("unable to create test file");
        }

        try {
            AbstractMmdWriter.createMmdFile(context, writerConfig);
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    private UseCaseConfig createUseCaseConfig(String absolutePath) {
        final String useCaseXml = "<use-case-config name=\"uc-test\">" +
                "  <sensors>" +
                "    <sensor>" +
                "      <name>sensor-1</name>" +
                "      <primary>true</primary>" +
                "    </sensor>" +
                "    <sensor>" +
                "      <name>sensor-2</name>" +
                "    </sensor>" +
                "  </sensors>" +
                "  <output-path>" +
                absolutePath +
                "</output-path>" +
                "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        return UseCaseConfig.load(inputStream);
    }
}
