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

import static org.junit.Assert.*;

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
    public void testAddEra5Variables_mmd15() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_mmd15();

        writeConfiguration_mmd15();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("amsre\\.Geostationary_Reflection_Latitude", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 4105, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 4087, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 7, mmd);

            // satellite fields
            NCTestUtils.assertDimension("left", 5, mmd);
            NCTestUtils.assertDimension("right", 7, mmd);
            NCTestUtils.assertDimension("up", 23, mmd);

            variable = NCTestUtils.getVariable("nwp_q", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 0, 0, 2.067875129796448E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 10, 0, 4.002843979833415E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 20, 0, 3.6158501188765513E-6, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Logarithm of surface pressure");
            NCTestUtils.assert3DValueDouble(3, 1, 1, 11.53063678741455, variable);
            NCTestUtils.assert3DValueDouble(3, 2, 1, 11.530539512634277, variable);
            NCTestUtils.assert3DValueDouble(3, 3, 1, 11.530522346496582, variable);

            variable = NCTestUtils.getVariable("nwp_v10", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert3DValueDouble(4, 2, 2, 0.9060587882995605, variable);
            NCTestUtils.assert3DValueDouble(4, 3, 2, 1.063584566116333, variable);
            NCTestUtils.assert3DValueDouble(4, 4, 2, 1.2027971744537354, variable);

            variable = NCTestUtils.getVariable("nwp_sst", mmd);
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.969209968386869E36");
            NCTestUtils.assert3DValueDouble(0, 3, 3, 275.75360107421875, variable);
            NCTestUtils.assert3DValueDouble(0, 4, 3, 275.5603942871094, variable);
            NCTestUtils.assert3DValueDouble(0, 5, 3, 275.3390808105469, variable);

            variable = NCTestUtils.getVariable("era5-time", mmd);
            NCTestUtils.assertAttribute(variable, "units", "seconds since 1970-01-01");
            NCTestUtils.assert1DValueLong(2, 1212400800, variable);
            NCTestUtils.assert1DValueLong(6, 1212145200, variable);

            // matchup fields
            NCTestUtils.assertDimension("the_time", 54, mmd);

            variable = NCTestUtils.getVariable("era5-mu-time", mmd);
            NCTestUtils.assertAttribute(variable, "units", "seconds since 1970-01-01");
            NCTestUtils.assert2DValueInt(1, 1, 959796000, variable);
            NCTestUtils.assert2DValueInt(2, 2, 959803200, variable);
            NCTestUtils.assert2DValueInt(3, 2, 959806800, variable);

            variable = NCTestUtils.getVariable("nwp_mu_u10", mmd);
            NCTestUtils.assertAttribute(variable, "units", "m s**-1");
            NCTestUtils.assert2DValueFloat(4, 3, 1.5901726484298706f, variable);
            NCTestUtils.assert2DValueFloat(5, 3, 1.4782710075378418f, variable);
            NCTestUtils.assert2DValueFloat(6, 3, 1.315316915512085f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_sst", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Sea surface temperature");
            NCTestUtils.assert2DValueFloat(7, 4, 275.3016662597656f, variable);
            NCTestUtils.assert2DValueFloat(8, 4, 275.30181884765625f, variable);
            NCTestUtils.assert2DValueFloat(9, 4, 275.30181884765625f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_mslhf", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert2DValueFloat(10, 5, -28.066068649291992f, variable);
            NCTestUtils.assert2DValueFloat(11, 5, -25.100168228149414f, variable);
            NCTestUtils.assert2DValueFloat(12, 5, -23.159440994262695f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_msshf", mmd);
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.969209968386869E36");
            NCTestUtils.assert2DValueFloat(13, 6, 12.113265037536621f, variable);
            NCTestUtils.assert2DValueFloat(14, 6, 13.183022499084473f, variable);
            NCTestUtils.assert2DValueFloat(15, 6, 13.555000305175781f, variable);
        }
    }

    @Test
    public void testAddEra5Variables_coo1() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_coo1();

        writeConfiguration_coo1();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "coo_1_slstr-s3a-nt_avhrr-frac-ma_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("avhrr-frac-ma_delta_azimuth", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 11.972550392150879, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 11.975187301635742, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 1, mmd);

            // satellite fields
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_x", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_y", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_z", 137, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "units", "~");
            NCTestUtils.assert3DVariable(variable.getFullName(), 0, 0, 0, 11.525834083557129, mmd);

            variable = NCTestUtils.getVariable("nwp_o3", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 0, 0, 1.9407424645123683E-7, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 10, 0, 3.718567541000084E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 20, 0, 9.952551408787258E-6, mmd);

            variable = NCTestUtils.getVariable("nwp_u10", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert3DValueDouble(0, 0, 0, -0.9531255960464478, variable);

            variable = NCTestUtils.getVariable("nwp_skt", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Skin temperature");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 301.2060852050781, variable);

            variable = NCTestUtils.getVariable("slstr.s3a.blowVert", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "10 metre V wind component");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 3.41879940032959, variable);
        }
    }

    private void writeConfiguration_mmd15() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era-5/v1");
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
                "            </nwp-aux-dir>\n" +
                "            <satellite-fields>" +
                "                <x_dim name='left' length='5' />" +
                "                <y_dim name='right' length='7' />" +
                "                <z_dim name='up' length='23' />" +
                "                <era5_time_variable>era5-time</era5_time_variable>" +
                "                <time_variable>amsre.acquisition_time</time_variable>" +
                "                <longitude_variable>amsre.longitude</longitude_variable>" +
                "                <latitude_variable>amsre.latitude</latitude_variable>" +
                "            </satellite-fields>" +
                "            <matchup-fields>" +
                "                <time_steps_past>41</time_steps_past>" +
                "                <time_steps_future>12</time_steps_future>" +
                "                <time_dim_name>the_time</time_dim_name>" +
                "                <era5_time_variable>era5-mu-time</era5_time_variable>" +
                "                <time_variable>drifter-sst.insitu.time</time_variable>" +
                "                <longitude_variable>drifter-sst.insitu.lon</longitude_variable>" +
                "                <latitude_variable>drifter-sst.insitu.lat</latitude_variable>" +
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

    private void writeConfiguration_coo1() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era-5/v1");
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
                "            </nwp-aux-dir>\n" +
                "            <satellite-fields>" +
                "                <x_dim name='slstr.s3a.nt_nwp_x' length='1' />" +
                "                <y_dim name='slstr.s3a.nt_nwp_y' length='1' />" +
                "                <z_dim name='slstr.s3a.nt_nwp_z' />" +
                "                <era5_time_variable>slstr.s3ant_nwp_time</era5_time_variable>" +
                "                <time_variable>slstr-s3a-nt_acquisition_time</time_variable>" +
                "                <longitude_variable>slstr-s3a-nt_longitude_tx</longitude_variable>" +
                "                <latitude_variable>slstr-s3a-nt_latitude_tx</latitude_variable>" +
                "" +
                "                <an_sfc_v10>slstr.s3a.blowVert</an_sfc_v10>" +
                "            </satellite-fields>" +
                "        </era5>\n" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private File getInputDirectory_mmd15() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing/mmd15sst");
    }

    private File getInputDirectory_coo1() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing/mmd_coo1");
    }
}
