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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SystemConfig;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class IngestionToolTest {

    private String ls;
    private IngestionTool ingestionTool;

    @Before
    public void SetUp() {
        ls = System.lineSeparator();
        ingestionTool = new IngestionTool();
    }

    @Test
    public void testPrintUsageTo() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ingestionTool.printUsageTo(outputStream);

        assertEquals("ingestion-tool version 1.0.0" + ls +
                             ls +
                             "usage: ingestion-tool <options>" + ls +
                             "Valid options are:" + ls +
                             "   -c,--config <arg>   Defines the configuration directory. Defaults to './config'." + ls +
                             "   -h,--help           Prints the tool usage." + ls +
                             "   -s,--sensor <arg>   Defines the sensor to be ingested." + ls, outputStream.toString());
    }

    @Test
    public void testGetOptions() {
        final Options options = IngestionTool.getOptions();
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
    }

    @Test
    public void testFileGlob() throws IOException {
        File[] files = setFileFilter(TestUtil.getTestDataDirectory().getPath(), "*.h5");
        assertTrue(files != null);
    }

    private File[] setFileFilter(String location, String regEx) {
        File fileLocation = new File(location);
        FileFilter wildcardFileFilter = new WildcardFileFilter(regEx);
        return fileLocation.listFiles(wildcardFileFilter);
    }
}
