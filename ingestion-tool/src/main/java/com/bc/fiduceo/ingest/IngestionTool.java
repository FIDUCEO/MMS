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

import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.ServicesUtils;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IngestionTool {

    static String VERSION = "1.0.0";
    private List<Calendar[]> daysIntervalYear;

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

        final Option versionOption = new Option("v", "version", true, "Define the sensor version.");
        options.addOption(versionOption);

        final Option parallelOption = new Option("concurrent", "concurrent-injection", true, "Define the number of concurrent execution.");
        parallelOption.setType(Number.class);
        parallelOption.setArgName("Number");
        options.addOption(parallelOption);

        return options;
    }

    void run(CommandLine commandLine) throws IOException, SQLException {

        final String configValue = commandLine.getOptionValue("config");
        final String sensorType = commandLine.getOptionValue("s");

        final String startTime = commandLine.getOptionValue("start");
        final String endTime = commandLine.getOptionValue("end");

        final String version = commandLine.getOptionValue("v");
        final String concurrent = commandLine.getOptionValue("concurrent");

        //todo mba to implement start and end time for concurrent injection
        if (!(startTime == null && endTime == null)) {
            Date startDate = TimeUtils.parseDOYBeginOfDay(startTime);
            Date endDate = TimeUtils.parseDOYEndOfDay(endTime);
            daysIntervalYear = TimeUtils.getDaysIntervalYear(startDate, endDate, Integer.parseInt(concurrent));
        }


        final File configDirectory = new File(configValue);
        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);

        // @todo 2 tb/tb parametrize geometry factory type 2015-12-16
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        if (!storage.isInitialized()) {
            storage.initialize();
        }

        try {
            ingestMetadata(systemConfig, geometryFactory, storage, sensorType);
        } finally {
            storage.close();
        }
    }

    private void ingestMetadata(SystemConfig systemConfig, GeometryFactory geometryFactory, Storage storage, String sensorType) throws SQLException, IOException {

        // @todo 2 tb/** the wildcard pattern should be supplied by the reader 2015-12-22
        // @todo 2 tb/** extend expression to run recursively through a file tree, write tests for this 2015-12-22
        Geometry geometry;
        ServicesUtils servicesUtils = new ServicesUtils<>();
        Reader reader = (Reader) servicesUtils.getServices(Reader.class, sensorType);
        List<File> searchFilesResult = searchReaderFiles(systemConfig, reader.getRegEx());

        getSplitInputProduct(daysIntervalYear, searchFilesResult);

        for (final File file : searchFilesResult) {
            reader.open(file);
            try {
                final AcquisitionInfo aquisitionInfo = reader.read();

                final SatelliteObservation satelliteObservation = new SatelliteObservation();
                final Sensor sensor = new Sensor();
                sensor.setName(sensorType);
                satelliteObservation.setSensor(sensor);

                satelliteObservation.setStartTime(aquisitionInfo.getSensingStart());
                satelliteObservation.setStopTime(aquisitionInfo.getSensingStop());
                satelliteObservation.setDataFile(file.getAbsoluteFile());

                if (aquisitionInfo.getMultiPolygons() == null) {
                    //todo: mba to specify which Geometry library to use on each reader. 2016-19-02
                    geometry = new GeometryFactory(GeometryFactory.Type.JTS).createPolygon(aquisitionInfo.getCoordinates());
                } else {
                    if (aquisitionInfo.getMultiPolygons().size() > 0) {
                        geometry = geometryFactory.createMultiPolygon(aquisitionInfo.getMultiPolygons());
                    } else {
                        geometry = geometryFactory.createPolygon(aquisitionInfo.getCoordinates());
                    }
                }
                satelliteObservation.setGeoBounds(geometry);
                storage.insert(satelliteObservation);
            } finally {
                reader.close();
            }
        }
    }

    public List<File> searchReaderFiles(SystemConfig systemConfig, String regEx) throws IOException {
        Path start = new File(systemConfig.getArchiveRoot()).toPath();
        FileFinder fileFinder = new FileFinder(regEx);
        Files.walkFileTree(start, fileFinder);
        return fileFinder.getFileList();
    }


    public List<Object[]> getSplitInputProduct(List<Calendar[]> calendars, List<File> searchFilesResult) {
        List<Object[]> fileList = new ArrayList<>();
        for (Calendar[] calendar : calendars) {
            Object[] files = searchFilesResult.stream().sequential().filter(p -> isFileContainBetween(p.getName(), calendar) == true).toArray();
            fileList.add(files);
        }
        return fileList;
    }

    boolean isFileContainBetween(String fileName, Calendar calendar[]) {
        boolean isInBetween = false;
        Calendar startDate = calendar[0];
        int sYearStart = startDate.get(Calendar.YEAR);
        sYearStart = sYearStart > 2000 ? sYearStart - 2000 : sYearStart - 1900;
        int sMonthEnd = startDate.get(Calendar.DAY_OF_YEAR);

        Calendar endDate = calendar[1];
        int eYearStart = endDate.get(Calendar.YEAR);
        eYearStart = eYearStart > 2000 ? eYearStart - 2000 : eYearStart - 1900;

        int eMonthEnd = endDate.get(Calendar.DAY_OF_YEAR);

        Pattern compile = Pattern.compile("'*\\d{5}");
        Matcher matcher = compile.matcher(fileName);
        if (matcher.find()) {
            String group = matcher.group();
            int yr = Integer.parseInt(group.substring(0, 2));
            int month = Integer.parseInt(group.substring(2, group.length()));

            if (yr == sYearStart || yr == eYearStart) {
                if (sMonthEnd <= month && month <= eMonthEnd) {
                    isInBetween = true;
                }
            }
        }
        return isInBetween;
    }

    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("ingestion-tool version " + VERSION);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "ingestion-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    private static class FileFinder extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;
        List<File> fileList = new ArrayList<>();

        public FileFinder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
        }

        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                fileList.add(file.toFile());
            }
        }

        public List<File> getFileList() {
            return fileList;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return FileVisitResult.CONTINUE;
        }
    }
}
