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
public class PostProcessingToolIntegrationTest_AddAmsr2Quality {

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
    public void testAddAmsr2QualiytData() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2013-181", "-end", "2013-183",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd6b_SST_gtmba-sst_amsr2-gcw1_2013-181_2013-183.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertDimension("scan_data_quality", 512, mmd);

            NCTestUtils.assert2DVariable("amsr2-gcw1_Scan_Data_Quality", 0, 1, 72, mmd);
            NCTestUtils.assert2DVariable("amsr2-gcw1_Scan_Data_Quality", 1, 2, 44, mmd);
            NCTestUtils.assert2DVariable("amsr2-gcw1_Scan_Data_Quality", 2, 3, -23, mmd);
            NCTestUtils.assert2DVariable("amsr2-gcw1_Scan_Data_Quality", 3, 4, 66, mmd);
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
                "        <add-amsr2-scan-data-quality>\n" +
                "            <target-variable name=\"amsr2-gcw1_Scan_Data_Quality\" />" +
                "            <filename-variable name=\"amsr2-gcw1_file_name\" />" +
                "            <processing-version-variable name=\"amsr2-gcw1_processing_version\" />" +
                "            <y-variable name=\"amsr2-gcw1_y\" />" +
                "        </add-amsr2-scan-data-quality>\n" +
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
        return new File(testDataDirectory, "post-processing/mmd06b_sst");
    }
}
