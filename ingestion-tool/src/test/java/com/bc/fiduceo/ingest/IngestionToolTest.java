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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IngestionToolTest {

    private String ls;

    @Before
    public void SetUp() {
        ls = System.lineSeparator();
    }

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final IngestionTool ingestionTool = new IngestionTool();

        ingestionTool.printUsageTo(outputStream);

        assertEquals("ingestion-tool version 1.0.0" + ls +
                             ls +
                             "usage: ingestion-tool <options>" + ls +
                             "Valid options are:" + ls +
                             "   -c,--config <arg>   Defines the configuration directory. Defaults to './config'." + ls +
                             "   -h,--help           Prints the tool usage." + ls +
                             "   -n,--name <arg>     Define the name of the product file." + ls +
                             "   -s,--sensor <arg>   Defines the sensor to be ingested." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final IngestionTool ingestionTool = new IngestionTool();
        final Options options = ingestionTool.getOptions();
        assertNotNull(options);

        final Option helpOption = options.getOption("h");
        assertNotNull(helpOption);
        assertEquals("h", helpOption.getOpt());
        assertEquals("help", helpOption.getLongOpt());
        assertEquals("Prints the tool usage.", helpOption.getDescription());
        assertFalse(helpOption.hasArg());

        final Option sensorOption = options.getOption("sensor");
        assertNotNull(sensorOption);
        assertEquals("s", sensorOption.getOpt());
        assertEquals("sensor", sensorOption.getLongOpt());
        assertEquals("Defines the sensor to be ingested.", sensorOption.getDescription());
        assertTrue(sensorOption.hasArg());

        final Option configOption = options.getOption("config");
        assertNotNull(configOption);
        assertEquals("c", configOption.getOpt());
        assertEquals("config", configOption.getLongOpt());
        assertEquals("Defines the configuration directory. Defaults to './config'.", configOption.getDescription());
        assertTrue(configOption.hasArg());

        final Option cnameOption = options.getOption("name");
        assertNotNull(cnameOption);
        assertEquals("n", cnameOption.getOpt());
        assertEquals("name", cnameOption.getLongOpt());
        assertTrue(cnameOption.hasArg());
    }

    @Test
    public void testInjectSensorName() {

    }
}
