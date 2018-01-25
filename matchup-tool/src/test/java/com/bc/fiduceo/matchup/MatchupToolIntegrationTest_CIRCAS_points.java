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

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_CIRCAS_points extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_CIRCAS_location_extracts() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withLocationElement(-58.230045, 36.67764)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-circas.xml");

        insert_MOD06_Terra();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2013-036", "-end", "2013-038"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2013-036", "2013-038");
        assertTrue(mmdFile.isFile());

        // @todo 1 tb/tb more assertions here 2018-01-10
//
//        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
//            final int matchupCount = NetCDFUtils.getDimensionLength("matchup_count", mmd);
//            assertEquals(10, matchupCount);
//
//            NCTestUtils.assert3DVariable("mhs-n18_Latitude", 0, 0, 0, 899425, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_Longitude", 0, 0, 1, 595288, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_Satellite_azimuth_angle", 0, 0, 2, 5531, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_Satellite_zenith_angle", 0, 0, 3, 5503, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_Solar_azimuth_angle", 0, 0, 4, 7118, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_Solar_zenith_angle", 0, 0, 5, 7856, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_acquisition_time", 0, 0, 6, 1314060561, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch1", 0, 0, 7, 21296, mmd);
//            NCTestUtils.assert3DVariable("mhs-n18_btemps_ch2", 0, 0, 8, 24782, mmd);
//
//            NCTestUtils.assert3DVariable("ocean-rain-sst_acquisition_time", 0, 0, 0, 1314060300, mmd);
//            NCTestUtils.assertStringVariable("ocean-rain-sst_file_name", 1, "OceanRAIN_allships_2010-2017_SST.ascii", mmd);
//            NCTestUtils.assert3DVariable("ocean-rain-sst_lat", 0, 0, 2, 89.93270111083984, mmd);
//            NCTestUtils.assert3DVariable("ocean-rain-sst_lon", 0, 0, 3, 62.43450164794922, mmd);
//            NCTestUtils.assertStringVariable("ocean-rain-sst_processing_version", 30, 4, "v1.0", mmd);
//            NCTestUtils.assert3DVariable("ocean-rain-sst_sst", 0, 0, 5, -1.600000023841858, mmd);
//            NCTestUtils.assert3DVariable("ocean-rain-sst_time", 0, 0, 6, 1314060660, mmd);
//            NCTestUtils.assertVectorVariable("ocean-rain-sst_x", 7, 0, mmd);
//            NCTestUtils.assertVectorVariable("ocean-rain-sst_y", 8, 549953, mmd);
//        }
    }

    private void insert_MOD06_Terra() throws IOException, SQLException {
        final String sensorKey = "mod06-te";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v006", "2013", "037", "MOD06_L2.A2013037.1435.006.2015066015540.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, "v006");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("mod06-te");
        primary.setPrimary(true);
        sensorList.add(primary);

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("mod06-te", 1, 1));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("circas")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "circas").getPath())
                .withDimensions(dimensions);
    }
}