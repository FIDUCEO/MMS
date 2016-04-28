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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;

public class MatchupToolMain {

    public static void main(String[] args) throws ParseException, IOException, SQLException, InvalidRangeException {
        final MatchupTool matchupTool = new MatchupTool();

        if (args.length == 0) {
            matchupTool.printUsageTo(System.err);
            return;
        }

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(MatchupTool.getOptions(), args);
        if (commandLine.hasOption("h") || commandLine.hasOption("--help")) {
            matchupTool.printUsageTo(System.err);
            return;
        }

        try {
            matchupTool.run(commandLine);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
