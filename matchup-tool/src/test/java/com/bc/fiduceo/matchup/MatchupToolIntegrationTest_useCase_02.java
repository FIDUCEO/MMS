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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_02 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_noMatchups_timeDeltaTooSmall_noResultsFromDb() throws IOException, ParseException, SQLException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(22, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA10();
        insert_AVHRR_GAC_NOAA11();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "1991-129", "-end", "1991-129"};

        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "1991-129", "1991-129");
        assertFalse(mmdFile.isFile());
    }

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(8500, null)  // 2 hours something - we have one intersecting time interval
                .withMaxPixelDistanceKm(1.42f, null)   // value in km
                .withAngularScreening("satellite_zenith_angle", "satellite_zenith_angle", Float.NaN, Float.NaN, 10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA10();
        insert_AVHRR_GAC_NOAA11();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "1991-129", "-end", "1991-129"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "1991-129", "1991-129");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1031, matchupCount);

            NCTestUtils.assertVectorVariable("avhrr-n10_x", 0, 0, mmd);
            NCTestUtils.assertVectorVariable("avhrr-n10_y", 1, 9430, mmd);
            NCTestUtils.assertStringVariable("avhrr-n10_file_name", 2, "19910509045700-ESACCI-L1C-AVHRR10_G-fv01.0.nc", mmd);

            NCTestUtils.assertVectorVariable("avhrr-n11_x", 3, 0, mmd);
            NCTestUtils.assertVectorVariable("avhrr-n11_y", 4, 5005, mmd);
            NCTestUtils.assertStringVariable("avhrr-n11_file_name", 5, "19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc", mmd);

            NCTestUtils.assert3DVariable("avhrr-n10_acquisition_time", 0, 0, 6, -2147483647, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_acquisition_time", 1, 0, 7, 673778009, mmd);

            NCTestUtils.assert3DVariable("avhrr-n11_lat", 2, 0, 8, -54.0890007019043, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_lon", 3, 0, 9, -128.2239990234375, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_dtime", 4, 0, 10, 2505.001220703125, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ch1", 0, 1, 11, 2, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ch2", 1, 1, 12, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ch3b", 3, 1, 14, -1446, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ch4", 4, 1, 15, -1243, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ch5", 0, 2, 16, -1780, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_satellite_zenith_angle", 1, 2, 17, 6962, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_solar_zenith_angle", 2, 2, 18, 12750, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_relative_azimuth_angle", 3, 2, 19, 7408, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_ict_temp", 4, 2, 20, 1220, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_qual_flags", 0, 3, 21, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_cloud_mask", 1, 3, 22, 3, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_cloud_probability", 2, 3, 23, 126, mmd);
            NCTestUtils.assert3DVariable("avhrr-n11_l1b_line_number", 3, 3, 24, 5029, mmd);

            NCTestUtils.assert3DVariable("avhrr-n10_lat", 4, 3, 25, -56.50600051879883, mmd);
            NCTestUtils.assert3DVariable("avhrr-n10_lon", 0, 4, 26, -32768.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n10_dtime", 1, 4, 27, 4705.4990234375, mmd);
            NCTestUtils.assert3DVariable("avhrr-n10_ch1", 2, 4, 28, 28, mmd);
            NCTestUtils.assert3DVariable("avhrr-n10_ch2", 3, 4, 29, 45, mmd);
            NCTestUtils.assert3DVariable("avhrr-n10_ch3b", 4, 4, 30, -1560, mmd);
            // @todo 3 tb/** add more assertions here
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_tooLargeTimedelta_noTimeOverlap() throws IOException, ParseException, SQLException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(300, null)   // 5 minutes, just too small to have an overlapping time interval
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA10();
        insert_AVHRR_GAC_NOAA11();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "1991-129", "-end", "1991-129"};

        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "1991-129", "1991-129");
        assertFalse(mmdFile.isFile());
    }

    private void insert_AVHRR_GAC_NOAA11() throws IOException, SQLException {
        final String processingVersion = "v01.3";
        final String sensorKey = "avhrr-n11";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, processingVersion, "1991", "05", "09", "19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, true);
        final SatelliteObservation observation = readSatelliteObservation(sensorKey, relativeArchivePath, processingVersion);
        storage.insert(observation);
    }

    private void insert_AVHRR_GAC_NOAA10() throws IOException, SQLException {
        final String processingVersion = "v01.3";
        final String sensorKey = "avhrr-n10";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, processingVersion, "1991", "05", "09", "19910509045700-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, true);
        final SatelliteObservation observation = readSatelliteObservation(sensorKey, relativeArchivePath, processingVersion);
        storage.insert(observation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-n11");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n10"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("avhrr-n11", 5, 5));
        dimensions.add(new Dimension("avhrr-n10", 5, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd02")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-02").getPath())
                .withDimensions(dimensions);
    }
}
