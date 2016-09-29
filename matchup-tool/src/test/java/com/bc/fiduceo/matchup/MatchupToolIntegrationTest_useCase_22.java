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

import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_22 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(1200)  // 5 minutes
                .withMaxPixelDistanceKm(5)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-22.xml");

        insert_AMSUB_NOAA15();
        insert_SSMT2_F14();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2001-165", "-end", "2001-165"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2001-165", "2001-165");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("amsub-n15_Latitude", 0, 0, 0, 480658, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Longitude", 1, 0, 1, 972300, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Satellite_azimuth_angle", 2, 0, 2, 32543, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Satellite_zenith_angle", 0, 1, 3, 4956, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Solar_azimuth_angle", 1, 1, 4, 30142, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Solar_zenith_angle", 2, 1, 5, 8272, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_acquisition_time", 0, 2, 6, 992523889, mmd);

            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch16", 1, 2, 7, 27707, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch17", 2, 2, 8, 27720, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch18", 0, 0, 9, 24099, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch19", 1, 0, 10, 25501, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch20", 2, 0, 11, 26426, mmd);

            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch16", 0, 1, 12, 0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch17", 1, 1, 13, 0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch18", 2, 1, 14, 0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch19", 0, 2, 15, 0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch20", 1, 2, 16, 0, mmd);

            NCTestUtils.assertStringVariable("amsub-n15_file_name", 17, "NSS.AMBX.NK.D01165.S1136.E1320.B1604142.WI.h5", mmd);

            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount01", 0, 0, 220, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount02", 1, 0, 221, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount03", 2, 0, 222, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount04", 0, 1, 223, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount05", 1, 1, 224, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_Temperature_misc_housekeeping_thermistorcount18", 2, 1, 225, 0.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_acquisition_time", 0, 2, 226, 992523255, mmd);

            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_DayofYear_1", 1, 2, 227, 165.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_DayofYear_1", 2, 2, 228, 165.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SatAlt", 0, 0, 229, 863.03125, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SatHeading", 1, 0, 230, 80.15132904052734, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SatLat", 2, 0, 231, 81.10199737548828, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SatLong", 0, 1, 232, 38.29920959472656, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SecondsofDay_1", 1, 1, 233, 46471.42346683411, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_SecondsofDay_2", 2, 1, 234, 46463.41565651333, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_Year_1", 0, 2, 235, 2001.0, mmd);
            NCTestUtils.assert3DVariable("ssmt2-f14_ancil_data_Year_2", 1, 2, 236, 2001.0, mmd);

            NCTestUtils.assert3DVariable("ssmt2-f14_channel_quality_flag_ch1", 2, 2, 237, 1.0, mmd);

            NCTestUtils.assert3DVariable("ssmt2-f14_Satellite_zenith_angle", 0, 0, 238, 47.41999816894531, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_withAngularScreening() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(1200)  // 5 minutes
                .withMaxPixelDistanceKm(5)
                .withAngularCosineScreening("Satellite_zenith_angle", "Satellite_zenith_angle", 0.01f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-22.xml");

        insert_AMSUB_NOAA15();
        insert_SSMT2_F14();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2001-165", "-end", "2001-165"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2001-165", "2001-165");
        assertFalse(mmdFile.isFile());  // no matchups remain tb 2016-09-28
    }

    @Test
    public void testMatchup_overlappingSensingTimes_withCloudScreening() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(1200)  // 5 minutes
                .withMaxPixelDistanceKm(5)
                .withBuehlerCloudScreening("btemps_ch3", "btemps_ch4", "Satellite_zenith_angle", "tb_ch3", "tb_ch4", "Satellite_zenith_angle")
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-22.xml");

        insert_AMSUB_NOAA15();
        insert_SSMT2_F14();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2001-165", "-end", "2001-165"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2001-165", "2001-165");
        assertFalse(mmdFile.isFile());  // no matchups remain tb 2016-09-28
    }

    private void insert_AMSUB_NOAA15() throws IOException, SQLException {
        final String sensorKey = "amsub-n15";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1.0", "2001", "06", "14", "NSS.AMBX.NK.D01165.S1136.E1320.B1604142.WI.h5"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_SSMT2_F14() throws IOException, SQLException {
        final String sensorKey = "ssmt2-f14";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v01", "2001", "06", "14", "F14200106141229.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("amsub-n15");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("ssmt2-f14"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("amsub-n15", 3, 3));
        dimensions.add(new Dimension("ssmt2-f14", 3, 3));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd22")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-22").getPath())
                .withDimensions(dimensions);
    }
}
