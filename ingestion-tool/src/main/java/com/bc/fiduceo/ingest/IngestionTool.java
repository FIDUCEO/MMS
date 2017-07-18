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

import com.bc.fiduceo.archive.Archive;
import com.bc.fiduceo.archive.ArchiveConfig;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;

class IngestionTool {

    private final Logger logger;

    IngestionTool() {
        this.logger = FiduceoLogger.getLogger();
    }

    void run(CommandLine commandLine) throws IOException, SQLException {
        logger.info("Start ingestion, Ingestion Tool version '" + VERSION_NUMBER + "'");

        final String configDirPath = commandLine.getOptionValue("config");
        final Path confDirPath = Paths.get(configDirPath);

        final String sensorType = commandLine.getOptionValue("s");
        final String processingVersion = commandLine.getOptionValue("v");

        final ToolContext context = initializeContext(commandLine, confDirPath);
        logger.info("Successfully initialized tool");

        try {
            ingestMetadata(context, sensorType, processingVersion);
        } finally {
            context.getStorage().close();
        }
    }

    private void ingestMetadata(ToolContext context, String sensorType, String processingVersion) throws SQLException, IOException {
        final ReaderFactory readerFactory = context.getReaderFactory();
        final Reader reader = readerFactory.getReader(sensorType);

        final Pattern pattern = getPattern(reader);
        final Storage storage = context.getStorage();

        final QueryParameter queryParameter = new QueryParameter();

        final SystemConfig systemConfig = context.getSystemConfig();
        final ArchiveConfig archiveConfig = systemConfig.getArchiveConfig();
        final Archive archive = new Archive(archiveConfig);
        final Date startDate = context.getStartDate();
        final Date endDate = context.getEndDate();

        final Path[] productPaths = archive.get(startDate, endDate, processingVersion, sensorType);
        for (final Path filePath : productPaths) {
            final Matcher matcher = getMatcher(filePath, pattern);
            final String dataFilePath = filePath.toString();
            if (!matcher.matches()) {
                logger.warning("The file '" + dataFilePath + "' does not follow the file naming pattern. Skipping");
                continue;
            }

            queryParameter.setPath(dataFilePath);
            final List<SatelliteObservation> observations = storage.get(queryParameter);
            if (observations.size() > 0) {
                logger.info("The file '" + dataFilePath + "' is already registered to the database. Skipping");
                continue;
            }

            logger.info("registering '" + dataFilePath + "' ...");

            try {
                reader.open(filePath.toFile());
                final AcquisitionInfo acquisitionInfo = reader.read();

                final SatelliteObservation satelliteObservation = new SatelliteObservation();
                satelliteObservation.setSensor(new Sensor(sensorType));
                satelliteObservation.setStartTime(acquisitionInfo.getSensingStart());
                satelliteObservation.setStopTime(acquisitionInfo.getSensingStop());
                satelliteObservation.setDataFilePath(dataFilePath);
                satelliteObservation.setGeoBounds(acquisitionInfo.getBoundingGeometry());
                satelliteObservation.setTimeAxes(acquisitionInfo.getTimeAxes());
                satelliteObservation.setNodeType(acquisitionInfo.getNodeType());
                satelliteObservation.setVersion(processingVersion);
                storage.insert(satelliteObservation);
            } catch (Exception e) {
                logger.severe("Unable to register the file '" + dataFilePath + "'");
                logger.severe("Cause: " + e.getMessage());
                e.printStackTrace();
                continue;
            } finally {
                reader.close();
            }
            logger.info("success");
        }
    }

    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("ingestion-tool version " + VERSION_NUMBER);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "ingestion-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    // package access for testing only tb 2016-03-14
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option sensorOption = new Option("s", "sensor", true, "Defines the sensor to be ingested.");
        options.addOption(sensorOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option startOption = new Option("start", "start-time", true, "Define the starting time of products to inject.");
        startOption.setArgName("Date");
        options.addOption(startOption);

        final Option endOption = new Option("end", "end-time", true, "Define the ending time of products to inject.");
        endOption.setArgName("Date");
        options.addOption(endOption);

        final Option versionOption = new Option("v", "version", true, "Define the sensor data processing version.");
        options.addOption(versionOption);

        return options;
    }

    // package access for testing only tb 2016-03-14
    static Pattern getPattern(Reader reader) {
        final String regEx = reader.getRegEx();
        return Pattern.compile(regEx);
    }

    // package access for testing only tb 2016-03-14
    static Matcher getMatcher(Path filePath, Pattern pattern) {
        final String fileName = filePath.getFileName().toString();
        return pattern.matcher(fileName);
    }

    // package access for testing only tb 2017-07-18
    static ToolContext initializeContext(CommandLine commandLine, Path confDirPath) throws IOException, SQLException {
        final ToolContext context = new ToolContext();

        setStartDate(commandLine, context);
        setEndDate(commandLine, context);

        verifyDates(context);

        final SystemConfig systemConfig = SystemConfig.loadFrom(confDirPath.toFile());
        context.setSystemConfig(systemConfig);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(confDirPath.toFile());

        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());
        context.setGeometryFactory(geometryFactory);

        final ReaderFactory readerFactory = ReaderFactory.get(geometryFactory);
        context.setReaderFactory(readerFactory);

        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        if (!storage.isInitialized()) {
            storage.initialize();
        }
        context.setStorage(storage);
        return context;
    }

    // package access for testing only tb 2017-07-18
    static void verifyDates(ToolContext context) {
        if (context.getEndDate().before(context.getStartDate())) {
            throw new RuntimeException("End date before start date");
        }
    }

    // package access for testing only tb 2017-07-18
    static void setEndDate(CommandLine commandLine, ToolContext context) {
        final String endTime = commandLine.getOptionValue("end");
        if (StringUtils.isNotNullAndNotEmpty(endTime)) {
            final Date endDate = TimeUtils.parse(endTime, "yyyy-DDD");
            context.setEndDate(endDate);
        } else {
            throw new RuntimeException("End date parameter missing");
        }
    }

    // package access for testing only tb 2017-07-18
    static void setStartDate(CommandLine commandLine, ToolContext context) {
        final String startTime = commandLine.getOptionValue("start");
        if (StringUtils.isNotNullAndNotEmpty(startTime)) {
            final Date startDate = TimeUtils.parse(startTime, "yyyy-DDD");
            context.setStartDate(startDate);
        } else {
            throw new RuntimeException("Start date parameter missing");
        }
    }
}
