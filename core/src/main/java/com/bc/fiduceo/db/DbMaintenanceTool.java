package com.bc.fiduceo.db;

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.log.FiduceoLogger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;

class DbMaintenanceTool {

    private static final int PAGE_SIZE = 512;
    private final Logger logger;
    private Storage storage;
    private PathAccumulator accumulator;

    DbMaintenanceTool() {
        this.logger = FiduceoLogger.getLogger();
    }

    // package access for testing only tb 2019-03-29
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option dryRunOption = new Option("d", "dryrun", false, "Defines 'dryrun' status, i.e. just test the replacement and report problems.");
        options.addOption(dryRunOption);

        final Option pathOption = new Option("p", "path", true, "Observation path segment to be replaced or truncated.");
        options.addOption(pathOption);

        final Option replaceOption = new Option("r", "replace", true, "Observation path segment replacement.");
        options.addOption(replaceOption);

        final Option truncateOption = new Option("t", "truncate", false, "Command to truncate path segment.");
        options.addOption(truncateOption);

        final Option segmentsOption = new Option("s", "segments", true, "Number of segments to consider for paths missing the search expression (default: 4)");
        options.addOption(segmentsOption);

        return options;
    }

    // package access for testing only tb 2019-03-29
    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("db-maintenance-tool version " + VERSION_NUMBER);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "db_maintenance <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    // package access for testing only tb 2019-03-29
    void run(CommandLine commandLine) throws IOException, SQLException {
        logger.info("Start db-maintenance-tool version '" + VERSION_NUMBER + "'");

        initialize(commandLine);

        final String oldPathSegment = commandLine.getOptionValue("path");
        final String newPathSegment = commandLine.getOptionValue("replace");

        final QueryParameter queryParameter = new QueryParameter();
        queryParameter.setOffset(0);
        queryParameter.setPageSize(PAGE_SIZE);

        boolean dryrun = commandLine.hasOption("dryrun");

        if (dryrun) {
            int numPathSegments = 4;
            final boolean segments = commandLine.hasOption("segments");
            if (segments) {
                final String numSegmentsString = commandLine.getOptionValue("segments");
                numPathSegments = Integer.parseInt(numSegmentsString);
            }
            logger.info("Dryrun checking paths  old: " + oldPathSegment + "  new: " + newPathSegment);
            accumulator = new PathAccumulator(oldPathSegment, numPathSegments);
            executeDryrun(oldPathSegment, queryParameter);
        } else {
            final boolean truncate = commandLine.hasOption("truncate");

            if (truncate) {
                logger.info("Removing path segment: " + oldPathSegment);
                processTruncate(oldPathSegment, queryParameter);
            } else {
                logger.info("Replacing paths - old: " + oldPathSegment + "  new: " + newPathSegment);
                processUpdate(oldPathSegment, newPathSegment, queryParameter);
            }
        }
    }

    private void executeDryrun(String oldPathSegment, QueryParameter queryParameter) throws SQLException {
        int total_count = 0;

        List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
        while (satelliteObservations.size() > 0) {
            checkPaths(oldPathSegment, satelliteObservations);

            total_count += satelliteObservations.size();
            logger.info("processed " + total_count + " datasets");

            final int newOffset = queryParameter.getOffset() + PAGE_SIZE;
            queryParameter.setOffset(newOffset);
            satelliteObservations = storage.get(queryParameter);
        }

        System.out.println("Datasets checked: " + total_count);

        final PathCount matches = accumulator.getMatches();
        System.out.println("Datasets ok to convert: " + matches.count);

        final List<PathCount> misses = accumulator.getMisses();
        if (misses.size() > 0) {
            System.out.println("Datasets with deviating path:");
            for (final PathCount miss : misses) {
                System.out.println("- " + miss.getPath() + ": " + miss.getCount());
            }
        }
    }

    private void processTruncate(String oldPathSegment, QueryParameter queryParameter) throws SQLException {
        try {
            int total_count = 0;

            List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
            while (satelliteObservations.size() > 0) {
                truncatePaths(oldPathSegment, satelliteObservations);

                total_count += satelliteObservations.size();
                logger.info("processed " + total_count + " datasets");

                final int newOffset = queryParameter.getOffset() + PAGE_SIZE;
                queryParameter.setOffset(newOffset);
                satelliteObservations = storage.get(queryParameter);
            }
        } finally {
            cleanup();
        }
    }

    private void processUpdate(String oldPathSegment, String newPathSegment, QueryParameter queryParameter) throws SQLException {
        try {
            int total_count = 0;

            List<SatelliteObservation> satelliteObservations = storage.get(queryParameter);
            while (satelliteObservations.size() > 0) {
                updatePaths(oldPathSegment, newPathSegment, satelliteObservations);

                total_count += satelliteObservations.size();
                logger.info("processed " + total_count + " datasets");

                final int newOffset = queryParameter.getOffset() + PAGE_SIZE;
                queryParameter.setOffset(newOffset);
                satelliteObservations = storage.get(queryParameter);
            }
        } finally {
            cleanup();
        }
    }

    private void truncatePaths(String oldPathSegment, List<SatelliteObservation> satelliteObservations) throws SQLException {
        AbstractBatch batch = null;

        for (final SatelliteObservation observation : satelliteObservations) {
            final String oldPath = observation.getDataFilePath().toString();
            final int start = oldPath.indexOf(oldPath);
            if (start >= 0) {
                final String newPath = StringUtils.remove(oldPath, oldPathSegment);
                batch = storage.updatePathBatch(observation, newPath, batch);
            }
        }

        if (batch != null) {
            storage.commitBatch(batch);
        }
    }

    private void updatePaths(String oldPathSegment, String newPathSegment, List<SatelliteObservation> satelliteObservations) throws SQLException {
        AbstractBatch batch = null;

        for (final SatelliteObservation observation : satelliteObservations) {
            final String oldPath = observation.getDataFilePath().toString();
            if (oldPath.contains(oldPathSegment)) {
                final String newPath = oldPath.replace(oldPathSegment, newPathSegment);
                batch = storage.updatePathBatch(observation, newPath, batch);
            }
        }

        if (batch != null) {
            storage.commitBatch(batch);
        }
    }

    private void checkPaths(String oldPathSegment, List<SatelliteObservation> satelliteObservations) {
        for (final SatelliteObservation observation : satelliteObservations) {
            final String oldPath = observation.getDataFilePath().toString();
            if (oldPath.contains(oldPathSegment)) {
                accumulator.addMatch();
            } else {
                accumulator.addMiss(oldPath);
            }
        }
    }

    private void cleanup() throws SQLException {
        if (storage != null) {
            storage.close();
        }
    }

    private void initialize(CommandLine commandLine) throws IOException, SQLException {
        final String configDirPath = commandLine.getOptionValue("config");
        final Path confDirPath = Paths.get(configDirPath);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(confDirPath.toFile());

        final SystemConfig systemConfig = SystemConfig.loadFrom(confDirPath.toFile());
        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());

        storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        if (!storage.isInitialized()) {
            storage.initialize();
        }
    }
}
