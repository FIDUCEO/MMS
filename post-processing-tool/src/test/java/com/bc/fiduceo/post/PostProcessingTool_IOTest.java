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

package com.bc.fiduceo.post;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class PostProcessingTool_IOTest {

    private File configDir;
    private File testDir;

    @Before
    public void setUp() throws Exception {
        testDir = TestUtil.createTestDirectory();
        configDir = new File(testDir, "config");
        if (!configDir.mkdirs()) {
            fail("unable to create test directory");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
        }
    }

    @Test
    public void testInitialisation() throws Exception {
        final Path src = Paths.get(getClass().getResource("processing_config.xml").toURI()).toAbsolutePath();

        final String absoluteProcessingConfigPath = src.toString();
        initialize(absoluteProcessingConfigPath);

        final String relativeProcessingConfigPath = "processingConfig.xml";
        final OutputStream outputStream = Files.newOutputStream(configDir.toPath().resolve(relativeProcessingConfigPath));
        Files.copy(src, outputStream);
        outputStream.flush();
        outputStream.close();
        initialize(relativeProcessingConfigPath);
    }

    private void initialize(String processingConfigPath) throws ParseException, IOException {

        final Options options = PostProcessingTool.getOptions();
        final PosixParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(options, new String[]{
                "-j", processingConfigPath,
                "-i", "/mmd_files",
                "-start", "2011-123",
                "-end", "2011-124",
                "-c", configDir.getPath()
        });

        final FileWriter fileWriter = new FileWriter(new File(configDir, "system-config.xml"));
        fileWriter.write("<system-config></system-config>");
        fileWriter.close();

        final PostProcessingContext context = PostProcessingTool.initialize(commandLine);

        final String separator = FileSystems.getDefault().getSeparator();
        assertEquals(separator + "mmd_files", context.getMmdInputDirectory().toString());
        assertEquals("03-May-2011 00:00:00", ProductData.UTC.createDateFormat().format(context.getStartDate()));
        assertEquals("04-May-2011 23:59:59", ProductData.UTC.createDateFormat().format(context.getEndDate()));

        final SystemConfig sysConfig = context.getSystemConfig();
        assertNotNull(sysConfig);
        assertNull(sysConfig.getArchiveConfig());

        final PostProcessingConfig config = context.getProcessingConfig();
        assertNotNull(config);
        final List<PostProcessing> processings = config.getProcessings();
        assertNotNull(processings);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", processings.getClass().getTypeName());
        assertEquals(1, processings.size());
        assertEquals("com.bc.fiduceo.post.distance.PostSphericalDistance", processings.get(0).getClass().getTypeName());
    }
}
