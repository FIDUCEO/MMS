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

package com.bc.fiduceo.ingest;

import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.tool.ToolContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IngestionToolTest {

    private String ls;
    private IngestionTool ingestionTool;

    @Before
    public void SetUp() {
        ls = System.lineSeparator();
        ingestionTool = new IngestionTool();
    }

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ingestionTool.printUsageTo(outputStream);

        assertEquals("ingestion-tool version 1.4.8" + ls +
                ls +
                "usage: ingestion-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>            Defines the configuration directory. Defaults to './config'." + ls +
                "   -end,--end-time <Date>       Define the ending time of products to inject." + ls +
                "   -h,--help                    Prints the tool usage." + ls +
                "   -s,--sensor <arg>            Defines the sensor to be ingested." + ls +
                "   -start,--start-time <Date>   Define the starting time of products to inject." + ls +
                "   -v,--version <arg>           Define the sensor data processing version." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final Options options = IngestionTool.getOptions();
        assertNotNull(options);

        final Option helpOption = options.getOption("h");
        assertNotNull(helpOption);
        assertEquals("h", helpOption.getOpt());
        assertEquals("help", helpOption.getLongOpt());
        assertEquals("Prints the tool usage.", helpOption.getDescription());
        assertFalse(helpOption.hasArg());

        final Option sensorOption = options.getOption("sensor");
        assertNotNull(sensorOption);
        assertEquals("s", sensorOption.getOpt());
        assertEquals("sensor", sensorOption.getLongOpt());
        assertEquals("Defines the sensor to be ingested.", sensorOption.getDescription());
        assertTrue(sensorOption.hasArg());

        final Option configOption = options.getOption("config");
        assertNotNull(configOption);
        assertEquals("c", configOption.getOpt());
        assertEquals("config", configOption.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", configOption.getDescription());
        assertTrue(configOption.hasArg());

        final Option startTime = options.getOption("start-time");
        assertNotNull(startTime);
        assertEquals("start", startTime.getOpt());
        assertEquals("start-time", startTime.getLongOpt());
        assertEquals("Define the starting time of products to inject.", startTime.getDescription());
        assertTrue(startTime.hasArg());


        final Option endTime = options.getOption("end-time");
        assertNotNull(endTime);
        assertEquals("end", endTime.getOpt());
        assertEquals("end-time", endTime.getLongOpt());
        assertEquals("Define the ending time of products to inject.", endTime.getDescription());
        assertTrue(endTime.hasArg());


        final Option version = options.getOption("version");
        assertNotNull(version);
        assertEquals("v", version.getOpt());
        assertEquals("version", version.getLongOpt());
        assertEquals("Define the sensor data processing version.", version.getDescription());
        assertTrue(version.hasArg());
    }

    @Test
    public void testGetPattern() {
        final Reader reader = mock(Reader.class);
        when(reader.getRegEx()).thenReturn("p*q");

        final Pattern pattern = IngestionTool.getPattern(reader);
        assertEquals("p*q", pattern.toString());
    }

    @Test
    public void testGetMatcher() {
        final Path path = mock(Path.class);
        final Path fileName = mock(Path.class);
        when(fileName.toString()).thenReturn("2345.nc");
        when(path.getFileName()).thenReturn(fileName);

        Pattern pattern = Pattern.compile("[0-9]{4}.nc");
        Matcher matcher = IngestionTool.getMatcher(path, pattern);
        assertTrue(matcher.matches());

        pattern = Pattern.compile("\\d.nc");
        matcher = IngestionTool.getMatcher(path, pattern);
        assertFalse(matcher.matches());
    }

    @Test
    public void testSetStartDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("2007-126");

        final ToolContext context = new ToolContext();

        IngestionTool.setStartDate(commandLine, context);

        assertEquals(1178409600000L, context.getStartDate().getTime());
    }

    @Test
    public void testSetStartDate_notSet() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("");

        final ToolContext context = new ToolContext();

        try {
            IngestionTool.setStartDate(commandLine, context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testSetEndDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("end")).thenReturn("2007-127");

        final ToolContext context = new ToolContext();

        IngestionTool.setEndDate(commandLine, context);

        assertEquals(1178496000000L, context.getEndDate().getTime());
    }

    @Test
    public void testSetEndDate_notSet() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("end")).thenReturn(null);

        final ToolContext context = new ToolContext();

        try {
            IngestionTool.setEndDate(commandLine, context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testVerifyDates_valid() {
        final ToolContext context = new ToolContext();
        context.setStartDate(new Date(10000000));
        context.setEndDate(new Date(11000000));

        IngestionTool.verifyDates(context);
        // expect nothing to happen tb 2017-07-18
    }

    @Test
    public void testVerifyDates_invalid() {
        final ToolContext context = new ToolContext();
        context.setStartDate(new Date(10000000));
        context.setEndDate(new Date(9000000));

        try {
            IngestionTool.verifyDates(context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
