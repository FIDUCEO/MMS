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
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

class IngestionTool {

    static String VERSION = "1.0.0";

    void run(CommandLine commandLine) throws IOException, SQLException {
        final String configValue = commandLine.getOptionValue("config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        if (!new File(configDirectory, "system.properties").isFile()) {
            throw new RuntimeException("implement stuff here");
        }

        // @todo 2 tb/tb parametrize geometry factory type 2015-12-16
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.JTS);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);

        final SatelliteObservation satelliteObservation = new SatelliteObservation();
        final Sensor sensor = new Sensor();
        sensor.setName("replace me");
        satelliteObservation.setSensor(sensor);
        satelliteObservation.setStartTime(new Date());
        satelliteObservation.setStopTime(new Date());
        satelliteObservation.setDataFile(new File("."));
        satelliteObservation.setGeoBounds(geometryFactory.parse("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))"));
        storage.insert(satelliteObservation);

        storage.close();

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

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        return options;
    }
}
