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
public class MatchupToolIntegrationTest_useCase_17 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(600, null)  // 10 minutes is large enough to create some matchups
                .withMaxPixelDistanceKm(5, null)
                .withAngularCosineScreening("Satellite_zenith_angle", "Satellite_zenith_angle", 0.01f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-17.xml");

        insert_AMSUB_NOAA15();
        insert_MHS_NOAA18();

        // 2007-08-22 is equal to 2007-234, so we set the interval to the three days around the acquisition tb 2016-04-21
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-233", "-end", "2007-235"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-233", "2007-235");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(22, matchupCount);
            
            NCTestUtils.assert3DVariable("amsub-n15_Latitude", 0, 0, 0, 740248, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Longitude", 0, 0, 1, -128073, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Satellite_azimuth_angle", 0, 0, 2, 19549, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Satellite_zenith_angle", 0, 0, 3, 3335, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Solar_azimuth_angle", 0, 0, 4, 13438, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_Solar_zenith_angle", 0, 0, 5, 6799, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_acquisition_time", 0, 0, 0, 1187777087, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch16", 0, 0, 1, 22659, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch17", 0, 0, 2, 25166, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch18", 0, 0, 3, 24345, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch19", 0, 0, 4, 25645, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_btemps_ch20", 0, 0, 5, 26744, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch16", 0, 0, 0, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch17", 0, 0, 1, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch18", 0, 0, 2, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch19", 0, 0, 3, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_chanqual_ch20", 0, 0, 4, 0.0, mmd);

            NCTestUtils.assertStringVariable("amsub-n15_file_name", 5, "L0502033.NSS.AMBX.NK.D07234.S1004.E1149.B4821213.WI.h5", mmd);

            NCTestUtils.assert3DVariable("amsub-n15_instrtemp", 0, 0, 0, 29281, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_qualind", 0, 0, 1, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_scanqual", 0, 0, 2, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_scnlin", 0, 0, 3, 5, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_scnlindy", 0, 0, 4, 234.0, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_scnlintime", 0, 0, 5, 36266453, mmd);
            NCTestUtils.assert3DVariable("amsub-n15_scnlinyr", 0, 0, 0, 2007.0, mmd);

            NCTestUtils.assertVectorVariable("amsub-n15_x", 1, 16, mmd);
            NCTestUtils.assertVectorVariable("amsub-n15_y", 2, 5, mmd);

            NCTestUtils.assert3DVariable("mhs-n18_Latitude", 0, 0, 3, 753918, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_Longitude", 0, 0, 4, -129896, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_Satellite_azimuth_angle", 0, 0, 5, 24901, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_Satellite_zenith_angle", 0, 0, 0, 4058, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_Solar_azimuth_angle", 0, 0, 1, 13648, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_Solar_zenith_angle", 0, 0, 2, 6701, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_acquisition_time", 0, 0, 3, 1187777612, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch1", 0, 0, 4, 22611, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch2", 0, 0, 5, 25588, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch3", 0, 0, 0, 24179, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch4", 0, 0, 1, 25075, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch5", 0, 0, 2, 26131, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_chanqual_ch1", 0, 0, 3, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_chanqual_ch2", 0, 0, 4, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_chanqual_ch3", 0, 0, 5, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_chanqual_ch4", 0, 0, 0, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_chanqual_ch5", 0, 0, 1, 0.0, mmd);

            NCTestUtils.assertStringVariable("mhs-n18_file_name", 2, "NSS.MHSX.NN.D07234.S1010.E1156.B1161920.GC.h5", mmd);

            NCTestUtils.assert3DVariable("mhs-n18_instrtemp", 0, 0, 3, 29340, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_qualind", 0, 0, 4, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_scanqual", 0, 0, 5, 0.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_scnlin", 0, 0, 0, 76, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_scnlindy", 0, 0, 1, 234.0, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_scnlintime", 0, 0, 2, 36812610, mmd);
            NCTestUtils.assert3DVariable("mhs-n18_scnlinyr", 0, 0, 3, 2007.0, mmd);

            NCTestUtils.assertVectorVariable("mhs-n18_x", 4, 19, mmd);
            NCTestUtils.assertVectorVariable("mhs-n18_y", 5, 79, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_additionalDistanceVariable() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(600, null)  // 10 minutes is large enough to create some matchups
                .withMaxPixelDistanceKm(5, null)
                .withAngularCosineScreening("Satellite_zenith_angle", "Satellite_zenith_angle", 0.01f)
                .withSphericalDistanceVariable()
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-17.xml");

        insert_AMSUB_NOAA15();
        insert_MHS_NOAA18();

        // 2007-08-22 is equal to 2007-234, so we set the interval to the three days around the acquisition tb 2016-04-21
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-233", "-end", "2007-235"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-233", "2007-235");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assertVectorVariable("mhs-n18_amsub-n15_matchup_spherical_distance", 0, 3.1593990325927734, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_tooLargeTimedelta_noTimeOverlap() throws IOException, ParseException, SQLException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(30, null)   // 30 seconds, just too small to have an overlapping time interval
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-17.xml");

        insert_AMSUB_NOAA15();
        insert_MHS_NOAA18();

        // 2007-08-22 is equal to 2007-234, so we set the interval to the three days around the acquisition tb 2016-04-21
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2007-233", "-end", "2007-235"};

        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2007-233", "2007-235");
        assertFalse(mmdFile.isFile());
    }

    private void insert_AMSUB_NOAA15() throws IOException, SQLException {
        final String sensorKey = "amsub-n15";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1.0", "2007", "08", "22", "L0502033.NSS.AMBX.NK.D07234.S1004.E1149.B4821213.WI.h5"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_MHS_NOAA18() throws IOException, SQLException {
        final String sensorKey = "mhs-n18";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1.0", "2007", "08", "22", "NSS.MHSX.NN.D07234.S1010.E1156.B1161920.GC.h5"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("mhs-n18");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("amsub-n15"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("mhs-n18", 1, 1));
        dimensions.add(new Dimension("amsub-n15", 1, 1));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd17")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-17").getPath())
                .withDimensions(dimensions);
    }
}
