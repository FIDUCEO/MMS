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


import com.bc.fiduceo.log.FiduceoLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class PostProcessingToolMain {

    public static void main(String[] args) throws ParseException {
        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(PostProcessingTool.getOptions(), args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            PostProcessingTool.printUsageTo(System.err);
            return;
        }
        if (commandLine.hasOption("h") || commandLine.hasOption("--help")) {
            PostProcessingTool.printUsageTo(System.out);
            return;
        }

        final PostProcessingTool postProcessingTool = new PostProcessingTool();

        try {
            postProcessingTool.run(commandLine);
        } catch (Throwable e) {
            FiduceoLogger.getLogger().severe(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
