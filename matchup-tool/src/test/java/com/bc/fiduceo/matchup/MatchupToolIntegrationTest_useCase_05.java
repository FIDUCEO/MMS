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
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_05 extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(2700) // 45 minutes - we have one intersecting time interval
                .withMaxPixelDistanceKm(20)   // value in km
                .withHIRS_LZA_Screening(10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-05.xml");

        insert_HIRS_NOAA10();
        insert_HIRS_NOAA11();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "1989-076", "-end", "1989-076"};
        MatchupToolMain.main(args);


        final File mmdFile = getMmdFilePath(useCaseConfig, "1989-076", "1989-076");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("hirs-n10_acquisition_time", 0, 0, 0, 606122189, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_bt_ch01", 2, 0, 2, Float.NaN, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_bt_ch02", 4, 0, 4, 223.5828857421875, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_bt_ch03", 1, 1, 6, 223.87351989746094, mmd);

            NCTestUtils.assert3DVariable("hirs-n10_counts_ch01", 3, 1, 8, 1403, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_counts_ch02", 0, 2, 10, -1373, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_counts_ch03", 2, 2, 12, -1828, mmd);

            NCTestUtils.assertStringVariable("hirs-n10_file_name", 14, "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc", mmd);

            NCTestUtils.assert3DVariable("hirs-n10_lat", 4, 2, 16, -81.4609375, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_lon", 1, 3, 18, -42.40625, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_lza", 3, 3, 20, 26.595956802368164, mmd);

            NCTestUtils.assert3DVariable("hirs-n10_radiance_ch01", 0, 4, 22, -458.3385009765625, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_radiance_ch02", 2, 4, 24, 47.478275299072266, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_radiance_ch03", 4, 4, 26, 46.72418975830078, mmd);

            NCTestUtils.assert3DVariable("hirs-n10_scanline", 1, 0, 27, 641, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_scanpos", 3, 0, 29, 11, mmd);
            NCTestUtils.assert3DVariable("hirs-n10_time", 0, 1, 31, 606122227, mmd);

            NCTestUtils.assertScalarVariable("hirs-n10_x", 33, 21, mmd);
            NCTestUtils.assertScalarVariable("hirs-n10_y", 35, 643, mmd);

            NCTestUtils.assert3DVariable("hirs-n11_acquisition_time", 1, 0, 1, N3iosp.NC_FILL_INT, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_bt_ch01", 3, 0, 3, 230.6595001220703, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_bt_ch02", 0, 1, 5, N3iosp.NC_FILL_FLOAT, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_bt_ch03", 2, 1, 7, 223.0589141845703, mmd);

            NCTestUtils.assert3DVariable("hirs-n11_counts_ch01", 4, 1, 9, -1599, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_counts_ch02", 1, 2, 11, N3iosp.NC_FILL_INT, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_counts_ch03", 3, 2, 13, -673, mmd);

            NCTestUtils.assertStringVariable("hirs-n11_file_name", 15, "NSS.HIRX.NH.D89076.S0557.E0743.B0245152.WI.nc", mmd);

            NCTestUtils.assert3DVariable("hirs-n11_lat", 0, 3, 17, N3iosp.NC_FILL_DOUBLE, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_lon", 2, 3, 19, -64.390625, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_lza", 4, 3, 21, 53.71925735473633, mmd);

            NCTestUtils.assert3DVariable("hirs-n11_radiance_ch01", 1, 4, 23, 52.45464324951172, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_radiance_ch02", 3, 4, 25, 48.26007080078125, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_radiance_ch03", 0, 0, 26, 122.11962890625, mmd);

            NCTestUtils.assert3DVariable("hirs-n11_scanline", 2, 0, 28, 447, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_scanpos", 4, 0, 30, 3, mmd);
            NCTestUtils.assert3DVariable("hirs-n11_time", 1, 1, 32, 606120235, mmd);
            // @todo 2 tb/tb reactivate when the testdata is updated 2016-08-22
            //NCTestUtils.assert3DVariable("hirs-n11_scanline_type", 1, 1, 33, 3, mmd);

            NCTestUtils.assertScalarVariable("hirs-n11_x", 34, 2, mmd);
            NCTestUtils.assertScalarVariable("hirs-n11_y", 36, 451, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_noTimeOverlap() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemProperties(configDir);
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(900)   // 15 minutes - we have no intersecting time intervals
                .withMaxPixelDistanceKm(20)   // value in km
                .withHIRS_LZA_Screening(10.f)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-05.xml");

        insert_HIRS_NOAA10();
        insert_HIRS_NOAA11();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "1989-076", "-end", "1989-076"};
        MatchupToolMain.main(args);


        final File mmdFile = getMmdFilePath(useCaseConfig, "1989-076", "1989-076");
        assertFalse(mmdFile.isFile());
    }

    private void insert_HIRS_NOAA11() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n11", "1.0", "1989", "03", "17", "NSS.HIRX.NH.D89076.S0557.E0743.B0245152.WI.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation noaa11 = readSatelliteObservation("hirs-n11", absolutePath, "1.0");
        storage.insert(noaa11);
    }

    private void insert_HIRS_NOAA10() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation noaa10 = readSatelliteObservation("hirs-n10", absolutePath, "1.0");
        storage.insert(noaa10);
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("hirs-n10");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("hirs-n11"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("hirs-n10", 5, 5));
        dimensions.add(new Dimension("hirs-n11", 5, 5));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd05")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-05").getPath())
                .withDimensions(dimensions);
    }
}
