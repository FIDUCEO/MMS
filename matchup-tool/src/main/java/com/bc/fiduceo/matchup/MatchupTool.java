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

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

class MatchupTool {

    static String VERSION = "1.0.0";

    public void run(CommandLine commandLine) throws IOException, SQLException {
        final MatchupToolContext context = initialize(commandLine);

        runMatchupGeneration(context);

        // input required:
        // - primary sensor
        // - secondary sensor (optional)
        // - insitu type (optional)
        // - start time (year/doy) (yyyy-DDD)
        // - end time (year/doy)
    }

    private MatchupToolContext initialize(CommandLine commandLine) throws IOException, SQLException {
        final MatchupToolContext context = new MatchupToolContext();
        final String configValue = commandLine.getOptionValue("config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);

        final String startDateString = commandLine.getOptionValue("start");
        if (StringUtils.isNullOrEmpty(startDateString)) {
            throw new RuntimeException("cmd-line parameter `start` missing");
        }
        final Date startDate = TimeUtils.parseDOYBeginOfDay(startDateString);
        context.setStartDate(startDate);

        final String endDateString = commandLine.getOptionValue("end");
        if (StringUtils.isNullOrEmpty(endDateString)) {
            throw new RuntimeException("cmd-line parameter `end` missing");
        }
        final Date endDate = TimeUtils.parseDOYEndOfDay(endDateString);
        context.setEndDate(endDate);

        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        context.setStorage(storage);
        return context;
    }

    private void runMatchupGeneration(MatchupToolContext context) throws SQLException {
        final QueryParameter parameter = new QueryParameter();
        // @todo 2 tb/tb sensor/platform from use-case configuration 2016-02-19
        parameter.setSensorName("amsub-noaa15");
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        // @todo 2 tb/tb time delta shall be read from use-case configuration 2016-02-19
        final int timeDelta = 300;
        for (final SatelliteObservation observation : primaryObservations) {
            final Date searchTimeStart = TimeUtils.addSeconds(-timeDelta, observation.getStartTime());
            final Date searchTimeEnd = TimeUtils.addSeconds(timeDelta, observation.getStopTime());

            // @todo 2 tb/tb sensor/platform from use-case configuration 2016-02-19
            parameter.setSensorName("mhs-noaa15");
            parameter.setStartTime(searchTimeStart);
            parameter.setStopTime(searchTimeEnd);

            final List<SatelliteObservation> secondaryObservations = storage.get(parameter);
            for (final SatelliteObservation secondary : secondaryObservations) {
                // @todo 1 tb/tb perform the intersection 2016-02-19
                // - calculate intersecting area (in lon/lat) - polygon.
                // -- if no polygon or empty -> continue
                //
                // - detect overlapping time interval from time axes
                // -- if not withing required time delta -> continue
                //
                // - detect all pixels (x/y) in primary observation that are contained in intersecting area
                // -- for each pixel:
                // --- find closest pixel in secondary observation
                // --- perform check on pixel time delta -> remove pixels that do not fulfil
                // --- perform check on pixel spatial delta -> remove pixels that are further away
                // --- perform check for observation angles (optional) -> remove pixels where constraint is not fulfilled
                // --- perform cloud processing (optional) -> remove pixels or add flags
                //
                // - if pixels are left: create output file
                // - for all remaining pixels:
                // -- extract pixel window for all bands and write to output (primary and secondary observation)
                // -- store metadata of each sensor-acquisition as described in use-case
                //
                //

            }
        }
    }

    // package access for testing only tb 2016-02-18
    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("matchup-tool version " + VERSION);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "matchup-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    // package access for testing only tb 2016-02-18
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option startOption = new Option("s", "start", true, "Defines the processing start-date, format 'yyyy-DDD'");
        options.addOption(startOption);

        final Option endOption = new Option("e", "end", true, "Defines the processing end-date, format 'yyyy-DDD'");
        options.addOption(endOption);

        return options;
    }
}
