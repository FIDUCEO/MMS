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
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.BoundingPolygonCreator;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReadersPlugin;
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
import java.util.List;

class IngestionTool {

    static String VERSION = "1.0.0";

    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option sensorOption = new Option("s", "sensor", true, "Defines the sensor to be ingested.");
        options.addOption(sensorOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        return options;
    }

    void run(CommandLine commandLine) throws IOException, SQLException {
        final String configValue = commandLine.getOptionValue("config");
        final String sensorType = commandLine.getOptionValue("s").toUpperCase();
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);

        // @todo 2 tb/tb parametrize geometry factory type 2015-12-16
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);

        // @todo 2 tb/tb check if database is already set up. If not, call initialize(). tb 2015-12-22

        try {
            ingestMetadata(systemConfig, geometryFactory, storage, sensorType);
        } finally {
            storage.close();
        }
    }

    private void ingestMetadata(SystemConfig systemConfig, GeometryFactory geometryFactory, Storage storage, String sensorType) throws SQLException, IOException {

        // @todo 2 tb/** the wildcard pattern should be supplied by the reader 2015-12-22
        // @todo 2 tb/** extend expression to run recursively through a file tree, write tests for this 2015-12-22
        sensorType = ReadersPlugin.valueOf(sensorType.toUpperCase().trim().replace('-', '_')).getType();
        ServicesUtils servicesUtils = new ServicesUtils<>();
        Reader reader = (Reader) servicesUtils.getServices(Reader.class, sensorType);

        List<File> searchFilesResult = searchReaderFiles(systemConfig, reader.getRegEx());

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

                MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(aquisitionInfo.getMultiPolygons());
                String multiPolygon1 = BoundingPolygonCreator.plotMultiPolygon((List<Polygon>) multiPolygon.getInner());
                satelliteObservation.setWellknowText(multiPolygon1);

                Geometry parse = geometryFactory.parse(multiPolygon1);
                satelliteObservation.setGeoBounds(parse);
                storage.insert(satelliteObservation);
            } finally {
                reader.close();
            }
        }
    }

    List<File> searchReaderFiles(SystemConfig systemConfig, String regEx) throws IOException {
        Path start = new File(systemConfig.getArchiveRoot()).toPath();
        FileFinder fileFinder = new FileFinder(regEx);
        Files.walkFileTree(start, fileFinder);
        return fileFinder.getFileList();
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
