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

package com.bc.fiduceo.post.plugin.land_distance;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.PostProcessingToolMain;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class AddLandDistance_IntegrationTest {

    private File configDir;
    private File testDirectory;

    @Before
    public void setUp() throws IOException {
        testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testAddDistanceToLand_singleSensor() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration_singleSensor();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-259", "-end", "2008-265",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd06_hirs-n15_iasi-ma_2008-259_2008-265.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 0, 687.4000244140625, mmd);
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 1, 615.2000122070312, mmd);
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 2, 686.2999877929688, mmd);
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 3, 657.6000366210938, mmd);
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 4, 709.0, mmd);
            NCTestUtils.assert3DVariable("distance-to-land", 0, 0, 5, 741.0, mmd);
        }
    }

    @Test
    public void testAddDistanceToLand_bothSensors() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration_bothSensors();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-259", "-end", "2008-265",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd06_hirs-n15_iasi-ma_2008-259_2008-265.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 0, 707.7000122070312, mmd);
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 1, 634.7000122070312, mmd);
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 2, 693.7999877929688, mmd);
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 3, 629.6000366210938, mmd);
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 4, 676.1000366210938, mmd);
            NCTestUtils.assert3DVariable("hirs-distance-to-land", 0, 1, 5, 709.6000366210938, mmd);

            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 0, 718.5, mmd);
            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 1, 619.7000122070312, mmd);
            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 2, 674.1000366210938, mmd);
            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 3, 601.2000122070312, mmd);
            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 4, 659.2999877929688, mmd);
            NCTestUtils.assert3DVariable("iasi-distance-to-land", 0, 0, 5, 706.7999877929688, mmd);
        }
    }

    private void writeConfiguration_singleSensor() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File distanceToLandFile = new File(testDataDirectory, "distance_to_land_map/Globolakes-static_distance_to_land_Map-300m-P5Y-2005-ESACCI_WB-fv1.0_RES120.nc");
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <add-distance-to-land>\n" +
                "            <aux-file-path>" + distanceToLandFile.getAbsolutePath() + "</aux-file-path>\n" +
                "            <target-variable name=\"distance-to-land\" />" +
                "            <lon-variable name=\"hirs-n15_lon\" />" +
                "            <lat-variable name=\"hirs-n15_lat\" />" +
                "        </add-distance-to-land>" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private void writeConfiguration_bothSensors() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File distanceToLandFile = new File(testDataDirectory, "distance_to_land_map/Globolakes-static_distance_to_land_Map-300m-P5Y-2005-ESACCI_WB-fv1.0_RES120.nc");
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <add-distance-to-land>\n" +
                "            <aux-file-path>" + distanceToLandFile.getAbsolutePath() + "</aux-file-path>\n" +
                "            <target-variable name=\"hirs-distance-to-land\" />" +
                "            <lon-variable name=\"hirs-n15_lon\" />" +
                "            <lat-variable name=\"hirs-n15_lat\" />" +
                "        </add-distance-to-land>" +
                "        <add-distance-to-land>\n" +
                "            <aux-file-path>" + distanceToLandFile.getAbsolutePath() + "</aux-file-path>\n" +
                "            <target-variable name=\"iasi-distance-to-land\" />" +
                "            <lon-variable name=\"iasi-ma_GGeoSondLoc_Lon\" />" +
                "            <lat-variable name=\"iasi-ma_GGeoSondLoc_Lat\" />" +
                "        </add-distance-to-land>" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private File getInputDirectory() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing/mmd06_sst");
    }
}
