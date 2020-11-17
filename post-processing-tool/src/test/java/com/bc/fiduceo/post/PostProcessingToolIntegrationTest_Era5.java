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

package com.bc.fiduceo.post;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class PostProcessingToolIntegrationTest_Era5 {

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
    public void testAddEra5Variables() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            Variable variable = NCTestUtils.getVariable("amsre\\.Geostationary_Reflection_Latitude", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 4105, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 4087, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 7, mmd);
            NCTestUtils.assertDimension("left", 5, mmd);
            NCTestUtils.assertDimension("right", 7, mmd);
            NCTestUtils.assertDimension("up", 23, mmd);

           // @todo 1 tb/tb add assertions
//
            variable = NCTestUtils.getVariable("nwp_q", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
//            NCTestUtils.assert3DValueDouble(2, 0, 0, 183.0, variable);
//            NCTestUtils.assert3DValueDouble(3, 0, 0, 177.0, variable);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Logarithm of surface pressure");

            variable = NCTestUtils.getVariable("nwp_v10", mmd);
            assertNull(variable.findAttribute("standard_name"));

            variable = NCTestUtils.getVariable("nwp_sst", mmd);
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.969209968386869E36");

            variable = NCTestUtils.getVariable("era5-time", mmd);
            NCTestUtils.assertAttribute(variable, "units", "seconds since 1970-01-01");
        }
    }

    private void writeConfiguration() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era5/v1");
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <era5>\n" +
                "            <nwp-aux-dir>\n" +
                era5Dir.getAbsolutePath() +
                "            </nwp-aux-dir>\n"+
                "            <satellite-fields>" +
                "                <x_dim name='left' length='5' />" +
                "                <y_dim name='right' length='7' />" +
                "                <z_dim name='up' length='23' />" +
                "                <era5_time_variable_name>era5-time</era5_time_variable_name>" +
                "            </satellite-fields>" +
                "            <matchup-fields>" +
                "            </matchup-fields>" +
                "        </era5>\n" +
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
        return new File(testDataDirectory, "post-processing/mmd15sst");
    }
}
