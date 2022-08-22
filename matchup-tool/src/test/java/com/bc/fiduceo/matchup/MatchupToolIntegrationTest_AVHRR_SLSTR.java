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

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_AVHRR_SLSTR extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AVHRR_SLSTR_UOR() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(60000, null)
                .withMaxPixelDistanceKm(0.1f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-avhrr-slstr.xml");

        insert_AVHRR_FRAC_MA();
        insert_SLSTR_UOR();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2020-142", "-end", "2020-143"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2020-142", "2020-143");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1380, matchupCount);

            NCTestUtils.assert3DVariable("slstr-s3a-uor_S1_radiance_in", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S7_BT_in", 1, 0, 24, 854, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_acquisition_time", 2, 0, 25, 1590189249, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S8_exception_in", 0, 1, 30, 2, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_cloud_io", 1, 1, 31, 32769, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_detector_io", 2, 1, 32, 255, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_latitude_in", 0, 2, 33, -19847056, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_longitude_in", 1, 2, 34, -5812828, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_tn", 2, 2, 35, 275.6034547080558, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_to", 0, 0, 36, Float.NaN, mmd);

            NCTestUtils.assert3DVariable("avhrr-frac-mc_acquisition_time", 0, 0, 1, 1590137472, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_cloudFlag", 1, 0, 2, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_delta_azimuth", 2, 0, 3, 52.04375076293945, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_flags", 0, 1, 4, -1, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_latitude", 1, 1, 5, -19.104799270629883, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_longitude", 2, 1, 6, -5.870356559753418, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_1", 0, 2, 7, 87.23554992675781, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_2", 1, 2, 8, 64.54716491699219, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_3a", 2, 2, 9, 11.970253944396973, mmd);
        }
    }

    private void insert_SLSTR_UOR() throws IOException, SQLException {
        final String sensorKey = "slstr-s3a-uor";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_AVHRR_FRAC_MA() throws IOException, SQLException {
        final String sensorKey = "avhrr-frac-mc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1", "2020", "05", "22", "NSS.FRAC.M3.D20143.S0824.E1005.B0798990.SV"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-frac-mc");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("slstr-s3a-uor"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("avhrr-frac-mc", 3, 3));
        dimensions.add(new com.bc.fiduceo.core.Dimension("slstr-s3a-uor", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("coo30")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "coo30-avhrr-slstr").getPath())
                .withDimensions(dimensions);
    }
}
