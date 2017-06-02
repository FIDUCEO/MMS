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

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_useCase_24 extends AbstractUsecaseIntegrationTest {

    public static final int AMSUB_WIN_LEN = 9;
    public static final int HIRS_WIN_LEN = 9;
    public static final int FILENAME_LENGTH = 128;
    public static final int PROCESSING_VERSION_LENGTH = 30;
    public static final int MATCHUP_COUNT = 42;
    public static final String PRIMARY_SENSOR_NAME = "amsub-n16";
    public static final String SECONDARY_SENSOR_NAME = "hirs-n16";

    @Test
    public void testMatchup_seedPointStrategy() throws IOException, ParseException, SQLException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                    .withTimeDeltaSeconds(300, null) // 5 minutes
                    .withMaxPixelDistanceKm(5, null)   // value in km
                    .withRandomSeedPoints(2000000)   // 2.000.000 random seed points for 7 days to fulfill 20000 points per 2280 scan lines
//                .withHIRS_LZA_Screening(10.f)
                    .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-24.xml");

        insert_secondary();
        insert_primary();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2011-233", "-end", "2011-239"};
        MatchupToolMain.main(args);


        final File mmdFile = getMmdFilePath(useCaseConfig, "2011-233", "2011-239");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            assertEquals(MATCHUP_COUNT, mmd.findDimension("matchup_count").getLength());

            List<Variable> variables = mmd.getVariables();
            final Expectation[] expectations = getExpectations();
            assertEquals(expectations.length, variables.size());
            for (int i = 0; i < variables.size(); i++) {
                final Variable variable = variables.get(i);
                final Expectation expectation = expectations[i];
                final String message = "pos " + i;
                assertEquals(message, expectation.name, variable.getShortName());
                final List<Dimension> dimensions = variable.getDimensions();
                final Dimension[] expDims = expectation.dimensions;
                assertEquals(message, expDims.length, dimensions.size());
                for (int j = 0; j < expDims.length; j++) {
                    Dimension expDim = expDims[j];
                    Dimension dim = dimensions.get(j);
                    final String pos = "pos i j " + i + " " + j;
                    assertEquals(pos, expDim.getLength(), dim.getLength());
                    assertEquals(pos, expDim.getShortName(), dim.getShortName());
                }
            }
        }
    }

    private void insert_secondary() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{
                    SECONDARY_SENSOR_NAME, "1.0", "2011", "08", "23", "190546533.NSS.HIRX.NL.D11235.S1235.E1422.B5628788.WI.nc"
        }, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation satelliteObservation = readSatelliteObservation(SECONDARY_SENSOR_NAME, absolutePath, "1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_primary() throws IOException, SQLException {
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{
                    PRIMARY_SENSOR_NAME, "v1.0", "2011", "08", "23", "190543373.NSS.AMBX.NL.D11235.S1235.E1422.B5628788.WI.h5"
        }, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;
        final SatelliteObservation satelliteObservation = readSatelliteObservation(PRIMARY_SENSOR_NAME, absolutePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(PRIMARY_SENSOR_NAME);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor(SECONDARY_SENSOR_NAME));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(PRIMARY_SENSOR_NAME, AMSUB_WIN_LEN, AMSUB_WIN_LEN));
        dimensions.add(new com.bc.fiduceo.core.Dimension(SECONDARY_SENSOR_NAME, HIRS_WIN_LEN, HIRS_WIN_LEN));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd24")
                    .withSensors(sensorList)
                    .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-24").getPath())
                    .withDimensions(dimensions);
    }

    private Expectation[] getExpectations() {
        return new Expectation[]{
                      /* 00 */  new Expectation("hirs-n16_time", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 01 */  new Expectation("hirs-n16_lat", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 02 */  new Expectation("hirs-n16_lon", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 03 */  new Expectation("hirs-n16_bt_ch01", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 04 */  new Expectation("hirs-n16_bt_ch02", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 05 */  new Expectation("hirs-n16_bt_ch03", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 06 */  new Expectation("hirs-n16_bt_ch04", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 07 */  new Expectation("hirs-n16_bt_ch05", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 08 */  new Expectation("hirs-n16_bt_ch06", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 09 */  new Expectation("hirs-n16_bt_ch07", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 10 */  new Expectation("hirs-n16_bt_ch08", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 11 */  new Expectation("hirs-n16_bt_ch09", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 12 */  new Expectation("hirs-n16_bt_ch10", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 13 */  new Expectation("hirs-n16_bt_ch11", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 14 */  new Expectation("hirs-n16_bt_ch12", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 15 */  new Expectation("hirs-n16_bt_ch13", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 16 */  new Expectation("hirs-n16_bt_ch14", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 17 */  new Expectation("hirs-n16_bt_ch15", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 18 */  new Expectation("hirs-n16_bt_ch16", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 19 */  new Expectation("hirs-n16_bt_ch17", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 20 */  new Expectation("hirs-n16_bt_ch18", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 21 */  new Expectation("hirs-n16_bt_ch19", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 22 */  new Expectation("hirs-n16_lza", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 23 */  new Expectation("hirs-n16_radiance_ch01", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 24 */  new Expectation("hirs-n16_radiance_ch02", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 25 */  new Expectation("hirs-n16_radiance_ch03", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 26 */  new Expectation("hirs-n16_radiance_ch04", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 27 */  new Expectation("hirs-n16_radiance_ch05", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 28 */  new Expectation("hirs-n16_radiance_ch06", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 29 */  new Expectation("hirs-n16_radiance_ch07", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 30 */  new Expectation("hirs-n16_radiance_ch08", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 31 */  new Expectation("hirs-n16_radiance_ch09", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 32 */  new Expectation("hirs-n16_radiance_ch10", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 33 */  new Expectation("hirs-n16_radiance_ch11", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 34 */  new Expectation("hirs-n16_radiance_ch12", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 35 */  new Expectation("hirs-n16_radiance_ch13", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 36 */  new Expectation("hirs-n16_radiance_ch14", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 37 */  new Expectation("hirs-n16_radiance_ch15", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 38 */  new Expectation("hirs-n16_radiance_ch16", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 39 */  new Expectation("hirs-n16_radiance_ch17", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 40 */  new Expectation("hirs-n16_radiance_ch18", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 41 */  new Expectation("hirs-n16_radiance_ch19", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 42 */  new Expectation("hirs-n16_radiance_ch20", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 43 */  new Expectation("hirs-n16_counts_ch01", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 44 */  new Expectation("hirs-n16_counts_ch02", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 45 */  new Expectation("hirs-n16_counts_ch03", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 46 */  new Expectation("hirs-n16_counts_ch04", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 47 */  new Expectation("hirs-n16_counts_ch05", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 48 */  new Expectation("hirs-n16_counts_ch06", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 49 */  new Expectation("hirs-n16_counts_ch07", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 50 */  new Expectation("hirs-n16_counts_ch08", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 51 */  new Expectation("hirs-n16_counts_ch09", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 52 */  new Expectation("hirs-n16_counts_ch10", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 53 */  new Expectation("hirs-n16_counts_ch11", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 54 */  new Expectation("hirs-n16_counts_ch12", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 55 */  new Expectation("hirs-n16_counts_ch13", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 56 */  new Expectation("hirs-n16_counts_ch14", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 57 */  new Expectation("hirs-n16_counts_ch15", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 58 */  new Expectation("hirs-n16_counts_ch16", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 59 */  new Expectation("hirs-n16_counts_ch17", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 60 */  new Expectation("hirs-n16_counts_ch18", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 61 */  new Expectation("hirs-n16_counts_ch19", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 62 */  new Expectation("hirs-n16_counts_ch20", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 63 */  new Expectation("hirs-n16_scanline", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 64 */  new Expectation("hirs-n16_scanpos", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 65 */  new Expectation("hirs-n16_scanline_type", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),

                      /* 66 */  new Expectation("hirs-n16_x", new Dimension[]{new Dimension("matchup_count", MATCHUP_COUNT)}),
                      /* 67 */  new Expectation("hirs-n16_y", new Dimension[]{new Dimension("matchup_count", MATCHUP_COUNT)}),
                      /* 68 */  new Expectation("hirs-n16_file_name", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("file_name", FILENAME_LENGTH),
                    }),

                      /* 69 */ new Expectation("hirs-n16_processing_version", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("processing_version", PROCESSING_VERSION_LENGTH),
                    }),
                      /* 70 */ new Expectation("hirs-n16_acquisition_time", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("hirs-n16_ny", HIRS_WIN_LEN),
                    new Dimension("hirs-n16_nx", HIRS_WIN_LEN),
                    }),
                      /* 71 */  new Expectation("amsub-n16_btemps_ch16", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 72 */  new Expectation("amsub-n16_btemps_ch17", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 73 */  new Expectation("amsub-n16_btemps_ch18", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 74 */  new Expectation("amsub-n16_btemps_ch19", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 75 */  new Expectation("amsub-n16_btemps_ch20", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 76 */  new Expectation("amsub-n16_chanqual_ch16", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 77 */  new Expectation("amsub-n16_chanqual_ch17", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 78 */  new Expectation("amsub-n16_chanqual_ch18", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 79 */  new Expectation("amsub-n16_chanqual_ch19", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 80 */  new Expectation("amsub-n16_chanqual_ch20", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 81 */  new Expectation("amsub-n16_instrtemp", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 82 */  new Expectation("amsub-n16_qualind", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 83 */  new Expectation("amsub-n16_scanqual", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 84 */  new Expectation("amsub-n16_scnlin", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 85 */  new Expectation("amsub-n16_scnlindy", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 86 */  new Expectation("amsub-n16_scnlintime", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 87 */  new Expectation("amsub-n16_scnlinyr", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 88 */  new Expectation("amsub-n16_Latitude", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 89 */  new Expectation("amsub-n16_Longitude", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 90 */  new Expectation("amsub-n16_Satellite_azimuth_angle", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 91 */  new Expectation("amsub-n16_Satellite_zenith_angle", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 92 */  new Expectation("amsub-n16_Solar_azimuth_angle", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 93 */  new Expectation("amsub-n16_Solar_zenith_angle", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    }),
                      /* 94 */  new Expectation("amsub-n16_x", new Dimension[]{new Dimension("matchup_count", MATCHUP_COUNT)}),
                      /* 95 */  new Expectation("amsub-n16_y", new Dimension[]{new Dimension("matchup_count", MATCHUP_COUNT)}),
                      /* 96 */  new Expectation("amsub-n16_file_name", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("file_name", FILENAME_LENGTH),
                    }),
                      /* 97 */  new Expectation("amsub-n16_processing_version", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("processing_version", PROCESSING_VERSION_LENGTH),
                    }),
                      /* 98 */  new Expectation("amsub-n16_acquisition_time", new Dimension[]{
                    new Dimension("matchup_count", MATCHUP_COUNT),
                    new Dimension("amsub-n16_ny", AMSUB_WIN_LEN),
                    new Dimension("amsub-n16_nx", AMSUB_WIN_LEN),
                    })
        };
    }

    static class Expectation {

        final String name;
        final Dimension[] dimensions;

        public Expectation(String name, Dimension[] dimensions) {
            this.name = name;
            this.dimensions = dimensions;
        }
    }
}
