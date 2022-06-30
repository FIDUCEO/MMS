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
public class MatchupToolIntegrationTest_useCase_6c_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingTimes_noGeometryMatch() throws IOException, ParseException, SQLException {
        insert_AMSRE();
        insert_Insitu("drifter-sst", "insitu_0_WMOID_51993_20040402_20060207.nc");

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(3600, null)
                .withMaxPixelDistanceKm(1.41f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertFalse(mmdFile.isFile());
    }

    @Test
    public void testMatchup_noInsituDataInInterval() throws IOException, ParseException, SQLException {
        insert_AMSRE();
        insert_Insitu("drifter-sst", "insitu_0_WMOID_46942_19951026_19951027.nc");

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(3600, null)
                .withMaxPixelDistanceKm(1.41f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertFalse(mmdFile.isFile());
    }

    @Test
    public void testMatchup_drifter() throws IOException, ParseException, SQLException, InvalidRangeException {
        insert_AMSRE();
        insert_Insitu("drifter-sst", "insitu_0_WMOID_71612_20040223_20151010.nc");

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(43200, null)
                .withMaxPixelDistanceKm(6.f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("amsre-aq_10_7H_Res_1_TB", 0, 0, 0, -22297, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_10_7V_Res_1_TB", 1, 0, 0, -15307, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_18_7H_Res_1_TB", 2, 0, 0, -17409, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_18_7V_Res_1_TB", 3, 0, 0, -12157, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_23_8H_Res_1_TB", 4, 0, 0, -12691, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_23_8V_Res_1_TB", 0, 1, 0, -9726, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_36_5H_Res_1_TB", 1, 1, 0, -11837, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_36_5V_Res_1_TB", 2, 1, 0, -8744, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_6_9H_Res_1_TB", 3, 1, 0, -23637, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_6_9V_Res_1_TB", 4, 1, 0, -16271, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_89_0H_Res_1_TB", 0, 2, 0, -6233, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_89_0V_Res_1_TB", 1, 2, 0, -5851, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_Channel_Quality_Flag_10H", 2, 2, 0, 0, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_Geostationary_Reflection_Latitude", 3, 2, 0, -1051, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_Geostationary_Reflection_Longitude", 4, 2, 0, -1363, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_Sun_Glint_Angle", 0, 3, 0, 1406, mmd);

            NCTestUtils.assert3DVariable("drifter-sst_acquisition_time", 0, 0, 0, 1108599012, mmd);
            NCTestUtils.assertStringVariable("drifter-sst_file_name", 0, "insitu_0_WMOID_71612_20040223_20151010.nc", mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.lat", 0, 0, 0, -51.040000915527344, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.lon", 0, 0, 0, 18.610000610351562, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.mohc_id", 0, 0, 0, 392166, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.sea_surface_temperature", 0, 0, 0, 1.7000000476837158, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_depth", 0, 0, 0, 0.2, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_qc_flag", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_track_flag", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.sst_uncertainty", 0, 0, 0, 0.389, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.time", 0, 0, 0, 856138212, mmd);
            NCTestUtils.assert3DVariable("drifter-sst_insitu.id", 0, 0, 0, 2005020000392166L, mmd);
            NCTestUtils.assertVectorVariable("drifter-sst_x", 0, 0, mmd);
            NCTestUtils.assertVectorVariable("drifter-sst_y", 0, 8485, mmd);
        }
    }

    private void insert_AMSRE() throws IOException, SQLException {
        final String sensorKey = "amsre-aq";
        final String version = "v12";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "16", "AMSR_E_L2A_BrightnessTemperatures_V12_200502161217_A.hdf"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_Insitu(String insituType, String fileName) throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", insituType, "v03.3", fileName}, true);
        final SatelliteObservation insitu = readSatelliteObservation("drifter-sst", relativeArchivePath, "v03.3");
        storage.insert(insitu);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("drifter-sst");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("amsre-aq"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("drifter-sst", 1, 1));
        dimensions.add(new Dimension("amsre-aq", 5, 5));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd6c_SST")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-6c").getPath())
                .withDimensions(dimensions);
    }
}
