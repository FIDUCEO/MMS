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
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.math.Intersection;
import com.bc.fiduceo.math.IntersectionEngine;
import com.bc.fiduceo.math.TimeInfo;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class MatchupTool {

    static String VERSION = "1.0.0";
    final Logger logger;

    MatchupTool() {
        logger = FiduceoLogger.getLogger();
    }

    public void run(CommandLine commandLine) throws IOException, SQLException {
        final ToolContext context = initialize(commandLine);

        runMatchupGeneration(context);
    }

    private ToolContext initialize(CommandLine commandLine) throws IOException, SQLException {
        final ToolContext context = new ToolContext();

        final String configValue = commandLine.getOptionValue("config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);
        context.setSystemConfig(systemConfig);

        context.setStartDate(getStartDate(commandLine));
        context.setEndDate(getEndDate(commandLine));

        final UseCaseConfig useCaseConfig = loadUseCaseConfig(commandLine, configDirectory);
        context.setUseCaseConfig(useCaseConfig);

        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());
        context.setGeometryFactory(geometryFactory);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        context.setStorage(storage);
        return context;
    }

    private void runMatchupGeneration(ToolContext context) throws SQLException {
        QueryParameter parameter = getPrimarySensorParameter(context);

        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final int timeDelta = useCaseConfig.getTimeDelta();

        for (final SatelliteObservation primaryObservation : primaryObservations) {
            final Date searchTimeStart = TimeUtils.addSeconds(-timeDelta, primaryObservation.getStartTime());
            final Date searchTimeEnd = TimeUtils.addSeconds(timeDelta, primaryObservation.getStopTime());

            final Geometry geoBounds = primaryObservation.getGeoBounds();
            parameter = getSecondarySensorParameter(useCaseConfig, geoBounds, searchTimeStart, searchTimeEnd);

            final List<SatelliteObservation> secondaryObservations = storage.get(parameter);
            for (final SatelliteObservation secondary : secondaryObservations) {
                final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondary);
                for(final Intersection intersection: intersectingIntervals) {
                    final TimeInfo timeInfo = intersection.getTimeInfo();
                    if (timeInfo.getMinimalTimeDelta() < timeDelta * 1000) {
                        System.out.println("we have an intersection here");
                    }
                }

                //
                // - detect all pixels (x/y) in primary observation that are contained in intersecting area
                // -- for each pixel:
                // --- find closest pixel in secondary observation
                // --- perform check on pixel spatial delta -> remove pixels that are further away
                // --- perform check on pixel time delta -> remove pixels that do not fulfil
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

    // @todo 1 tb/tb write tests 2016-03-07
    static QueryParameter getSecondarySensorParameter(UseCaseConfig useCaseConfig, Geometry geoBounds, Date searchTimeStart, Date searchTimeEnd) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor secondarySensor = getSecondarySensor(useCaseConfig);
        parameter.setSensorName(secondarySensor.getName());
        parameter.setStartTime(searchTimeStart);
        parameter.setStopTime(searchTimeEnd);
        parameter.setGeometry(geoBounds);
        return parameter;
    }

    // @todo 1 tb/tb write tests 2016-03-07
    static Sensor getSecondarySensor(UseCaseConfig useCaseConfig) {
        // @todo 2 tb/tb this is not the optimal way to retriev the secondary sensor. Works for now but needs refactoring when we have insitu matchups 2016-03-07
        final List<Sensor> sensors = useCaseConfig.getSensors();
        Sensor secondarySensor = null;
        for (final Sensor sensor : sensors) {
            if (!sensor.isPrimary()) {
                secondarySensor = sensor;
                break;
            }
        }
        if (secondarySensor == null) {
            throw new RuntimeException("Secondary sensor not configured");
        }
        return secondarySensor;
    }

    // package access for testing only tb 2016-02-23
    static QueryParameter getPrimarySensorParameter(ToolContext context) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor primarySensor = context.getUseCaseConfig().getPrimarySensor();
        if (primarySensor == null) {
            throw new RuntimeException("primary sensor not present in configuration file");
        }
        parameter.setSensorName(primarySensor.getName());
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());
        return parameter;
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

        final Option useCaseOption = new Option("u", "usecase", true, "Defines the path to the use-case configuration file. Path is relative to the configuration directory.");
        options.addOption(useCaseOption);

        return options;
    }

    // package access for testing only tb 2016-02-23
    static Date getEndDate(CommandLine commandLine) {
        final String endDateString = commandLine.getOptionValue("end");
        if (StringUtils.isNullOrEmpty(endDateString)) {
            throw new RuntimeException("cmd-line parameter `end` missing");
        }
        return TimeUtils.parseDOYEndOfDay(endDateString);
    }

    // package access for testing only tb 2016-02-23
    static Date getStartDate(CommandLine commandLine) {
        final String startDateString = commandLine.getOptionValue("start");
        if (StringUtils.isNullOrEmpty(startDateString)) {
            throw new RuntimeException("cmd-line parameter `start` missing");
        }
        return TimeUtils.parseDOYBeginOfDay(startDateString);
    }

    private UseCaseConfig loadUseCaseConfig(CommandLine commandLine, File configDirectory) throws IOException {
        final String usecaseConfigFileName = commandLine.getOptionValue("usecase");
        if (StringUtils.isNullOrEmpty(usecaseConfigFileName)) {
            throw new RuntimeException("Use case configuration file not supplied");
        }

        final File useCaseConfigFile = new File(configDirectory, usecaseConfigFileName);
        if (!useCaseConfigFile.isFile()) {
            throw new RuntimeException("Use case config file does not exist: '" + usecaseConfigFileName + "'");
        }

        final UseCaseConfig useCaseConfig;
        try (FileInputStream inputStream = new FileInputStream(useCaseConfigFile)) {
            useCaseConfig = UseCaseConfig.load(inputStream);
        }

        return useCaseConfig;
    }
}
