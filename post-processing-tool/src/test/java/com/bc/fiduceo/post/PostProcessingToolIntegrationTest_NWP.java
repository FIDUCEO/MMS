/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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

package com.bc.fiduceo.post;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.post.plugin.nwp.CDOTestRunner;
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

@RunWith(CDOTestRunner.class)
public class PostProcessingToolIntegrationTest_NWP {

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
    public void testAddNWPVariables() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2004-008", "-end", "2004-012",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

//        final File targetFile = new File(testDirectory, "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc");
//        assertTrue(targetFile.isFile());
//
//        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
//            NCTestUtils.assert3DVariable("amsre.solar_zenith_angle", 0, 0, 0, 104.08000183105469, mmd);
//            NCTestUtils.assert3DVariable("amsre.solar_zenith_angle", 1, 0, 0, 103.97999572753906, mmd);
//
//            NCTestUtils.assert3DVariable("amsre.solar_azimuth_angle", 2, 0, 0, -11.169998168945312, mmd);
//            NCTestUtils.assert3DVariable("amsre.solar_azimuth_angle", 3, 0, 0, -11.29998779296875, mmd);
//        }
    }

    private void writeConfiguration() throws IOException {
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <nwp>\n" +
                "            <cdo-home>/usr/local/bin/cdo</cdo-home>\n" +
                "            <nwp-aux-dir>/the/auxiliary/files</nwp-aux-dir>\n" +
                "            <delete-on-exit>true</delete-on-exit>\n" +
                "            <analysis-steps>19</analysis-steps>\n" +
                "            <forecast-steps>33</forecast-steps>\n" +
                "            <time-variable-name>acquisition-time</time-variable-name>\n" +
                "            <longitude-variable-name>acquisition-time</longitude-variable-name>\n" +
                "            <latitude-variable-name>acquisition-time</latitude-variable-name>\n" +
                "        </nwp>" +
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
        return new File(testDataDirectory, "post-processing/mmd06c");
    }
}
