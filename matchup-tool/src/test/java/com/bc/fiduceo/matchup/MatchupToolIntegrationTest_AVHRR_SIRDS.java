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
public class MatchupToolIntegrationTest_AVHRR_SIRDS extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AVHRR_SIRDS() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(7200, null)
                .withMaxPixelDistanceKm(5, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-42.xml");

        insert_AVHRR_FRAC_MA();
        insert_SIRDS();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2019-276", "-end", "2019-276"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2019-276", "2019-276");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(62, matchupCount);

            NCTestUtils.assert3DVariable("ship-sirds_acquisition_time", 0, 0, 0, 1570068000, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_collection", 0, 0, 1, 1, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_depth", 0, 0, 3, 5.0, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_depth_corr", 0, 0, 4, -4.9, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_latitude", 0, 0, 5, 36.8, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_longitude", 0, 0, 6, -121.1, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_prof_id", 0, 0, 7, 55629, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_qc1", 0, 0, 8, 0, mmd);
            NCTestUtils.assert3DVariable("ship-sirds_qc2", 0, 0, 9, -99, mmd);
            NCTestUtils.assertStringVariable("ship-sirds_plat_id", 9, 10, "MK0010071", mmd);

            NCTestUtils.assert3DVariable("avhrr-frac-ma_acquisition_time", 0, 0, 1, 1570072980, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_cloudFlag", 1, 0, 2, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_delta_azimuth", 2, 0, 3, -49.05099868774414, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_flags", 3, 0, 4, 0, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_latitude", 4, 0, 5, 36.77418518066406, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_longitude", 0, 1, 6, -121.08010864257812, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_radiance_1", 1, 1, 7, 0.17542949318885803, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_radiance_2", 2, 1, 8, 0.057511311024427414, mmd);
            NCTestUtils.assert3DVariable("avhrr-frac-ma_radiance_3a", 3, 1, 9, 0.0, mmd);
        }
    }

    private void insert_SIRDS() throws IOException, SQLException {
        final String sensorKey = "ship-sirds";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", "SSTCCI2_refdata_ship_201910.nc"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_AVHRR_FRAC_MA() throws IOException, SQLException {
        final String sensorKey = "avhrr-frac-ma";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1", "2019", "10", "03", "NSS.FRAC.M2.D19276.S0249.E0432.B6721920.SV"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("ship-sirds");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("avhrr-frac-ma"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("avhrr-frac-ma", 5, 5));
        dimensions.add(new com.bc.fiduceo.core.Dimension("ship-sirds", 1, 1));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd42")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-42").getPath())
                .withDimensions(dimensions);
    }
}
