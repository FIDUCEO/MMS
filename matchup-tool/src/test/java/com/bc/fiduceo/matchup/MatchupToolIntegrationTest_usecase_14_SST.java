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

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_usecase_14_SST {

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
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(2100)
                .withMaxPixelDistanceKm(1.41f).
                createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig);

        insert_AATSR();
        insert_AMSRE();


        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

    }

    private File storeUseCaseConfig(UseCaseConfig useCaseConfig) throws IOException {
        final File useCaseConfigFile = new File(configDir, "usecase-14_sst.xml");
        final FileOutputStream outputStream = new FileOutputStream(useCaseConfigFile);
        useCaseConfig.store(outputStream);
        outputStream.close();

        return useCaseConfigFile;
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("amsre-aq");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("aatsr-en"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("amsre-aq", 21, 21));
        dimensions.add(new Dimension("aatsr-en", 11, 11));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd14_sst")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-14_sst").getPath())
                .withDimensions(dimensions);
    }

    private void insert_AATSR() throws IOException, SQLException {
        final String sensorKey = "aatsr-en";
        final String version = "v2.1";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "17", "ATS_TOA_1PUUPA20050217_053700_000065272034_00434_15518_9023.N1"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_AMSRE() throws IOException, SQLException {
        final String sensorKey = "amsre-aq";
        final String version = "v12";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private SatelliteObservation readSatelliteObservation(String sensorKey, String absolutePath, String version) throws IOException {
        final ReaderFactory readerFactory = ReaderFactory.get(geometryFactory);
        try (Reader reader = readerFactory.getReader(sensorKey)) {
            reader.open(new File(absolutePath));
            final AcquisitionInfo acquisitionInfo = reader.read();
            final SatelliteObservation satelliteObservation = new SatelliteObservation();
            satelliteObservation.setSensor(new Sensor(sensorKey));
            satelliteObservation.setStartTime(acquisitionInfo.getSensingStart());
            satelliteObservation.setStopTime(acquisitionInfo.getSensingStop());
            satelliteObservation.setDataFilePath(absolutePath);
            satelliteObservation.setGeoBounds(acquisitionInfo.getBoundingGeometry());
            satelliteObservation.setTimeAxes(acquisitionInfo.getTimeAxes());
            satelliteObservation.setNodeType(acquisitionInfo.getNodeType());
            satelliteObservation.setVersion(version);

            return satelliteObservation;
        }
    }

}
