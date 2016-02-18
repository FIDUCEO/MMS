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


import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

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

        assertEquals("matchup-tool version 1.0.0" + ls + ls+
                "usage: matchup-tool <options>" + ls +
                "Valid options are:" + ls +
                "   -c,--config <arg>   Defines the configuration directory. Defaults to './config'." + ls +
                "   -e,--end <arg>      Defines the processing end-date, format 'yyyy-DDD'" + ls +
                "   -h,--help           Prints the tool usage." + ls +
                "   -s,--start <arg>    Defines the processing start-date, format 'yyyy-DDD'" + ls, outputStream.toString());
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
    }
}
