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

import static org.junit.Assert.*;

import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_6c_SST_2secondarySensors extends AbstractUsecaseIntegrationTest {

    public static final String PRIM_SENSOR_NAME = "drifter-sst";

    public static final String SEC_SENSOR_NAME_1 = "hirs-n18";
    public static final String SEC_SENSOR_VERSION_1 = "1.0";
    public static final String SEC_SENSOR_NAME_2 = "mhs-n18";
    public static final String SEC_SENSOR_VERSION_2 = "v1.0";

    @Test
    public void testMatchup_drifter() throws IOException, ParseException, SQLException, InvalidRangeException {
        insert_HIRS_NOAA18();
        insert_MHS_NOAA18();
        insert_Insitu(PRIM_SENSOR_NAME, "insitu_0_WMOID_51939_20031105_20131121.nc");

        final MatchupToolUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder
                    .withTimeDeltaSeconds(8000, SEC_SENSOR_NAME_1)
                    .withMaxPixelDistanceKm(10.0f, SEC_SENSOR_NAME_1)
                    .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2011-233", "-end", "2011-239"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2011-233", "2011-239");
        assertTrue(mmdFile.isFile());

//        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
//            NCTestUtils.assert3DVariable("amsre-aq_10_7H_Res_1_TB", 0, 0, 0, -22297, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_10_7V_Res_1_TB", 1, 0, 0, -15307, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_18_7H_Res_1_TB", 2, 0, 0, -17409, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_18_7V_Res_1_TB", 3, 0, 0, -12157, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_23_8H_Res_1_TB", 4, 0, 0, -12691, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_23_8V_Res_1_TB", 0, 1, 0, -9726, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_36_5H_Res_1_TB", 1, 1, 0, -11837, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_36_5V_Res_1_TB", 2, 1, 0, -8744, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_6_9H_Res_1_TB", 3, 1, 0, -23637, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_6_9V_Res_1_TB", 4, 1, 0, -16271, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_89_0H_Res_1_TB", 0, 2, 0, -6233, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_89_0V_Res_1_TB", 1, 2, 0, -5851, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_Channel_Quality_Flag_10H", 2, 2, 0, 0, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_Geostationary_Reflection_Latitude", 3, 2, 0, -1051, mmd);
//            NCTestUtils.assert3DVariable("amsre-aq_Geostationary_Reflection_Longitude", 4, 2, 0, -1363, mmd);
//
//            NCTestUtils.assert3DVariable("drifter-sst_acquisition_time", 0, 0, 0, 1108599012, mmd);
//            NCTestUtils.assertStringVariable("drifter-sst_file_name", 0, "insitu_0_WMOID_71612_20040223_20151010.nc", mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.lat", 0, 0, 0, -51.040000915527344, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.lon", 0, 0, 0, 18.610000610351562, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.mohc_id", 0, 0, 0, 392166, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.sea_surface_temperature", 0, 0, 0, 1.7000000476837158, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_depth", 0, 0, 0, 0.2, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_qc_flag", 0, 0, 0, 0, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_track_flag", 0, 0, 0, 0, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_uncertainty", 0, 0, 0, 0.389, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.time", 0, 0, 0, 856138212, mmd);
//            NCTestUtils.assert3DVariable("drifter-sst_insitu.id", 0, 0, 0, 2005020000392166L, mmd);
//            NCTestUtils.assertVectorVariable("drifter-sst_x", 0, 0, mmd);
//            NCTestUtils.assertVectorVariable("drifter-sst_y", 0, 8485, mmd);
//        }
    }

    private void insert_Insitu(String insituType, String fileName) throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", insituType, "v03.3", fileName}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation insitu = readSatelliteObservation("drifter-sst", absolutePath, "v03.3");
        storage.insert(insitu);
    }

    private void insert_HIRS_NOAA18() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{
                    "hirs-n18", "1.0", "2011", "08", "23", "190455003.NSS.HIRX.NN.D11235.S0028.E0223.B3223536.WI.nc"
        }, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation satelliteObservation = readSatelliteObservation("hirs-n18", absolutePath, "1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_MHS_NOAA18() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{
                    "mhs-n18", "v1.0", "2011", "08", "23", "190457103.NSS.MHSX.NN.D11235.S0028.E0223.B3223536.WI.h5"
        }, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation satelliteObservation = readSatelliteObservation("mhs-n18", absolutePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(PRIM_SENSOR_NAME);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor(SEC_SENSOR_NAME_1, SEC_SENSOR_VERSION_1));
        sensorList.add(new Sensor(SEC_SENSOR_NAME_2, SEC_SENSOR_VERSION_2));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension(PRIM_SENSOR_NAME, 1, 1));
        dimensions.add(new Dimension(SEC_SENSOR_NAME_1, 5, 5));
        dimensions.add(new Dimension(SEC_SENSOR_NAME_2, 3, 3));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd6c_SST")
                    .withSensors(sensorList)
                    .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-6c").getPath())
                    .withDimensions(dimensions);
    }
}
