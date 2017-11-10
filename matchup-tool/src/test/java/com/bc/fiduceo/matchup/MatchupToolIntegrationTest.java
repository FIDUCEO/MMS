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
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class MatchupToolIntegrationTest {

    private final String ls = System.lineSeparator();
    private final String expectedPrintUsage = "matchup-tool version 1.3.2" + ls +
            ls +
            "usage: matchup-tool <options>" + ls +
            "Valid options are:" + ls +
            "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
            "   -end,--end-date <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
            "   -h,--help                   Prints the tool usage." + ls +
            "   -start,--start-date <arg>   Defines the processing start-date, format 'yyyy-DDD'" + ls +
            "   -u,--usecase <arg>          Defines the path to the use-case configuration file. Path is relative to the" + ls +
            "                               configuration directory." + ls;

    @Test
    public void testRunMatchup_notInputParameter_printUsageExpected() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String[] args = new String[0];
        final String errOutput = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOutput);
    }

    @Test
    public void testRunMatchup_withHelpParameter_printUsageExpected() throws ParseException, IOException, SQLException, InvalidRangeException {
        String[] args = new String[]{"-h"};
        final String errOut = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOut);

        args = new String[]{"--help"};
        final String errOutput = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOutput);
    }

    private String callMatchupToolMain_wrappedWithSystemErrSpy(String[] args) throws ParseException, IOException, SQLException, InvalidRangeException {
        final PrintStream err = System.err;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);

        try {
            System.setErr(printStream);

            MatchupToolMain.main(args);
        } finally {
            System.setErr(err);
        }
        printStream.close();
        return out.toString();
    }
}
