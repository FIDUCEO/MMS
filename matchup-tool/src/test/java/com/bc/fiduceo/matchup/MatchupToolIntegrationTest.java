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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class MatchupToolIntegrationTest {

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
    public void testRunMatchup_notInputParameter() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2016-02-15
        final String[] args = new String[0];
        MatchupToolMain.main(args);
    }

    @Test
    public void testRunMatchup_help() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2016-02-16
        String[] args = new String[]{"-h"};
        MatchupToolMain.main(args);

        args = new String[]{"--help"};
        MatchupToolMain.main(args);
    }

    @Test
    public void testRunMatchup_missingSystemProperties() throws ParseException, IOException, SQLException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124", "-e", "1999-176", "-u", configFileName};

        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testRunMatchup_missingDatabaseProperties() throws ParseException, IOException, SQLException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124", "-e", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testRunMatchup_missingUseCaseConfig() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124", "-e", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testRunMatchup_missingStartDate() throws ParseException, IOException, SQLException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-e", "1999-176"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testRunMatchup_missingEndDate() throws ParseException, IOException, SQLException {
        final String configFileName = "use-case-config.xml";
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "--start", "1999-124"};

        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeDatabaseProperties_H2(configDir);
        writeUseCaseConfig(configFileName);

        try {
            MatchupToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testRunMatchup_AMSUB_MHS_noTimeOverlap() throws SQLException, IOException, ParseException {
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
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-n17");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n18"));
        useCaseConfig.setSensors(sensorList);
        useCaseConfig.setTimeDelta(2);
        final File file = new File(configDir, configFileName);
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        useCaseConfig.store(fileOutputStream);
        fileOutputStream.close();
    }

}
