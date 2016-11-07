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

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_6c_SST extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingTimes_noGeometryMatch() throws IOException, ParseException, SQLException, InvalidRangeException {
        insert_AMSRE();
        insert_Insitu();

        final MatchupToolUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(3600)
                .withMaxPixelDistanceKm(1.41f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-6c_sst.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertFalse(mmdFile.isFile());
    }

    private void insert_AMSRE() throws IOException, SQLException {
        final String sensorKey = "amsre-aq";
        final String version = "v12";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_Insitu() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "drifter-sst", "v03.3", "insitu_0_WMOID_51993_20040402_20060207.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation insitu = readSatelliteObservation("drifter-sst", absolutePath, "v03.3");
        storage.insert(insitu);
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("drifter-sst");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("amsre-aq"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("drifter-sst", 1, 1));
        dimensions.add(new Dimension("amsre-aq", 5, 5));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd6c_SST")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-6c").getPath())
                .withDimensions(dimensions);
    }
}
