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
import static org.junit.Assert.assertTrue;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_03_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(28800, null) // 4 hrs
                .withMaxPixelDistanceKm(20f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-03_SST.xml");

        insert_AVHRR_GAC_NOAA17();
        insert_GTMBA_SST();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2003-302", "-end", "2006-304"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2003-302", "2006-304");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(10, matchupCount);

            NCTestUtils.assert3DVariable("avhrr-n17_acquisition_time", 0, 0, 0, 1162208239, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch1", 1, 0, 1, 359, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch2", 2, 0, 2, 212, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch3a", 3, 0, 3, 593, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch3b", 4, 0, 4, -32768, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch4", 0, 1, 5, 2189, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ch5", 1, 1, 6, 1992, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_cloud_mask", 2, 1, 7, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_cloud_probability", 3, 1, 8, -1, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_dtime", 4, 1, 9, 2183.00048828125, mmd);
            NCTestUtils.assertStringVariable("avhrr-n17_file_name", 0, "20061030110000-ESACCI-L1C-AVHRR17_G-fv01.0.nc", mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_ict_temp", 0, 2, 1, 1437, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_l1b_line_number", 1, 2, 2, 4368, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_lat", 2, 2, 3, -0.02500000037252903, mmd);
            NCTestUtils.assert3DVariable("avhrr-n17_lon", 3, 2, 4, -22.933013916015625, mmd);

            NCTestUtils.assert3DVariable("gtmba-sst_acquisition_time", 0, 0, 0, 1162180800, mmd);
            NCTestUtils.assertStringVariable("gtmba-sst_file_name", 1, "insitu_3_WMOID_31007_19990306_20160920.nc", mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.collection", 0, 0, 2, 1, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.id", 0, 0, 3, 2006100000935922L, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.lat", 0, 0, 4, -0.009999999776482582, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.lon", 0, 0, 5, -22.989999771118164, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.mohc_id", 0, 0, 6, 943364, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.prof_id", 0, 0, 7, 943364, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.qc1", 0, 0, 8, 0, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.qc2", 0, 0, 0, -99, mmd);
            NCTestUtils.assert3DVariable("gtmba-sst_insitu.sea_surface_temperature", 0, 0, 0, 26.100000381469727, mmd);
        }
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("gtmba-sst");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-n17"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("gtmba-sst", 1, 1));
        dimensions.add(new Dimension("avhrr-n17", 5, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd03_sst")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-03_sst").getPath())
                .withDimensions(dimensions);
    }

    private void insert_AVHRR_GAC_NOAA17() throws IOException, SQLException {
        final String processingVersion = "v01.2";
        final String sensorKey = "avhrr-n17";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, processingVersion, "2006", "10", "30", "20061030110000-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation aatsr = readSatelliteObservation(sensorKey, absolutePath, processingVersion);
        storage.insert(aatsr);
    }

    private void insert_GTMBA_SST() throws IOException, SQLException {
        final String processingVersion = "v04.0";
        final String sensorKey = "gtmba-sst";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", sensorKey, processingVersion, "insitu_3_WMOID_31007_19990306_20160920.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation iasi = readSatelliteObservation(sensorKey, absolutePath, processingVersion);
        storage.insert(iasi);
    }
}
