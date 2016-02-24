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
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                "   -c,--config <arg>                             Defines the configuration directory. Defaults to './config'." + ls +
                "   -concurrent,--concurrent-injection <Number>   Define the number of concurrent execution." + ls +
                "   -end,--end-time <Date>                        Define the ending time of products to inject." + ls +
                "   -h,--help                                     Prints the tool usage." + ls +
                "   -s,--sensor <arg>                             Defines the sensor to be ingested." + ls +
                "   -start,--start-time <Date>                    Define the starting time of products to inject." + ls +
                "   -v,--version <arg>                            Define the sensor version." + ls, outputStream.toString());
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

        final Option startTime = options.getOption("start-time");
        assertNotNull(startTime);
        assertEquals("start", startTime.getOpt());
        assertEquals("start-time", startTime.getLongOpt());
        assertEquals("Define the starting time of products to inject.", startTime.getDescription());
        assertTrue(startTime.hasArg());


        final Option endTime = options.getOption("end-time");
        assertNotNull(endTime);
        assertEquals("end", endTime.getOpt());
        assertEquals("end-time", endTime.getLongOpt());
        assertEquals("Define the ending time of products to inject.", endTime.getDescription());
        assertTrue(endTime.hasArg());


        final Option version = options.getOption("version");
        assertNotNull(version);
        assertEquals("v", version.getOpt());
        assertEquals("version", version.getLongOpt());
        assertEquals("Define the sensor version.", version.getDescription());
        assertTrue(version.hasArg());


        final Option concurrent = options.getOption("concurrent");
        assertNotNull(concurrent);
        assertEquals("concurrent", concurrent.getOpt());
        assertEquals("concurrent-injection", concurrent.getLongOpt());
        assertEquals("Define the number of concurrent execution.", concurrent.getDescription());
        assertTrue(concurrent.hasArg());
    }

    @Test
    public void testFileGlob() throws IOException {
        File[] files = setFileFilter(TestUtil.getTestDataDirectory().getPath(), "*.h5");
        assertTrue(files != null);
    }


    @Test
    @Ignore //todo mba implement the file systems [archive-root]/[sensor-platform]/[version]/[year]/[month]/[day]
    public void testGroupInputProduct() throws IOException {
        SystemConfig systemConfig = new SystemConfig();
        IngestionTool ingestionTool = new IngestionTool();
        systemConfig.loadFrom(TestUtil.getTestDataDirectory());

        List<File> files1 = ingestionTool.searchReaderFiles(systemConfig, "'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5");
        Date dateStart = TimeUtils.parseDOYBeginOfDay("2015-1");
        Date dateEnd = TimeUtils.parseDOYBeginOfDay("2015-365");
        List<Calendar[]> daysIntervalYear = TimeUtils.getDaysIntervalYear(dateStart, dateEnd, 20);

        List<Object[]> splitInputProduct = ingestionTool.getSplitInputProduct(daysIntervalYear, files1);

        for (Object[] files : splitInputProduct) {
            System.out.println("#####################" + files.length);
            for (Object file : files) {
                System.out.println("file.getName() = " + file.toString());
            }
            System.out.println("----------------------------------------------------");
        }
    }

    private File[] setFileFilter(String location, String regEx) {
        File fileLocation = new File(location);
        FileFilter wildcardFileFilter = new WildcardFileFilter(regEx);
        return fileLocation.listFiles(wildcardFileFilter);
    }

}
