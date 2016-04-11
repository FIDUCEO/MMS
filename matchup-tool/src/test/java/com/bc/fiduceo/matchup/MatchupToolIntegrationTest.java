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

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class MatchupToolIntegrationTest {

    private final String ls = System.lineSeparator();
    private final String expectedPrintUsage = "matchup-tool version 1.0.0" + ls +
            ls +
            "usage: matchup-tool <options>" + ls +
            "Valid options are:" + ls +
            "   -c,--config <arg>           Defines the configuration directory. Defaults to './config'." + ls +
            "   -end,--end-time <arg>       Defines the processing end-date, format 'yyyy-DDD'" + ls +
            "   -h,--help                   Prints the tool usage." + ls +
            "   -start,--start-time <arg>   Defines the processing start-date, format 'yyyy-DDD'" + ls +
            "   -u,--usecase <arg>          Defines the path to the use-case configuration file. Path is relative to the" + ls +
            "                               configuration directory." + ls;
    private File configDir;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testRunMatchup_notInputParameter_printUsageExpected() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String[] args = new String[0];
        final String errOutput = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOutput);
    }

    @Test
    public void testRunMatchup_withHelpParameter_printUsageExpected() throws ParseException, IOException, SQLException, InvalidRangeException {
        String[] args = new String[]{"-h"};
        final String errOut = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOut);

        args = new String[]{"--help"};
        final String errOutput = callMatchupToolMain_wrappedWithSystemErrSpy(args);
        assertEquals(expectedPrintUsage, errOutput);
    }

    @Test
    public void testRunMatchup_missingSystemProperties() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start-time", "1999-124", "-end", "1999-176", "-u", configFileName};

        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String path = new File(configDir, "system.properties").getAbsolutePath();
            assertEquals("Configuration file not found: " + path, expected.getMessage());
        }
    }

    @Test
    public void testRunMatchup_missingDatabaseProperties() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "1999-124", "--end-time", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            final String path = new File(configDir, "database.properties").getAbsolutePath();
            assertEquals("Configuration file not found: " + path, expected.getMessage());
        }
    }

    @Test
    public void testRunMatchup_missingUseCaseConfig() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "1999-124", "-end", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("Use case configuration file not supplied", expected.getMessage());
        }
    }

    @Test
    public void testRunMatchup_missingStartDate() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-end", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("cmd-line parameter `start` missing", expected.getMessage());
        }
    }

    @Test
    public void testRunMatchup_missingEndDate() throws ParseException, IOException, SQLException, InvalidRangeException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertEquals("cmd-line parameter `end` missing", expected.getMessage());
        }
    }

    @Test
    public void testRunMatchup_AMSUB_MHS_noTimeOverlap() throws SQLException, IOException, ParseException, InvalidRangeException {
        final String configFileName = "use-case-config.xml";
        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Storage storage = Storage.create(TestUtil.getDatasource_H2(), geometryFactory);
        storage.initialize();

        try {
            final Geometry geometry = geometryFactory.parse("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
            final SatelliteObservation amsubObservation = createSatelliteObservation(geometry, "amsub-n15", "2010-07-21 16:34:19", "2010-07-21 16:55:07");
            storage.insert(amsubObservation);

            final SatelliteObservation mhsObservation = createSatelliteObservation(geometry, "mhs-n18", "2010-08-21 16:34:19", "2010-08-21 16:55:07");
            storage.insert(mhsObservation);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "2007-100", "--end", "2007-200", "-u", configFileName};

            TestUtil.writeSystemProperties(configDir);
            TestUtil.writeDatabaseProperties_H2(configDir);
            writeUseCaseConfig(configFileName);

            MatchupToolMain.main(args);

            // @todo 1 tb/tb assert that no output file is generated 2016-02-19

        } finally {
            storage.clear();
            storage.close();
        }
    }

    private String callMatchupToolMain_wrappedWithSystemErrSpy(String[] args) throws ParseException, IOException, SQLException, InvalidRangeException {
        final PrintStream err = System.err;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);
        System.setErr(printStream);

        MatchupToolMain.main(args);

        System.setErr(err);
        printStream.close();
        return out.toString();
    }

    private SatelliteObservation createSatelliteObservation(Geometry geometry, String sensorName, String startDate, String stopDate) {
        final SatelliteObservation observation = new SatelliteObservation();
        observation.setStartTime(TimeUtils.parse(startDate, "yyyy-MM-dd hh:mm:ss"));
        observation.setStopTime(TimeUtils.parse(stopDate, "yyyy-MM-dd hh:mm:ss"));
        observation.setGeoBounds(geometry);
        final Sensor sensor = new Sensor();
        sensor.setName(sensorName);
        observation.setSensor(sensor);
        observation.setDataFilePath(".");
        return observation;
    }

    private void writeUseCaseConfig(String configFileName) throws IOException {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        useCaseConfig.setName("use-case-15");

        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-n17");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n18"));
        useCaseConfig.setSensors(sensorList);

        final List<Dimension> dimensionsList = new ArrayList<>();
        dimensionsList.add(new Dimension("avhrr-n17", 2, 3));
        dimensionsList.add(new Dimension("avhrr-n18", 2, 3));
        useCaseConfig.setDimensions(dimensionsList);

        useCaseConfig.setTimeDeltaSeconds(2);
        useCaseConfig.setOutputPath(new File(TestUtil.getTestDir(), "mmd-15").getPath());

        final File file = new File(configDir, configFileName);
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        useCaseConfig.store(fileOutputStream);
        fileOutputStream.close();
    }
}
