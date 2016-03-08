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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchupToolTest {

    private String ls;

    @Before
    public void SetUp() {
        ls = System.lineSeparator();
    }

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final MatchupTool matchupTool = new MatchupTool();

        matchupTool.printUsageTo(outputStream);

        assertEquals("matchup-tool version 1.0.0" + ls + ls +
                "usage: matchup-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>    Defines the configuration directory. Defaults to './config'." + ls +
                "   -e,--end <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
                "   -h,--help            Prints the tool usage." + ls +
                "   -s,--start <arg>     Defines the processing start-date, format 'yyyy-DDD'" + ls +
                "   -u,--usecase <arg>   Defines the path to the use-case configuration file. Path is relative to the configuration" + ls +
                "                        directory." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final Options options = MatchupTool.getOptions();
        assertNotNull(options);

        final Option helpOption = options.getOption("h");
        assertNotNull(helpOption);
        assertEquals("h", helpOption.getOpt());
        assertEquals("help", helpOption.getLongOpt());
        assertEquals("Prints the tool usage.", helpOption.getDescription());
        assertFalse(helpOption.hasArg());

        final Option configOption = options.getOption("config");
        assertNotNull(configOption);
        assertEquals("c", configOption.getOpt());
        assertEquals("config", configOption.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", configOption.getDescription());
        assertTrue(configOption.hasArg());

        final Option startOption = options.getOption("start");
        assertNotNull(startOption);
        assertEquals("s", startOption.getOpt());
        assertEquals("start", startOption.getLongOpt());
        assertEquals("Defines the processing start-date, format 'yyyy-DDD'", startOption.getDescription());
        assertTrue(startOption.hasArg());

        final Option endOption = options.getOption("end");
        assertNotNull(endOption);
        assertEquals("e", endOption.getOpt());
        assertEquals("end", endOption.getLongOpt());
        assertEquals("Defines the processing end-date, format 'yyyy-DDD'", endOption.getDescription());
        assertTrue(endOption.hasArg());

        final Option useCaseOption = options.getOption("usecase");
        assertNotNull(useCaseOption);
        assertEquals("u", useCaseOption.getOpt());
        assertEquals("usecase", useCaseOption.getLongOpt());
        assertEquals("Defines the path to the use-case configuration file. Path is relative to the configuration directory.", useCaseOption.getDescription());
        assertTrue(useCaseOption.hasArg());
    }

    @Test
    public void testGetEndDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("end")).thenReturn("1998-345");

        final Date endDate = MatchupTool.getEndDate(commandLine);
        TestUtil.assertCorrectUTCDate(1998, 12, 11, 23, 59, 59, 999, endDate);
    }

    @Test
    public void testGetEndDate_missingValue() {
        final CommandLine commandLine = mock(CommandLine.class);

        try {
            MatchupTool.getEndDate(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetStartDate() {
        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("start")).thenReturn("1999-346");

        final Date startDate = MatchupTool.getStartDate(commandLine);
        TestUtil.assertCorrectUTCDate(1999, 12, 12, 0, 0, 0, 0, startDate);
    }

    @Test
    public void testGetStartDate_missingValue() {
        final CommandLine commandLine = mock(CommandLine.class);

        try {
            MatchupTool.getStartDate(commandLine);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testGetPrimarySensorParameter() {
        final MatchupToolContext context = new MatchupToolContext();
        context.setStartDate(TimeUtils.parseDOYBeginOfDay("2002-23"));
        context.setEndDate(TimeUtils.parseDOYEndOfDay("2002-23"));

        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensor.setPrimary(true);
        sensorList.add(sensor);
        useCaseConfig.setSensors(sensorList);
        context.setUseCaseConfig(useCaseConfig);

        final QueryParameter parameter = MatchupTool.getPrimarySensorParameter(context);
        assertNotNull(parameter);
        assertEquals("amsub-n16", parameter.getSensorName());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 0, 0, 0, 0, parameter.getStartTime());
        TestUtil.assertCorrectUTCDate(2002, 1, 23, 23, 59, 59, 999, parameter.getStopTime());
    }

    @Test
    public void testGetPrimarySensorParameter_missingPrimarySensor() {
        final MatchupToolContext context = new MatchupToolContext();

        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor sensor = new Sensor("amsub-n16");
        sensorList.add(sensor);
        useCaseConfig.setSensors(sensorList);
        context.setUseCaseConfig(useCaseConfig);

        try {
            MatchupTool.getPrimarySensorParameter(context);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }
}
