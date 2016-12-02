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

import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;
import org.mockito.InOrder;
import ucar.nc2.NetcdfFileWriter;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostProcessingToolTest {

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
        assertEquals("start-date", o.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'", o.getDescription());
        assertEquals(true, o.hasArg());
        assertEquals(true, o.isRequired());
    }

    @Test
    public void testPrintUsage() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        PostProcessingTool.printUsageTo(out);

        final String ls = System.lineSeparator();
        final String expected = "post-processing-tool version 1.1.1-SNAPSHOT" + ls +
                "" + ls +
                "usage: post-processing-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
                "   -end,--end-date <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
                "   -h,--help                   Prints the tool usage." + ls +
                "   -i,--input-dir <arg>        Defines the path to the input mmd files directory." + ls +
                "   -j,--job-config <arg>       Defines the path to post processing job configuration file. Path is relative to the" + ls +
                "                               configuration directory." + ls +
                "   -start,--start-date <arg>   Defines the processing start-date, format 'yyyy-DDD'";
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
}
