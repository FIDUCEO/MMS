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
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.ParseException;
import org.junit.*;
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
public class MatchupToolIntegrationTest_useCase_01 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AVHRR_v013() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(300, null)
                .withMaxPixelDistanceKm(1, null)   // value in km
                .withAtsrAngularScreening(10.0, 1.0)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-01.xml");

        insert_AATSR();
        insert_AVHRR_GAC_NOAA18_v013();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2006-046", "-end", "2006-046"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2006-046", "2006-046");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(31283, matchupCount);
            
            NCTestUtils.assert3DVariable("aatsr-en_acquisition_time", 0, 0, 6, 1139989157, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_altitude", 1, 0, 7, -384.9903259277344, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_0370", 2, 0, 8, 26506, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_1100", 3, 0, 9, 26293, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_1200", 4, 0, 10, 26125, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_0370", 5, 0, 11, 26692, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_1100", 6, 0, 13, 26365, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_1200", 7, 0, 14, 26295, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_cloud_flags_fward", 8, 0, 15, 3554, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_cloud_flags_nadir", 9, 0, 16, 482, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_confid_flags_fward", 10, 0, 17, 0, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_confid_flags_nadir", 0, 1, 18, 0, mmd);
            NCTestUtils.assertStringVariable("aatsr-en_file_name", 19, "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1", mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lat_corr_fward", 2, 1, 20, -0.0, mmd);

            NCTestUtils.assert3DVariable("avhrr-n18_acquisition_time", 0, 0, 1400, 1139989392, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch1", 1, 0, 1401, 115, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch2", 2, 0, 1402, 202, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3a", 3, 0, 1403, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3b", 4, 0, 1404, -2068, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch4", 5, 0, 1405, -2186, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch5", 6, 0, 1406, -289, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_mask", 7, 0, 1407, 7, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_probability", 8, 0, 1408, -128, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_dtime", 9, 0, 1409, 5820.501953125, mmd);
            NCTestUtils.assertStringVariable("avhrr-n18_file_name", 1410, "20060215060600-ESACCI-L1C-AVHRR18_G-fv01.0.nc", mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ict_temp", 0, 1, 1411, 1555, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_l1b_line_number", 1, 1, 1412, 11641, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_lat", 2, 1, 1413, 76.69000244140625, mmd);
        }
    }

    @Test
    public void testMatchup_AVHRR_v014_CSPP_withOverlapRemoval() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(300, null)
                .withMaxPixelDistanceKm(1, null)   // value in km
                .withAtsrAngularScreening(10.0, 1.0)
                .withOverlapRemoval("PRIMARY")
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-01.xml");

        insert_AATSR();
        insert_AVHRR_GAC_NOAA18_v014_CSPP();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2006-046", "-end", "2006-046"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2006-046", "2006-046");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1118, matchupCount);
            
            NCTestUtils.assert3DVariable("aatsr-en_acquisition_time", 0, 0, 23, 1139989169, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_altitude", 1, 0, 24, -257.77374267578125, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_0370", 2, 0, 25, 26860, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_1100", 3, 0, 26, 26986, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_1200", 4, 0, 27, 26620, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_0370", 5, 0, 28, 27002, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_1100", 6, 0, 29, 26946, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_nadir_1200", 7, 0, 30, 26837, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_cloud_flags_fward", 8, 0, 31, 34, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_cloud_flags_nadir", 9, 0, 32, 0, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_confid_flags_fward", 10, 0, 33, 0, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_confid_flags_nadir", 0, 1, 34, 0, mmd);
            NCTestUtils.assertStringVariable("aatsr-en_file_name", 35, "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1", mmd);
            NCTestUtils.assert3DVariable("aatsr-en_lat_corr_fward", 2, 1, 36, -0.19031231105327606, mmd);

            NCTestUtils.assert3DVariable("avhrr-n18_acquisition_time", 3, 1, 500, 1139989339, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch1", 4, 1, 501, 303, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch1_noise", 5, 1, 502, 46, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch2", 6, 1, 503, 371, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch2_noise", 7, 1, 504, 38, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3a", 8, 1, 505, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3a_noise", 9, 1, 506, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3b", 10, 1, 507, -884, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3b_nedt", 0, 2, 508, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch4", 1, 2, 509, -1935, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch4_nedt", 2, 2, 510, 38, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch5", 3, 2, 511, -1611, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch5_nedt", 4, 2, 512, 36, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_mask", 5, 2, 513, 7, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_probability", 6, 2, 514, -128, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_dtime", 7, 2, 515, 5698.501953125, mmd);
            NCTestUtils.assertStringVariable("avhrr-n18_file_name", 516, "20060215060600-ESACCI-L1C-AVHRR18_G-fv01.0.nc", mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ict_temp", 8, 2, 517, 1558, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_l1b_line_number", 9, 2, 518, 11545, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_lat", 10, 2, 519, 72.61799621582031, mmd);
        }
    }

    private void insert_AVHRR_GAC_NOAA18_v013() throws IOException, SQLException {
        final String dataVersion = "v01.3";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", dataVersion, "2006", "02", "15", "20060215060600-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, true);
        final SatelliteObservation noaa18 = readSatelliteObservation("avhrr-n18", relativeArchivePath, dataVersion);
        storage.insert(noaa18);

    }private void insert_AVHRR_GAC_NOAA18_v014_CSPP() throws IOException, SQLException {
        final String dataVersion = "v01.4-cspp";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", dataVersion, "2006", "02", "15", "20060215060600-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, true);
        final SatelliteObservation noaa18 = readSatelliteObservation("avhrr-n18", relativeArchivePath, dataVersion);
        storage.insert(noaa18);
    }

    private void insert_AATSR() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"aatsr-en", "v3", "2006", "02", "15", "ATS_TOA_1PUUPA20060215_070852_000065272045_00120_20715_4282.N1"}, true);
        final SatelliteObservation aatsr = readSatelliteObservation("aatsr-en", relativeArchivePath, "v3");
        storage.insert(aatsr);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("aatsr-en");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n18"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("aatsr-en", 11, 11));
        dimensions.add(new Dimension("avhrr-n18", 11, 11));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd01")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-01").getPath())
                .withDimensions(dimensions);
    }
}
