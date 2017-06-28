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

package com.bc.fiduceo.post;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import org.apache.commons.cli.ParseException;
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
public class PostProcessingToolIntegrationTest_DistanceToLand {

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
    public void testAddDistanceToLand() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration();

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

    private void writeConfiguration() throws IOException {
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

    private void writeConfiguration_sensorExtract() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File eraInterimDir = new File(testDataDirectory, "era-interim/v1");
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <nwp>\n" +
                "            <cdo-home>/home/tom/Dev/cdo_installation/bin</cdo-home>\n" + // @todo 2 tb/tb move to test-config 2017-01-11
                "            <nwp-aux-dir>" + eraInterimDir.getAbsolutePath() + "</nwp-aux-dir>\n" +
                "            <delete-on-exit>true</delete-on-exit>\n" +
                "\n" +
                "            <sensor-extraction>\n" +
                "                <time-variable-name>amsre.acquisition_time</time-variable-name>\n" +
                "                <x-dimension>5</x-dimension>\n" +
                "                <x-dimension-name>amsre.nwp.nx</x-dimension-name>\n" +
                "                <y-dimension>5</y-dimension>\n" +
                "                <y-dimension-name>amsre.nwp.ny</y-dimension-name>\n" +
                "                <z-dimension>60</z-dimension>\n" +
                "                <z-dimension-name>amsre.nwp.nz</z-dimension-name>\n" +
                "                <longitude-variable-name>amsre.longitude</longitude-variable-name>\n" +
                "                <latitude-variable-name>amsre.latitude</latitude-variable-name>\n" +
                "            </sensor-extraction>\n" +
                "        </nwp>\n" +
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
