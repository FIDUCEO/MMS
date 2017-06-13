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
public class PostProcessingToolIntegrationTest_IASISpectrum {

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
    public void testAddIASISpectrum() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2014-115", "-end", "2014-115",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd03_iasi-mb_avhrr-n19_2014-115_2014-115.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertDimension("iasi_ss", 8700, mmd);

            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 0, 0, 0, 0, 5.123000009916723E-4, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 1, 0, 0, 0, 5.151000223122537E-4, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 2, 0, 0, 0, 5.19599998369813E-4, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 3, 0, 0, 0, 5.481999833136797E-4, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 4350, 0, 0, 0, 1.6140000298037194E-5, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 4351, 0, 0, 0, 1.4739999642188195E-5, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 8698, 0, 0, 0, 9.969209968386869E36, mmd);
            NCTestUtils.assert4DVariable("iasi-mb_GS1cSpect", 8699, 0, 0, 0, 9.969209968386869E36, mmd);
        }
    }

    private void writeConfiguration() throws IOException {
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <add-iasi-spectrum>\n" +
                "            <target-variable name=\"iasi-mb_GS1cSpect\" />" +
                "            <reference-variable name=\"iasi-mb_OnboardUTC\" />" +
                "            <x-variable name=\"iasi-mb_x\" />" +
                "            <y-variable name=\"iasi-mb_y\" />" +
                "            <file-name-variable name=\"iasi-mb_file_name\" />" +
                "            <processing-version-variable name=\"iasi-mb_processing_version\" />" +
                "        </add-iasi-spectrum>" +
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
        final File postProcessingDir = new File(testDataDirectory, "post-processing");
        return new File(postProcessingDir, "mmd03");
    }
}
