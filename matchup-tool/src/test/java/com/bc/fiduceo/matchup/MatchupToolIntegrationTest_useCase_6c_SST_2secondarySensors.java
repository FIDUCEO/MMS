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

import static com.bc.fiduceo.NCTestUtils.*;
import static org.junit.Assert.*;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.DataType;
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
                    .withMaxPixelDistanceKm(10.0f, SEC_SENSOR_NAME_2)
                    .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2011-233", "-end", "2011-239"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2011-233", "2011-239");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            assertDimension("drifter-sst_nx", 1, mmd);
            assertDimension("drifter-sst_ny", 1, mmd);
            assertDimension("hirs-n18_nx", 5, mmd);
            assertDimension("hirs-n18_ny", 5, mmd);
            assertDimension("mhs-n18_nx", 3, mmd);
            assertDimension("mhs-n18_ny", 3, mmd);
            final int fn_Size = 128;
            assertDimension("file_name", fn_Size, mmd);
            final int pv_Size = 30;
            assertDimension("processing_version", pv_Size, mmd);
            assertDimension("matchup_count", 4, mmd);

            final String filenameDims = "matchup_count file_name";
            final String versionDims = "matchup_count processing_version";
            final String insitu1DDims = "matchup_count";
            final String insitu3DDims = "matchup_count drifter-sst_ny drifter-sst_nx";

            final String hirs3DDims = "matchup_count hirs-n18_ny hirs-n18_nx";

            assertVariablePresentAnd3DValueLong("drifter-sst_insitu.time", DataType.INT, insitu3DDims, 0, 0, 0, 1061596188, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.lat", DataType.FLOAT, insitu3DDims, 0, 0, 1, -32.3f, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.lon", DataType.FLOAT, insitu3DDims, 0, 0, 3, -155.59f, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.sea_surface_temperature", DataType.FLOAT, insitu3DDims, 0, 0, 1, 17.9f, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.sst_uncertainty", DataType.FLOAT, insitu3DDims, 0, 0, 2, 0.389f, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.sst_depth", DataType.FLOAT, insitu3DDims, 0, 0, 2, 0.2f, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.sst_qc_flag", DataType.SHORT, insitu3DDims, 0, 0, 0, 0, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.sst_track_flag", DataType.SHORT, insitu3DDims, 0, 0, 2, 3, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.mohc_id", DataType.INT, insitu3DDims, 0, 0, 1, 1031446, mmd);
            assertVariablePresentAnd3DValueDouble("drifter-sst_insitu.id", DataType.LONG, insitu3DDims, 0, 0, 2, 2011080001034085L, mmd);
            assertVariablePresentAnd1DValueLong("drifter-sst_x", DataType.INT, insitu1DDims, 1, 0, mmd);
            assertVariablePresentAnd1DValueLong("drifter-sst_y", DataType.INT, insitu1DDims, 1, 22482, mmd);
            assertStringVariable("drifter-sst_file_name", filenameDims, fn_Size, 2, "insitu_0_WMOID_51939_20031105_20131121.nc", mmd);
            assertStringVariable("drifter-sst_processing_version", versionDims, pv_Size, 2, "v03.3", mmd);
            assertVariablePresentAnd3DValueLong("drifter-sst_acquisition_time", DataType.INT, insitu3DDims, 0, 0, 0, 1314056988, mmd);

            assertVariablePresentAnd3DValueLong("hirs-n18_time", DataType.INT, hirs3DDims, 2, 2, 0, 1314064551, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_lat", DataType.DOUBLE, hirs3DDims, 2, 2, 0, -32.3208, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_lon", DataType.DOUBLE, hirs3DDims, 2, 2, 0, -155.4985, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch01", DataType.FLOAT, hirs3DDims, 1, 3, 2, 237.43939f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch02", DataType.FLOAT, hirs3DDims, 1, 3, 2, 224.90039f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch03", DataType.FLOAT, hirs3DDims, 1, 3, 2, 223.14154f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch04", DataType.FLOAT, hirs3DDims, 1, 3, 2, 223.25329f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch05", DataType.FLOAT, hirs3DDims, 1, 3, 2, 230.21084f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch06", DataType.FLOAT, hirs3DDims, 1, 3, 2, 243.05542f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch07", DataType.FLOAT, hirs3DDims, 1, 3, 2, 258.86190f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch08", DataType.FLOAT, hirs3DDims, 1, 3, 2, 282.95468f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch09", DataType.FLOAT, hirs3DDims, 1, 3, 2, 252.21335f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch10", DataType.FLOAT, hirs3DDims, 1, 3, 2, 277.46731f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch11", DataType.FLOAT, hirs3DDims, 1, 3, 2, 255.88241f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch12", DataType.FLOAT, hirs3DDims, 1, 3, 2, 231.09004f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch13", DataType.FLOAT, hirs3DDims, 1, 3, 2, 266.11725f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch14", DataType.FLOAT, hirs3DDims, 1, 3, 2, 249.75566f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch15", DataType.FLOAT, hirs3DDims, 1, 3, 2, 236.55244f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch16", DataType.FLOAT, hirs3DDims, 1, 3, 2, 230.91896f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch17", DataType.FLOAT, hirs3DDims, 1, 3, 2, 273.56720f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch18", DataType.FLOAT, hirs3DDims, 1, 3, 2, 285.74746f, mmd);
            assertVariablePresentAnd3DValueDouble("hirs-n18_bt_ch19", DataType.FLOAT, hirs3DDims, 1, 3, 2, 288.67504f, mmd);



        }
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
