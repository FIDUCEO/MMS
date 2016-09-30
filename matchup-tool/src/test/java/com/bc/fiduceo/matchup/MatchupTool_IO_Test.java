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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.matchup.writer.MmdWriterConfig;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(IOTestRunner.class)
public class MatchupTool_IO_Test {

    private File testDirectory;

    @Before
    public void setUp() {
        testDirectory = TestUtil.createTestDirectory();
    }

    @After
    public void taerDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testLoadWriterConfig() throws IOException {
        TestUtil.writeMmdWriterConfig(testDirectory);

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("config", "./config")).thenReturn(testDirectory.getAbsolutePath());

        final MmdWriterConfig mmdWriterConfig = MatchupTool.loadWriterConfig(commandLine);
        assertNotNull(mmdWriterConfig);
        assertFalse(mmdWriterConfig.isOverwrite());
    }

    @Test
    public void testLoadWriterConfig_configMissing() throws IOException {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("config", "./config")).thenReturn(testDirectory.getAbsolutePath());

        try {
            MatchupTool.loadWriterConfig(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}

