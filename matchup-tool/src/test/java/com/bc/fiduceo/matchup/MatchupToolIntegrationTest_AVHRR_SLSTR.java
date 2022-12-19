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
                .withTimeDeltaSeconds(7200, null)
                .withMaxPixelDistanceKm(0.025f, null)
                .withOverlapRemoval("SECONDARY")
                .withPixelPosition("SECONDARY", 568, 1467, -1, -1)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-avhrr-slstr.xml");

        insert_AVHRR_FRAC_MC();
        insert_SLSTR_UOR();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2020-142", "-end", "2020-143"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2020-142", "2020-143");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1641, matchupCount);

            NCTestUtils.assert3DVariable("slstr-s3a-uor_S1_radiance_in", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S7_BT_in", 1, 0, 24, -641, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_acquisition_time", 2, 0, 25, 1590189143, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S8_exception_in", 0, 1, 30, 0, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_cloud_io", 1, 1, 31, 1024, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_detector_io", 2, 1, 32, 0, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_latitude_in", 0, 2, 33, -28294595, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_longitude_in", 1, 2, 34, -15396355, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_tn", 2, 2, 35, 255.55368047574404, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_to", 0, 0, 36, 255.10406145929286, mmd);

            NCTestUtils.assert3DVariable("avhrr-frac-mc_acquisition_time", 0, 0, 1, 1590186057, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_cloudFlag", 1, 0, 2, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_delta_azimuth", 2, 0, 3, -5.319550037384033, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_flags", 0, 1, 4, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_latitude", 1, 1, 5, -28.55242347717285, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_longitude", 2, 1, 6, -11.668692588806152, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_1", 0, 2, 7, -0.10571436583995819, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_2", 1, 2, 8, 0.009339322336018085, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_3a", 2, 2, 9, 0.0, mmd);
        }
    }

    @Test
    public void testMatchup_AVHRR_SLSTR_UOR_coo6() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder_coo06()
                .withTimeDeltaSeconds(7200, null)
                .withMaxPixelDistanceKm(1.f, null)
                .withBorderDistance(4, 4)
                .withOverlapRemoval("PRIMARY")
                .withPixelPosition("PRIMARY", 548, 1448, -1, -1)
                .withPixelValueScreening("S8_BT_in > 260.0", null)
                .withRandomPointsPerDay(2400000, "INV_TRUNC_COSINE_LAT")
                .withTestRun()  // we need that to always have the same random sequence, else we cannot run assertions tb 2022-12-14
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-avhrr-slstr.xml");

        insert_AVHRR_FRAC_MC_coo06();
        insert_SLSTR_UOR_coo06();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2020-084", "-end", "2020-085"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2020-084", "2020-085");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(2, matchupCount);

            NCTestUtils.assert3DVariable("slstr-s3a-uor_S2_radiance_in", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S8_BT_in", 1, 0, 1, -489, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_acquisition_time", 2, 0, 0, 1585094546, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_S9_exception_in", 3, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_cloud_in", 4, 0, 0, 6720, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_detector_io", 5, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_latitude_in", 6, 0, 0, 44083570, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_longitude_in", 0, 1, 1, -35702362, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_tn", 1, 1, 0, 315.4619750972859, mmd);
            NCTestUtils.assert3DVariable("slstr-s3a-uor_solar_azimuth_to", 2, 1, 1, 315.61679013351716, mmd);

            NCTestUtils.assert3DVariable("avhrr-frac-mc_acquisition_time", 3, 1, 0, 1585088512, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_cloudFlag", 4, 1, 0, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_delta_azimuth", 5, 1, 1, -51.33234405517578, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_flags", 6, 1, 0, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_latitude", 0, 2, 1, 45.60192108154297, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_longitude", 1, 2, 0, -34.61174392700195, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_1", 2, 2, 1, -0.10135757923126221, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_2", 3, 2, 0, 0.007804560009390116, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_3a", 4, 2, 1, 0.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_4", 5, 2, 0, 73.82252502441406, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-mc_radiance_5", 6, 2, 1, 97.5717544555664, mmd);
        }
    }

    private void insert_SLSTR_UOR() throws IOException, SQLException {
        final String sensorKey = "slstr-s3a-uor";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "1.0", "2020", "05", "22", "S3A_SL_1_RBT____20200522T231202_20200522T231502_20200524T053503_0179_058_286_5580_LN2_O_NT_004.SEN3"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_SLSTR_UOR_coo06() throws IOException, SQLException {
        final String sensorKey = "slstr-s3a-uor";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "1.0", "2020", "03", "25", "S3A_SL_1_RBT____20200325T000151_20200325T000451_20200326T054139_0179_056_216_0720_LN2_O_NT_004.zip"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_AVHRR_FRAC_MC() throws IOException, SQLException {
        final String sensorKey = "avhrr-frac-mc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1", "2020", "05", "22", "NSS.FRAC.M3.D20143.S2148.E2331.B0799798.SV"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_AVHRR_FRAC_MC_coo06() throws IOException, SQLException {
        final String sensorKey = "avhrr-frac-mc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1", "2020", "03", "24", "NSS.FRAC.M3.D20084.S2209.E2352.B0715960.SV"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder_coo06() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("slstr-s3a-uor");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-frac-mc"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("avhrr-frac-mc", 7, 7));
        dimensions.add(new com.bc.fiduceo.core.Dimension("slstr-s3a-uor", 7, 7));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("coo06")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "coo06-avhrr-slstr").getPath())
                .withDimensions(dimensions);
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
