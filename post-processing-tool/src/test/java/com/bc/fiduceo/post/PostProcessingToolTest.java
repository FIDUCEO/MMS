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
 */

package com.bc.fiduceo.post;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.*;
import org.mockito.InOrder;
import org.mockito.Mockito;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostProcessingToolTest {

    private File configDir;
    private File testDir;

    @Before
    public void setUp() throws Exception {
        testDir = new File(TestUtil.getTestDir(), "PostProcessingToolTest");
        configDir = new File(testDir, "config");
    }

    @After
    public void tearDown() throws Exception {
        if (testDir.isDirectory()) {
            FileUtils.deleteTree(testDir);
        }
    }

    @Test
    public void testOptions() {
        final Options options = PostProcessingTool.getOptions();
        assertEquals(6, options.getOptions().size());

        Option o;

        o = options.getOption("c");
        assertNotNull(o);
        assertEquals("config", o.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(false, o.isRequired());


        o = options.getOption("d");
        assertNotNull(o);
        assertEquals("mmd-dir", o.getLongOpt());
        assertEquals("Defines the path to the input mmd files directory.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("end");
        assertNotNull(o);
        assertEquals("end-time", o.getLongOpt());
        assertEquals("Defines the processing end-date, format 'yyyy-DDD'", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("h");
        assertNotNull(o);
        assertEquals("help", o.getLongOpt());
        assertEquals("Prints the tool usage.", o.getDescription());
        assertEquals(false, o.hasArg());
        assertEquals(false, o.isRequired());

        o = options.getOption("j");
        assertNotNull(o);
        assertEquals("job-config", o.getLongOpt());
        assertEquals("Defines the path to post processing job configuration file. Path is relative to the configuration directory.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("start");
        assertNotNull(o);
        assertEquals("start-time", o.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());
    }

    @Test
    public void testInitialisation() throws Exception {
        final Options options = PostProcessingTool.getOptions();
        final PosixParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(options, new String[]{
                    "-j", Paths.get(getClass().getResource("processing_config.xml").toURI()).toAbsolutePath().toString(),
                    "-d", "/mmd_files",
                    "-start", "2011-123",
                    "-end", "2011-124",
                    "-c", configDir.getPath()
        });
        configDir.mkdirs();
        final FileWriter fileWriter = new FileWriter(new File(configDir, "system-config.xml"));
        fileWriter.write("<system-config></system-config>");
        fileWriter.close();

        final PostProcessingContext context = PostProcessingTool.initialize(commandLine);

        assertEquals("\\mmd_files", context.getMmdInputDirectory().toString());
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

    @Test
    public void name() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        PostProcessingTool.printUsageTo(out);

        final String ls = System.lineSeparator();

        final String expected = "post-processing-tool version 1.1.1-SNAPSHOT" + ls +
                                "" + ls +
                                "usage: post-processing-tool <options>" + ls +
                                "Valid options are:" + ls +
                                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                                "   -d,--mmd-dir <arg>          Defines the path to the input mmd files directory." + ls +
                                "   -end,--end-time <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
                                "   -h,--help                   Prints the tool usage." + ls +
                                "   -j,--job-config <arg>       Defines the path to post processing job configuration file. Path is relative to the" + ls +
                                "                               configuration directory." + ls +
                                "   -start,--start-time <arg>   Defines the processing start-date, format 'yyyy-DDD'";
        assertEquals(expected, out.toString().trim());
    }

    @Test
    public void testRunPostProcessing_runWithNetcdfFileWriter() throws Exception {
        final NetcdfFileWriter ncFile = mock(NetcdfFileWriter.class);
        final PostProcessing p1 = mock(PostProcessing.class);
        final PostProcessing p2 = mock(PostProcessing.class);

        PostProcessingTool.run(ncFile, Arrays.asList(p1, p2));

        final InOrder inOrder = inOrder(ncFile, p1, p2);
        inOrder.verify(ncFile, times(1)).setRedefineMode(true);
        inOrder.verify(p1, times(1)).prepare(ncFile);
        inOrder.verify(p2, times(1)).prepare(ncFile);
        inOrder.verify(ncFile, times(1)).setRedefineMode(false);
        inOrder.verify(p1, times(1)).compute(ncFile);
        inOrder.verify(p2, times(1)).compute(ncFile);
        verifyNoMoreInteractions(ncFile, p1, p2);
    }

    @Test
    public void testFilenameIsInTimeRange() throws Exception {
        final String fileStart = "2005-123";
        final String fileEnd = "2005-128";
        final String filename = "AnyCahractersBefore_" + fileStart + "_" + fileEnd + ".nc";

        final long startTime = TimeUtils.parseDOYBeginOfDay(fileStart).getTime();
        final long endTime = TimeUtils.parseDOYEndOfDay(fileEnd).getTime();

        assertTrue(PostProcessingTool.isFileInTimeRange(startTime, endTime, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime +1, endTime, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime, endTime -1, filename));
    }
}
