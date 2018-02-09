/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
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

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_6b_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup() throws IOException, ParseException, SQLException, InvalidRangeException {
        insert_AMSR2();
        insert_Insitu("gtmba-sst", "insitu_3_WMOID_99099_20130701_20130701.nc");

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(3600, null)
                .withMaxPixelDistanceKm(12.f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6b_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2013-181", "-end", "2013-183"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2013-181", "2013-183");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", mmd);
            assertEquals(5, matchupCount);

            NCTestUtils.assert3DVariable("amsr2-gcw1_Area_Mean_Height", 0, 0, 0, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Brightness_Temperature_(res06,6.9GHz,H)", 1, 0, 1, 8868, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Brightness_Temperature_(res10,36.5GHz,V)", 2, 0, 2, 22494, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Brightness_Temperature_(res23,89.0GHz,H)", 3, 0, 3, 24975, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Earth_Azimuth", 4, 0, 4, 16793, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Land_Ocean_Flag_36", 0, 1, 0, 0, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Scan_Time", 1, 1, 1, 6.468264070538166E8, mmd);
            NCTestUtils.assert3DVariable("amsr2-gcw1_Sun_Elevation", 2, 1, 2, -1049, mmd);
        }
    }

    private void insert_AMSR2() throws IOException, SQLException {
        final String sensorKey = "amsr2-gcw1";
        final String version = "v220";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2013", "07", "01", "GW1AM2_201307010942_035A_L1SGRTBR_2220220.h5"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_Insitu(String insituType, String fileName) throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", insituType, "vFake", fileName}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation insitu = readSatelliteObservation("gtmba-sst", absolutePath, "vFake");
        storage.insert(insitu);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("gtmba-sst");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("amsr2-gcw1"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("gtmba-sst", 1, 1));
        dimensions.add(new Dimension("amsr2-gcw1", 5, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd6b_SST")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-6b").getPath())
                .withDimensions(dimensions);
    }
}
