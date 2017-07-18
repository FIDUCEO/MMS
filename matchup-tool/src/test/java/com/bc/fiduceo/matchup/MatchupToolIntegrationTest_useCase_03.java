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

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_03 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder(1, 1)
                .withTimeDeltaSeconds(900, null) // 15 mins
                .withMaxPixelDistanceKm(2.82f, null)   // value in km (2 * sqrt(2))
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-03.xml");

        insert_IASI_MetopB();
        insert_AVHRR_GAC_NOAA19();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2014-115", "-end", "2014-115"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2014-115", "2014-115");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", mmd);
            assertEquals(817, matchupCount);

            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_INST_MDR", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_PROC_MDR", 0, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_EARTH_SATELLITE_DISTANCE", 0, 0, 2, 7191921, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstCol", 0, 0, 3, 1764.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstLin", 0, 0, 4, -5024.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbCol", 0, 0, 3, 68, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbLin", 0, 0, 4, 66, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsRadAnalNbClass", 0, 0, 5, 4, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSDatIasi", 0, 0, 6, 1398430077961L, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSIasiMode", 0, 0, 7, 161, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSOPSProcessingMode", 0, 0, 8, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_CCD", 0, 0, 9, 1, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_SP", 0, 0, 10, 10, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BCldFrac", 0, 0, 11, 66, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BLandFrac", 0, 0, 12, 2, mmd);

            NCTestUtils.assert3DVariable("avhrr-n19_acquisition_time", 0, 0, 100, 1398430788, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch1", 1, 0, 101, 308, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch2", 2, 0, 102, 114, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch3a", 3, 0, 103, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch3b", 4, 0, 104, 1801, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch4", 5, 0, 105, -212, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch5", 6, 0, 106, -553, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_cloud_mask", 7, 0, 107, 7, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_cloud_probability", 8, 0, 108, -128, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_dtime", 9, 0, 109, 6128.5, mmd);
            NCTestUtils.assertStringVariable("avhrr-n19_file_name", 10, "20140425111800-ESACCI-L1C-AVHRR19_G-fv01.0.nc", mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ict_temp", 11, 0, 111, 1560, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_l1b_line_number", 12, 0, 112, 12279, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_lat", 13, 0, 113, 64.36499786376953, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_lon", 14, 0, 114, -25.902008056640625, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_VZACondition() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder(1, 1)
                .withTimeDeltaSeconds(900, null) // 15 mins
                .withMaxPixelDistanceKm(2.82f, null)   // value in km (2 * sqrt(2))
                .withAngularScreening("GGeoSondAnglesMETOP_Zenith", "satellite_zenith_angle", Float.NaN, Float.NaN, 10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-03.xml");

        insert_IASI_MetopB();
        insert_AVHRR_GAC_NOAA19();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2014-115", "-end", "2014-115"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2014-115", "2014-115");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", mmd);
            assertEquals(142, matchupCount);

            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_INST_MDR", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_PROC_MDR", 0, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_EARTH_SATELLITE_DISTANCE", 0, 0, 2, 7191921, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstCol", 0, 0, 3, 1641.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstLin", 0, 0, 4, -4524.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbCol", 0, 0, 3, 68, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbLin", 0, 0, 4, 62, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsRadAnalNbClass", 0, 0, 5, 4, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSDatIasi", 0, 0, 6, 1398430078180L, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSIasiMode", 0, 0, 7, 161, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSOPSProcessingMode", 0, 0, 8, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_CCD", 0, 0, 9, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_SP", 0, 0, 10, 5, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BCldFrac", 0, 0, 11, 83, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BLandFrac", 0, 0, 12, 99, mmd);

            NCTestUtils.assert3DVariable("avhrr-n19_acquisition_time", 0, 0, 100, 1398430756, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch1", 1, 0, 101, 3130, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch2", 2, 0, 102, 3463, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch3a", 3, 0, 103, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch3b", 4, 0, 104, -832, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch4", 5, 0, 105, -1976, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ch5", 6, 0, 106, -4302, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_cloud_mask", 7, 0, 107, 3, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_cloud_probability", 8, 0, 108, 126, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_dtime", 9, 0, 109, 6019.501953125, mmd);
            NCTestUtils.assertStringVariable("avhrr-n19_file_name", 10, "20140425111800-ESACCI-L1C-AVHRR19_G-fv01.0.nc", mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_ict_temp", 11, 0, 111, 1553, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_l1b_line_number", 12, 0, 112, 12056, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_lat", 13, 0, 113, 57.37900161743164, mmd);
            NCTestUtils.assert3DVariable("avhrr-n19_lon", 14, 0, 114, -20.4840087890625, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_VZACondition_IASIWindowExtract() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder(3, 1)
                .withTimeDeltaSeconds(900, null) // 15 mins
                .withMaxPixelDistanceKm(2.82f, null)   // value in km (2 * sqrt(2))
                .withAngularScreening("GGeoSondAnglesMETOP_Zenith", "satellite_zenith_angle", Float.NaN, Float.NaN, 10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-03.xml");

        insert_IASI_MetopB();
        insert_AVHRR_GAC_NOAA19();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2014-115", "-end", "2014-115"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2014-115", "2014-115");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", mmd);
            assertEquals(142, matchupCount);

            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_INST_MDR", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_DEGRADED_PROC_MDR", 1, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_EARTH_SATELLITE_DISTANCE", 2, 0, 2, 7191921, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstCol", 0, 0, 3, 1702.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedFirstLin", 1, 0, 4, -4524.0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbCol", 2, 0, 3, 68, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsImageClassifiedNbLin", 0, 0, 4, 62, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GCcsRadAnalNbClass", 1, 0, 5, 4, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSDatIasi", 2, 0, 6, 1398430078395L, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSIasiMode", 0, 0, 7, 161, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPSOPSProcessingMode", 1, 0, 8, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_CCD", 2, 0, 9, 0, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEPS_SP", 0, 0, 10, 5, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BCldFrac", 1, 0, 11, 83, mmd);
            NCTestUtils.assert3DVariable("iasi-mb_GEUMAvhrr1BLandFrac",2 , 0, 12, 98, mmd);

            // no need to check the AVHRR again tb 2017-06-13
        }
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder(int iasiWidth, int iasiHeight) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("iasi-mb");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n19"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("iasi-mb", iasiWidth, iasiHeight));
        dimensions.add(new Dimension("avhrr-n19", 15, 15));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd03")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-03").getPath())
                .withDimensions(dimensions);
    }

    private void insert_AVHRR_GAC_NOAA19() throws IOException, SQLException {
        final String processingVersion = "1.02";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n19", processingVersion, "2014", "04", "25", "20140425111800-ESACCI-L1C-AVHRR19_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation aatsr = readSatelliteObservation("avhrr-n19", absolutePath, processingVersion);
        storage.insert(aatsr);
    }

    private void insert_IASI_MetopB() throws IOException, SQLException {
        final String processingVersion = "v7-0N";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"iasi-mb", processingVersion, "2014", "04", "IASI_xxx_1C_M01_20140425124756Z_20140425142652Z_N_O_20140425133911Z.nat"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation iasi = readSatelliteObservation("iasi-mb", absolutePath, processingVersion);
        storage.insert(iasi);
    }
}
