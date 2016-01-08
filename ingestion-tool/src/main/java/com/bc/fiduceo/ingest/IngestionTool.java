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
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.FilterReaders;
import com.bc.fiduceo.reader.Reader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.io.WildcardMatcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IngestionTool {

    static String VERSION = "1.0.0";

    void run(CommandLine commandLine) throws IOException, SQLException {
        final String configValue = commandLine.getOptionValue("config");
        final String sensorType = commandLine.getOptionValue("s").toUpperCase();
        final String fname = commandLine.getOptionValue("n");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);

        // @todo 2 tb/tb parametrize geometry factory type 2015-12-16
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);

        // @todo 2 tb/tb check if database is already set up. If not, call initialize(). tb 2015-12-22

        try {
            ingestMetadata(systemConfig, geometryFactory, storage, sensorType, fname);
        } finally {
            storage.close();
        }
    }

    private void ingestMetadata(SystemConfig systemConfig, GeometryFactory geometryFactory, Storage storage, String sensorType, String wildcard) throws SQLException, IOException {

        final String archiveRoot = systemConfig.getArchiveRoot();
        // @todo 2 tb/** the wildcard pattern should be supplied by the reader 2015-12-22
        // @todo 2 tb/** extend expression to run recursively through a file tree, write tests for this 2015-12-22
        //@todo 1 mb/** check the wildcard
        final File[] inputFiles = WildcardMatcher.glob(archiveRoot + File.separator + wildcard);
        FilterReaders filterReaders = new FilterReaders();
        Reader reader = filterReaders.getReader(sensorType);
        for (final File file : inputFiles) {
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

                final List<Point> coordinates = aquisitionInfo.getCoordinates();
                satelliteObservation.setGeoBounds(geometryFactory.createPolygon(coordinates));

                storage.insert(satelliteObservation);
            } finally {
                reader.close();
            }
        }


    }

    //todo 1 mb to search easily within a bulk of file with wildcard
    File[] getSensorFiles(String archiveRoot,String sensorType) {
        Pattern pattern = Pattern.compile("'?[A-Z].+[MHSX|AMBX].NK.D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.d5");
        Matcher matcher = pattern.matcher("NSS.MHSX.NK.D15357.S1248.E1419.B9158283");
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = new File(archiveRoot).listFiles();

        for (File file : files) {
            String name = file.getName();
            if (pattern.matcher(name).find()) {
                arrayList.add(file);
            }
        }
        return null;
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

    Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option sensorOption = new Option("s", "sensor", true, "Defines the sensor to be ingested.");
        options.addOption(sensorOption);

        final Option sensorNameOption = new Option("n", "name", true, "Define the name of the product file.");
        options.addOption(sensorNameOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        return options;
    }
}
