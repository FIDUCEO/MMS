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
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.matchup.writer.MmdWriterFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.fail;

abstract class AbstractUsecaseIntegrationTest {

    File configDir;
    GeometryFactory geometryFactory;
    Storage storage;

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

    SatelliteObservation readSatelliteObservation(String sensorKey, String absolutePath, String version) throws IOException {
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

    File storeUseCaseConfig(UseCaseConfig useCaseConfig, String fileName) throws IOException {
        final File useCaseConfigFile = new File(configDir, fileName);
        final FileOutputStream outputStream = new FileOutputStream(useCaseConfigFile);
        useCaseConfig.store(outputStream);
        outputStream.close();

        return useCaseConfigFile;
    }

    File getMmdFilePath(UseCaseConfig useCaseConfig, String startDateString, String endDateString) {
        final String mmdFileName = MmdWriterFactory.createMMDFileName(useCaseConfig, TimeUtils.parseDOYBeginOfDay(startDateString), TimeUtils.parseDOYEndOfDay(endDateString));
        return new File(useCaseConfig.getOutputPath(), mmdFileName);
    }
}
