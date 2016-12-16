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

import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.post.plugin.DummyPostProcessingPlugin;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hamcrest.CoreMatchers;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.*;
import org.mockito.InOrder;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Formatter;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class PostProcessingToolTest {

    private static final String CONFIG = PostProcessingConfig.TAG_NAME_ROOT;
    private static final String PROCESSINGS = PostProcessingConfig.TAG_NAME_POST_PROCESSINGS;
    private static final String NEW_FILES = PostProcessingConfig.TAG_NAME_NEW_FILES;
    private static final String OUTPUT_DIR = PostProcessingConfig.TAG_NAME_OUTPUT_DIR;

    private static final String DUMMY_NAME = DummyPostProcessingPlugin.DUMMY_POST_PROCESSING_NAME;
    private Element root;

    @Before
    public void setUp() throws Exception {
        root = new Element(CONFIG).addContent(Arrays.asList(
                    new Element(NEW_FILES).addContent(
                                new Element(OUTPUT_DIR).addContent("An_Output_Directory")),
                    new Element(PROCESSINGS).addContent(
                                new Element(DUMMY_NAME).addContent("C")
                    )
        ));
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


        o = options.getOption("i");
        assertNotNull(o);
        assertEquals("input-dir", o.getLongOpt());
        assertEquals("Defines the path to the input mmd files directory.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());

        o = options.getOption("end");
        assertNotNull(o);
        assertEquals("end-date", o.getLongOpt());
        assertEquals("Defines the processing end-date, format 'yyyy-DDD'. DDD = Day of year.", o.getDescription());
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
        assertEquals("start-date", o.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'. DDD = Day of year.", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());
    }

    @Test
    public void testPrintUsage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        PostProcessingTool.printUsageTo(out);
//        PostProcessingTool.printUsageTo(System.out);

        final String ls = System.lineSeparator();
        final String expected = "post-processing-tool version 1.1.2" + ls +
                                "" + ls +
                                "usage: post-processing-tool <options>" + ls +
                                "Valid options are:" + ls +
                                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                                "   -end,--end-date <arg>       Defines the processing end-date, format 'yyyy-DDD'. DDD = Day of year." + ls +
                                "   -h,--help                   Prints the tool usage." + ls +
                                "   -i,--input-dir <arg>        Defines the path to the input mmd files directory." + ls +
                                "   -j,--job-config <arg>       Defines the path to post processing job configuration file. Path is relative to the" + ls +
                                "                               configuration directory." + ls +
                                "   -start,--start-date <arg>   Defines the processing start-date, format 'yyyy-DDD'. DDD = Day of year.";
        assertEquals(expected, out.toString().trim());
    }

    @Test
    public void testRun() throws Exception {
        final Group rootGroup = mock(Group.class);
        when(rootGroup.getShortName()).thenReturn("root");

        final NetcdfFile reader = mock(NetcdfFile.class);
        when(reader.getRootGroup()).thenReturn(rootGroup);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final PostProcessing p1 = mock(PostProcessing.class);
        final PostProcessing p2 = mock(PostProcessing.class);

        PostProcessingTool.run(reader, writer, Arrays.asList(p1, p2));

        final InOrder inOrder = inOrder(reader, writer, p1, p2);
        inOrder.verify(writer, times(1)).addGroup(null, "root");
        inOrder.verify(p1, times(1)).prepare(reader, writer);
        inOrder.verify(p2, times(1)).prepare(reader, writer);
        inOrder.verify(writer, times(1)).create();
        inOrder.verify(p1, times(1)).compute(same(reader), same(writer));
        inOrder.verify(p2, times(1)).compute(same(reader), same(writer));
        verifyNoMoreInteractions(writer, p1, p2);
    }

    @Test
    public void testFilenameIsInTimeRange() throws Exception {
        final String fileStart = "2005-123";
        final String fileEnd = "2005-128";
        final String filename = "AnyCahractersBefore_" + fileStart + "_" + fileEnd + ".nc";

        final long startTime = TimeUtils.parseDOYBeginOfDay(fileStart).getTime();
        final long endTime = TimeUtils.parseDOYEndOfDay(fileEnd).getTime();

        assertTrue(PostProcessingTool.isFileInTimeRange(startTime, endTime, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime + 1, endTime, filename));
        assertFalse(PostProcessingTool.isFileInTimeRange(startTime, endTime - 1, filename));
    }

    @Test
    public void testGetDate() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("TheDateString");

        final String start = PostProcessingTool.getDate(commandLine, "start");

        assertEquals("TheDateString", start);
    }

    @Test
    public void testGetDate_emptyString() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("");

        try {
            PostProcessingTool.getDate(commandLine, "start");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of cmd-line parameter 'start' is missing.", expected.getMessage());
        }
    }

    @Test
    public void testGetDate_null() throws Exception {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn(null);

        try {
            PostProcessingTool.getDate(commandLine, "start");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Value of cmd-line parameter 'start' is missing.", expected.getMessage());
        }
    }

    @Test
    public void testThatPostProcessingToolInCaseOfExceptionsPrintsTheErrorMessageAndContinueWithTheNextFile() throws Exception {
        final ArrayList<Path> mmdFiles = new ArrayList<>();
        mmdFiles.add(Paths.get("nonExistingFileOne"));
        mmdFiles.add(Paths.get("nonExistingFileTwo"));
        mmdFiles.add(Paths.get("nonExistingFileThree"));

        final Formatter formatter = new SimpleFormatter();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final StreamHandler handler = new StreamHandler(stream, formatter);
        final Logger logger = FiduceoLogger.getLogger();

        try {
            logger.addHandler(handler);

            final PostProcessingContext context = new PostProcessingContext();
            final PostProcessingConfig processingConfig = getConfig();
            context.setProcessingConfig(processingConfig);

            final PostProcessingTool postProcessingTool = new PostProcessingTool();
            postProcessingTool.context = context;

            postProcessingTool.computeFiles(mmdFiles);

            handler.close();
            final String string = stream.toString();
            assertThat(string, CoreMatchers.containsString("nonExistingFileOne"));
            assertThat(string, CoreMatchers.containsString("nonExistingFileTwo"));
            assertThat(string, CoreMatchers.containsString("nonExistingFileThree"));
        } finally {
            logger.removeHandler(handler);
        }
    }

    private PostProcessingConfig getConfig() throws Exception {
        final Document document = new Document(root);

        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        new XMLOutputter(Format.getPrettyFormat()).output(document, bs);
        bs.close();

        return PostProcessingConfig.load(new ByteArrayInputStream(bs.toByteArray()));
    }
}
