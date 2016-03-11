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

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.*;
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

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_12 {

    private File configDir;
    private Storage storage;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() throws SQLException {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        storage = Storage.create(TestUtil.getdatasourceMongoDb(), geometryFactory);
        storage.clear();
        storage.initialize();
    }

    @After
    public void tearDown() throws SQLException {
        if (storage != null) {
            storage.clear();
            storage.close();
        }

        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testMatchup_noMatchups_timeDeltaTooSmall_noResultsFromDb() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfig();
        useCaseConfig.setTimeDelta(22);
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig);

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};

        MatchupToolMain.main(args);

        // @todo 1 tb/tb add assertions when we are able to write an MMD file. 2016-03-07
        // assert here: empty MMD file
    }

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfig();
        useCaseConfig.setTimeDelta(10800);  // 3 hours - we have one intersecting time interval
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig);

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};

        MatchupToolMain.main(args);

        // @todo 1 tb/tb add assertions when we are able to write an MMD file. 2016-03-07
        // assert here: MMD file generated at the desired location
        // open MMD file and check content
    }

    @Test
    public void testMatchup_overlappingSensingTimes_tooLargeTimedelta_noTimeOverlap() throws IOException, ParseException, SQLException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfig();
        useCaseConfig.setTimeDelta(10000);  // 2 hours something, just too small to have an overllipng time interval
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig);

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};

        MatchupToolMain.main(args);

        // @todo 1 tb/tb add assertions when we are able to write an MMD file. 2016-03-07
        // assert here: empty MMD file
    }

    private void insert_AVHRR_GAC_NOAA18() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "1.02", "2007", "04", "01", "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation noaa18 = TestData.createObservation_AVHRR_GAC_NOAA_18(absolutePath, geometryFactory);
        storage.insert(noaa18);
    }

    private void insert_AVHRR_GAC_NOAA17() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "1.01", "2007", "04", "01", "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation noaa17 = TestData.createObservation_AVHRR_GAC_NOAA_17(absolutePath, geometryFactory);
        storage.insert(noaa17);
    }

    private File storeUseCaseConfig(UseCaseConfig useCaseConfig) throws IOException {
        final File useCaseConfigFile = new File(configDir, "usecase-12.xml");
        final FileOutputStream outputStream = new FileOutputStream(useCaseConfigFile);
        useCaseConfig.store(outputStream);
        outputStream.close();

        return useCaseConfigFile;
    }

    private UseCaseConfig createUseCaseConfig() {
        final UseCaseConfig useCaseConfig = new UseCaseConfig();
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-n17");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n18"));
        useCaseConfig.setSensors(sensorList);
        useCaseConfig.setOutputPath("usecase-12");
        return useCaseConfig;
    }
}
