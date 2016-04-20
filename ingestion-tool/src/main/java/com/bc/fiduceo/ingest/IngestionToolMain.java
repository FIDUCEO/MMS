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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @todo 2 se/** add class description  2016-02-25
 */
public class IngestionToolMain {

    public static void main(String[] args) throws ParseException, IOException, SQLException {
        final IngestionTool ingestionTool = new IngestionTool();

        if (args.length == 0) {
            ingestionTool.printUsageTo(System.err);
            return;
        }

        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine = parser.parse(IngestionTool.getOptions(), args);
        if (commandLine.hasOption("h") || commandLine.hasOption("--help")) {
            ingestionTool.printUsageTo(System.out);
            return;
        }

        try {
            ingestionTool.run(commandLine);
        } catch (Throwable e) {
            System.exit(-1);
        }
    }
}
