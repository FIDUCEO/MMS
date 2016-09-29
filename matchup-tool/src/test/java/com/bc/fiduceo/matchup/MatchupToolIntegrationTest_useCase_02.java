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
import com.bc.fiduceo.TestData;
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
public class MatchupToolIntegrationTest_useCase_02 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_noMatchups_timeDeltaTooSmall_noResultsFromDb() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(22)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};

        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-090", "2007-092");
        assertFalse(mmdFile.isFile());
    }

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
       final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(10800) // 3 hours - we have one intersecting time interval
                .withMaxPixelDistanceKm(3)   // value in km
                .withAngularScreening("satellite_zenith_angle", "satellite_zenith_angle", Float.NaN, Float.NaN, 10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-090", "2007-092");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assertScalarVariable("avhrr-n17_x", 0, 52.0, mmd);
            NCTestUtils.assertScalarVariable("avhrr-n17_y", 1, 13025.0, mmd);
            NCTestUtils.assertStringVariable("avhrr-n17_file_name", 2, "20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc", mmd);

            NCTestUtils.assertScalarVariable("avhrr-n18_x", 3, 79.0, mmd);
            NCTestUtils.assertScalarVariable("avhrr-n18_y", 4, 2306.0, mmd);
            NCTestUtils.assertStringVariable("avhrr-n18_file_name", 5, "20070401080400-ESACCI-L1C-AVHRR18_G-fv01.0.nc", mmd);

            NCTestUtils.assert3DVariable("avhrr-n17_acquisition_time", 0, 0, 6, 1175405006.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_acquisition_time", 1, 0, 7, 1175415805.0, mmd);

            NCTestUtils.assert3DVariable("avhrr-n18_lat", 2, 0, 8, 19.722999572753906, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_lon", 3, 0, 9, -103.84298706054688, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_dtime", 4, 0, 10, 1154.00048828125, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch1", 0, 1, 11, 3.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch2", 1, 1, 12, 0.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3a", 2, 1, 13, -32768.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch3b", 3, 1, 14, 991.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch4", 4, 1, 15, 668.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ch5", 0, 2, 16, 932.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_satellite_zenith_angle", 1, 2, 17, 3870.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_solar_zenith_angle", 2, 2, 18, 14806.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_relative_azimuth_angle", 3, 2, 19, 5455.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_ict_temp", 4, 2, 20, 1466.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_qual_flags", 0, 3, 21, 0.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_mask", 1, 3, 22, 7.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_cloud_probability", 2, 3, 23, -128.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n18_l1b_line_number", 3, 3, 24, 2312.0, mmd);

            NCTestUtils.assert3DVariable("avhrr-n17_lat", 4, 3, 25, 19.702999114990234, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_lon", 0, 4, 26, -104.16299438476562, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_dtime", 1, 4, 27, 6515.00048828125, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch1", 1, 1, 28, 0.0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch2", 2, 1, 29, 5.0, mmd);
            // @todo 2 tb/** add more assertions here
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_tooLargeTimedelta_noTimeOverlap() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(10000)   // 2 hours something, just too small to have an overlapping time interval
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-02.xml");

        insert_AVHRR_GAC_NOAA17();
        insert_AVHRR_GAC_NOAA18();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-090", "-end", "2007-092"};

        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-090", "2007-092");
        assertFalse(mmdFile.isFile());
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

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("avhrr-n17");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n18"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("avhrr-n17", 5, 5));
        dimensions.add(new Dimension("avhrr-n18", 5, 5));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd02")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-02").getPath())
                .withDimensions(dimensions);
    }
}
