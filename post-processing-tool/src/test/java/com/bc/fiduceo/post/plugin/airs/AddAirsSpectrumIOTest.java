/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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
package com.bc.fiduceo.post.plugin.airs;

import static org.junit.Assert.*;
import static ucar.nc2.NetcdfFileWriter.Version.netcdf4;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessingToolMain;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.*;
import org.junit.runner.*;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(IOTestRunner.class)
public class AddAirsSpectrumIOTest {

    final int cutOutHeight = 7;
    final int cutOutWidth = 5;
    final String matchupFileName = "mmd9_9_copy_from_AIRS_TEST_MATCHUP_2010-004_2010-008.nc";
    final int numMatchups = 9;
    final int numChannels = 2378;

    Path testDirRoot;
    Path matchupDir;
    Path postProcOut;
    Path configDir;
    Path ppConfigFile;

    @Before
    public void setUp() throws Exception {
        testDirRoot = TestUtil.createTestDirectory().toPath().resolve("usecase-airs-post-processing");
        createMatchupFile();
        createPostProcessingDir();
        initConfigDirAndFiles();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testAddAirsSpectrum() throws IOException, InvalidRangeException {
        PostProcessingToolMain.main(new String[]{
                "-c", configDir.toString(),
                "-i", matchupDir.toString(),
                "-j", ppConfigFile.getFileName().toString(),
                "-start", "2010-001",
                "-end", "2010-365"
        });

        final Path expectedPostProcFile = postProcOut.resolve(matchupFileName);
        assertTrue(Files.exists(expectedPostProcFile));

        final int[] shapeExpected = {numMatchups, cutOutHeight, cutOutWidth, numChannels};

        try (NetcdfFile createdPPFile = NetCDFUtils.openReadOnly(expectedPostProcFile.toAbsolutePath().toString())) {
            final Variable varRadiances = createdPPFile.findVariable(null, "airs-aq_radiances");
            assertNotNull(varRadiances);
            assertEquals(DataType.FLOAT, varRadiances.getDataType());
            assertArrayEquals(shapeExpected, varRadiances.getShape());
            final Map<int[], float[]> expecteds = getExpectedRadianceValues();
            for (Map.Entry<int[], float[]> entry : expecteds.entrySet()) {
                final int matchup = entry.getKey()[0];
                final int channel = entry.getKey()[1];
                final float[] expValues = entry.getValue();
                final Array read = varRadiances.read(new int[]{matchup, 0, 0, channel}, new int[]{1, cutOutHeight, cutOutWidth, 1});
                assertArrayEquals("Failed at matchup: " + matchup + " channel: " + channel, expValues, (float[]) read.getStorage(), 0.00000001f);
            }

            final Variable varCalFlag = createdPPFile.findVariable(null, "airs-aq_CalFlag");
            assertNotNull(varCalFlag);
            assertEquals(DataType.BYTE, varCalFlag.getDataType());
            assertArrayEquals(shapeExpected, varCalFlag.getShape());
            final Map<int[], byte[]> expectedCalFlagValues = getExpectedCalFlagValues();
            for (Map.Entry<int[], byte[]> entry : expectedCalFlagValues.entrySet()) {
                final int matchup = entry.getKey()[0];
                final int channel = entry.getKey()[1];
                final byte[] expValues = entry.getValue();
                final Array read = varCalFlag.read(new int[]{matchup, 0, 0, channel}, new int[]{1, cutOutHeight, cutOutWidth, 1});
                assertArrayEquals("Failed at matchup: " + matchup + " channel: " + channel, expValues, (byte[]) read.getStorage());
            }

            final Variable varSpaceViewDelta = createdPPFile.findVariable(null, "airs-aq_spaceViewDelta");
            assertNotNull(varSpaceViewDelta);
            assertEquals(DataType.FLOAT, varSpaceViewDelta.getDataType());
            assertArrayEquals(shapeExpected, varSpaceViewDelta.getShape());
            final Map<int[], float[]> expectedSpaceViewDeltaValues = getExpectedSpaceViewDeltaValues();
            for (Map.Entry<int[], float[]> entry : expectedSpaceViewDeltaValues.entrySet()) {
                final int matchup = entry.getKey()[0];
                final int channel = entry.getKey()[1];
                final float[] expValues = entry.getValue();
                final Array read = varSpaceViewDelta.read(new int[]{matchup, 0, 0, channel}, new int[]{1, cutOutHeight, cutOutWidth, 1});
                assertArrayEquals("Failed at matchup: " + matchup + " channel: " + channel, expValues, (float[]) read.getStorage(), 0.00000001f);
            }
        }

    }

    private Map<int[], float[]> getExpectedRadianceValues() {
        final Map<int[], float[]> expecteds = new LinkedHashMap<>();

        int matchup = 0; // y=1 x=1
        int channel = 0;
        final float fill = -9999.0f;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 53.75f, 51.75f, 53.0f, 53.25f,
                        fill, 53.5f, 52.25f, 52.5f, 52.75f,
                        fill, 54f, 53.25f, 51.75f, 53.25f,
                        fill, 53.5f, 52.75f, 52.25f, 52.75f,
                        fill, 52.75f, 52.25f, 52.5f, 53.75f
                });
        channel = 888;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 87.25f, 86.5f, 87.25f, 88.25f,
                        fill, 87.5f, 87.75f, 88.0f, 88.5f,
                        fill, 88.75f, 87.0f, 88.75f, 87.25f,
                        fill, 88.0f, 88.0f, 87.75f, 87.75f,
                        fill, 89.0f, 88.75f, 87.25f, 87.25f

                });
        channel = numChannels - 1;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 0.40039062f, 0.39111328f, 0.39697266f, 0.41357422f,
                        fill, 0.41796875f, 0.3935547f, 0.3955078f, 0.39941406f,
                        fill, 0.41210938f, 0.3955078f, 0.39697266f, 0.4008789f,
                        fill, 0.40429688f, 0.39746094f, 0.39648438f, 0.39990234f,
                        fill, 0.40429688f, 0.3955078f, 0.3984375f, 0.4033203f

                });
        matchup = 1; // y=1 x=45
        channel = 1212;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        23.75f, 23.125f, 20.125f, 20.625f, 20.5625f,
                        20.0f, 22.25f, 26.0f, 22.875f, 25.25f,
                        22.6875f, 21.1875f, 26.875f, 28.6875f, 33.5625f,
                        20.375f, 22.3125f, 26.4375f, 27.0625f, 34.5625f,
                        24.25f, 24.625f, 31.0f, 37.3125f, 38.25f

                });
        matchup = 2; // y=1 x=88
        channel = 914;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        89.0f, 89.0f, 81.0f, 48.0f, fill,
                        89.0f, 88.5f, 73.5f, 46.5f, fill,
                        87.0f, 86.5f, 66.0f, 44.5f, fill,
                        89.0f, 89.0f, 78.5f, 52.0f, fill,
                        87.5f, 89.0f, 78.0f, 51.0f, fill
                });
        matchup = 3; // y=65 x=1
        channel = 2222;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, 0.8222656f, 0.79785156f, 0.80566406f, 0.8261719f,
                        fill, 0.7988281f, 0.81640625f, 0.84277344f, 0.84472656f,
                        fill, 0.8232422f, 0.86035156f, 0.8623047f, 0.85546875f,
                        fill, 0.8251953f, 0.8623047f, 0.86816406f, 0.8701172f,
                        fill, 0.8310547f, 0.85839844f, 0.84521484f, 0.87890625f,
                        fill, 0.82128906f, 0.84472656f, 0.8588867f, 0.9091797f,
                        fill, 0.828125f, 0.82910156f, 0.8642578f, 0.86328125f

                });
        matchup = 4; // y=65 x=45
        channel = 1824;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        6.6875f, 6.625f, 6.78125f, 6.8125f, 6.84375f,
                        6.78125f, 6.6875f, 6.796875f, 6.890625f, 6.90625f,
                        6.78125f, 6.84375f, 6.796875f, 6.921875f, 6.921875f,
                        6.96875f, 6.890625f, 6.859375f, 6.9375f, 7.0f,
                        7.09375f, 7.078125f, 7.15625f, 7.109375f, 7.078125f,
                        7.28125f, 7.3125f, 7.3125f, 7.28125f, 7.203125f,
                        7.3125f, 7.390625f, 7.328125f, 7.203125f, 7.21875f
                });
        matchup = 5; // y=65 x=88
        channel = 1234;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        61.625f, 63.8125f, 54.25f, 49.0f, fill,
                        63.25f, 62.75f, 61.5625f, 63.125f, fill,
                        60.3125f, 50.625f, 63.375f, 63.0f, fill,
                        47.75f, 51.75f, 63.3125f, 62.0625f, fill,
                        60.75f, 58.25f, 61.375f, 60.75f, fill,
                        63.75f, 64.375f, 59.6875f, 48.75f, fill,
                        62.25f, 60.0f, 57.375f, 56.875f, fill
                });
        matchup = 6; // y=133 x=1
        channel = 636;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, 109.5f, 106.375f, 106.125f, 106.0f,
                        fill, 106.875f, 105.75f, 104.625f, 106.875f,
                        fill, 108.125f, 107.25f, 108.375f, 107.625f,
                        fill, 108.75f, 107.375f, 108.25f, 108.0f,
                        fill, 107.375f, 108.875f, 110.875f, 110.625f,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });
        matchup = 7; // y=133 x=45
        channel = 163;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        38.25f, 38.5f, 38.5f, 38.5f, 38.75f,
                        38.25f, 38.25f, 37.875f, 39.125f, 38.25f,
                        38.5f, 38.0f, 39.125f, 38.0f, 38.125f,
                        38.375f, 38.0f, 38.0f, 38.75f, 37.75f,
                        38.75f, 39.0f, 38.25f, 38.25f, 37.75f,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });
        matchup = 8; // y=133 x=88
        channel = 1777;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        1.7421875f, 2.84375f, 3.5078125f, 3.5078125f, fill,
                        2.28125f, 2.734375f, 3.5078125f, 3.578125f, fill,
                        2.328125f, 2.65625f, 3.5390625f, 3.234375f, fill,
                        2.390625f, 2.875f, 3.5078125f, 3.359375f, fill,
                        2.421875f, 2.921875f, 3.578125f, 3.828125f, fill,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });
        return expecteds;
    }

    private Map<int[], byte[]> getExpectedCalFlagValues() {
        final Map<int[], byte[]> expecteds = new LinkedHashMap<>();

        final byte fill = -1;

        int matchup = 0; // y=1 x=1
        int channel = 0;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0
                });
        matchup = 1; // y=1 x=45
        channel = 737;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        16, 16, 16, 16, 16,
                        16, 16, 16, 16, 16,
                        0, 0, 0, 0, 0
                });
        matchup = 2; // y=1 x=88
        channel = 2358;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        32, 32, 32, 32, fill,
                        32, 32, 32, 32, fill,
                        32, 32, 32, 32, fill,
                        32, 32, 32, 32, fill,
                        32, 32, 32, 32, fill
                });

        matchup = 3; // y=65 x=1
        channel = 1841;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0,
                        fill, 16, 16, 16, 16,
                        fill, 16, 16, 16, 16,
                        fill, 16, 16, 16, 16,
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0
                });

        matchup = 4; // y=65 x=45
        channel = 828;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        16, 16, 16, 16, 16,
                        16, 16, 16, 16, 16,
                        0, 0, 0, 0, 0,
                        16, 16, 16, 16, 16,
                        16, 16, 16, 16, 16
                });

        matchup = 5; // y=65 x=88
        channel = 480;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        16, 16, 16, 16, fill,
                        0, 0, 0, 0, fill,
                        0, 0, 0, 0, fill,
                        0, 0, 0, 0, fill,
                        0, 0, 0, 0, fill,
                        16, 16, 16, 16, fill,
                        0, 0, 0, 0, fill
                });

        matchup = 6; // y=133 x=1
        channel = 1100;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        fill, 0, 0, 0, 0,
                        fill, 0, 0, 0, 0,
                        fill, 16, 16, 16, 16,
                        fill, 0, 0, 0, 0,
                        fill, 16, 16, 16, 16,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        matchup = 7; // y=133 x=45
        channel = 1148;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0,
                        16, 16, 16, 16, 16,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        matchup = 8; // y=133 x=88
        channel = 1194;
        expecteds.put(
                new int[]{matchup, channel},
                new byte[]{
                        0, 0, 0, 0, fill,
                        16, 16, 16, 16, fill,
                        0, 0, 0, 0, fill,
                        0, 0, 0, 0, fill,
                        16, 16, 16, 16, fill,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        return expecteds;
    }

    private Map<int[], float[]> getExpectedSpaceViewDeltaValues() {
        final Map<int[], float[]> expecteds = new LinkedHashMap<>();

        final float fill = -9999.0f;

        int matchup = 0; // y=1 x=1
        int channel = 0;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 10.0f, 10.0f, 10.0f, 10.0f,
                        fill, -12.0f, -12.0f, -12.0f, -12.0f,
                        fill, 6.0f, 6.0f, 6.0f, 6.0f,
                        fill, -4.0f, -4.0f, -4.0f, -4.0f,
                        fill, -2.0f, -2.0f, -2.0f, -2.0f
                });
        channel = 1448;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 0.0f, 0.0f, 0.0f, 0.0f,
                        fill, 1.0f, 1.0f, 1.0f, 1.0f,
                        fill, -2.0f, -2.0f, -2.0f, -2.0f,
                        fill, 2.0f, 2.0f, 2.0f, 2.0f,
                        fill, -1.0f, -1.0f, -1.0f, -1.0f
                });
        channel = numChannels - 1;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        fill, 0.0f, 0.0f, 0.0f, 0.0f,
                        fill, -2.5f, -2.5f, -2.5f, -2.5f,
                        fill, 2.0f, 2.0f, 2.0f, 2.0f,
                        fill, 1.5f, 1.5f, 1.5f, 1.5f,
                        fill, -2.0f, -2.0f, -2.0f, -2.0f
                });
        matchup = 1; // y=1 x=45
        channel = 737;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        11.0f, 11.0f, 11.0f, 11.0f, 11.0f,
                        14.5f, 14.5f, 14.5f, 14.5f, 14.5f,
                        -31.0f, -31.0f, -31.0f, -31.0f, -31.0f,
                        75.0f, 75.0f, 75.0f, 75.0f, 75.0f,
                        -1.5f, -1.5f, -1.5f, -1.5f, -1.5f
                });
        matchup = 2; // y=1 x=88
        channel = 2356;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill,
                        9.0f, 9.0f, 9.0f, 9.0f, fill,
                        -86.5f, -86.5f, -86.5f, -86.5f, fill,
                        45.0f, 45.0f, 45.0f, 45.0f, fill,
                        -94.0f, -94.0f, -94.0f, -94.0f, fill,
                        26.5f, 26.5f, 26.5f, 26.5f, fill
                });

        matchup = 3; // y=65 x=1
        channel = 1841;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, -21.0f, -21.0f, -21.0f, -21.0f,
                        fill, 12.0f, 12.0f, 12.0f, 12.0f,
                        fill, -107.5f, -107.5f, -107.5f, -107.5f,
                        fill, 83.0f, 83.0f, 83.0f, 83.0f,
                        fill, -41.5f, -41.5f, -41.5f, -41.5f,
                        fill, 97.5f, 97.5f, 97.5f, 97.5f,
                        fill, -46.0f, -46.0f, -46.0f, -46.0f
                });

        matchup = 4; // y=65 x=45
        channel = 828;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                        16.5f, 16.5f, 16.5f, 16.5f, 16.5f,
                        -218.0f, -218.0f, -218.0f, -218.0f, -218.0f,
                        236.5f, 236.5f, 236.5f, 236.5f, 236.5f,
                        92.0f, 92.0f, 92.0f, 92.0f, 92.0f,
                        -172.5f, -172.5f, -172.5f, -172.5f, -172.5f,
                        311.5f, 311.5f, 311.5f, 311.5f, 311.5f
                });

        matchup = 5; // y=65 x=88
        channel = 480;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        166.0f, 166.0f, 166.0f, 166.0f, fill,
                        -27.0f, -27.0f, -27.0f, -27.0f, fill,
                        157.0f, 157.0f, 157.0f, 157.0f, fill,
                        46.5f, 46.5f, 46.5f, 46.5f, fill,
                        53.5f, 53.5f, 53.5f, 53.5f, fill,
                        -170.0f, -170.0f, -170.0f, -170.0f, fill,
                        169.5f, 169.5f, 169.5f, 169.5f, fill
                });

        matchup = 6; // y=133 x=1
        channel = 1001;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        fill, -1.5f, -1.5f, -1.5f, -1.5f,
                        fill, -1.5f, -1.5f, -1.5f, -1.5f,
                        fill, 0.0f, 0.0f, 0.0f, 0.0f,
                        fill, -1.0f, -1.0f, -1.0f, -1.0f,
                        fill, 0.5f, 0.5f, 0.5f, 0.5f,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        matchup = 7; // y=133 x=45
        channel = 1452;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        5.0f, 5.0f, 5.0f, 5.0f, 5.0f,
                        -2.5f, -2.5f, -2.5f, -2.5f, -2.5f,
                        -3.5f, -3.5f, -3.5f, -3.5f, -3.5f,
                        2.5f, 2.5f, 2.5f, 2.5f, 2.5f,
                        8.5f, 8.5f, 8.5f, 8.5f, 8.5f,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        matchup = 8; // y=133 x=88
        channel = 1353;
        expecteds.put(
                new int[]{matchup, channel},
                new float[]{
                        -29.0f, -29.0f, -29.0f, -29.0f, fill,
                        20.5f, 20.5f, 20.5f, 20.5f, fill,
                        -21.5f, -21.5f, -21.5f, -21.5f, fill,
                        -7.0f, -7.0f, -7.0f, -7.0f, fill,
                        18.0f, 18.0f, 18.0f, 18.0f, fill,
                        fill, fill, fill, fill, fill,
                        fill, fill, fill, fill, fill
                });

        return expecteds;
    }

    private void initConfigDirAndFiles() throws IOException {
        configDir = testDirRoot.resolve("config");
        Files.createDirectories(configDir);
        TestUtil.writeSystemConfig(configDir.toFile());
        ppConfigFile = configDir.resolve("post-processing-config.xml");

        Files.deleteIfExists(ppConfigFile);
        OutputStream os = Files.newOutputStream(ppConfigFile);
        final PrintWriter pw = new PrintWriter(os);

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<post-processing-config>");
        pw.println("    <create-new-files>");
        pw.println("        <output-directory>" + postProcOut.toString() + "</output-directory>");
        pw.println("    </create-new-files>");
        pw.println("    <post-processings>");
        pw.println("        <add-airs-channel-data>");
        pw.println("            <mmd-source-file-variable-name>airs-aq_file_name</mmd-source-file-variable-name>");
        pw.println("            <mmd-processing-version-variable-name>airs-aq_processing_version</mmd-processing-version-variable-name>");
        pw.println("            <mmd-x-variable-name>airs-aq_x</mmd-x-variable-name>");
        pw.println("            <mmd-y-variable-name>airs-aq_y</mmd-y-variable-name>");
        pw.println("            <mmd-variable-name-cut-out-reference>airs-aq_cutOutRef</mmd-variable-name-cut-out-reference>");
        pw.println("            <target-variable-name-radiances>airs-aq_radiances</target-variable-name-radiances>");
        pw.println("            <target_variable_name_CalFlag>airs-aq_CalFlag</target_variable_name_CalFlag>");
        pw.println("            <target_variable_name_SpaceViewDelta>airs-aq_spaceViewDelta</target_variable_name_SpaceViewDelta>");
        pw.println("        </add-airs-channel-data>");
        pw.println("    </post-processings>");
        pw.println("</post-processing-config>");
        pw.flush();
        pw.close();
    }

    private void createPostProcessingDir() throws IOException {
        postProcOut = testDirRoot.resolve("post-proc-out");
        Files.createDirectory(postProcOut);
    }

    private void createMatchupFile() throws IOException, InvalidRangeException {
        matchupDir = testDirRoot.resolve("matchup-dir");
        Files.createDirectories(matchupDir);
        final Path path = matchupDir.resolve(matchupFileName);
        final NetcdfFileWriter ncWriter = NetcdfFileWriter.createNew(netcdf4, path.toString());

        final Dimension dimNX = ncWriter.addDimension(null, "airs-aq_nx", cutOutWidth);
        final Dimension dimNY = ncWriter.addDimension(null, "airs-aq_ny", cutOutHeight);
        final Dimension dimFN = ncWriter.addDimension(null, "file_name", 128);
        final Dimension dimPV = ncWriter.addDimension(null, "processing_version", 30);
        final Dimension dimMC = ncWriter.addDimension(null, "matchup_count", numMatchups);
        final Variable varFN = ncWriter.addVariable(null, "airs-aq_file_name", DataType.CHAR, Arrays.asList(dimMC, dimFN));
        varFN.addAttribute(new Attribute("description", "file name of the original data file"));
        final Variable varPV = ncWriter.addVariable(null, "airs-aq_processing_version", DataType.CHAR, Arrays.asList(dimMC, dimPV));
        varPV.addAttribute(new Attribute("description", "the processing version of the original data file"));
        final Variable varX = ncWriter.addVariable(null, "airs-aq_x", DataType.INT, Arrays.asList(dimMC));
        final Variable varY = ncWriter.addVariable(null, "airs-aq_y", DataType.INT, Arrays.asList(dimMC));
        final Variable varTopog = ncWriter.addVariable(null, "airs-aq_cutOutRef", DataType.FLOAT, Arrays.asList(dimMC, dimNY, dimNX));

        ncWriter.create();

        ncWriter.write(varX, Array.factory(new int[]{
                1, 45, 88,
                1, 45, 88,
                1, 45, 88,
                }));
        ncWriter.write(varY, Array.factory(new int[]{
                1, 1, 1,
                65, 65, 65,
                133, 133, 133,
                }));

        final char[][] file = {"AIRS.2010.01.07.001.L1B.AIRS_Rad.v5.0.0.0.G10007112420.hdf".toCharArray()};
        final char[][] version = {"v5.0.0.0".toCharArray()};

        final Array arrayF = Array.factory(file);
        final Array arrayV = Array.factory(version);

        for (int i = 0; i < 9; i++) {
            ncWriter.write(varFN, new int[]{i, 0}, arrayF);
            ncWriter.write(varPV, new int[]{i, 0}, arrayV);
        }
        ncWriter.flush();
        ncWriter.close();
    }
}