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

        final File targetFile = new File(testDirectory, "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertDimension("matchup.nwp.an.time", 19, mmd);
            NCTestUtils.assertDimension("matchup.nwp.fc.time", 33, mmd);

            NCTestUtils.assertScalarVariable("matchup.nwp.an.t0", 0, 1073433600, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.sea_ice_fraction", 0, 1, 0.0, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.sea_surface_temperature", 1, 2, 293.451416015625, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.10m_east_wind_component", 2, 3, 6.577442169189453, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.10m_north_wind_component", 3, 4, -3.9324848651885986, mmd);

            NCTestUtils.assertScalarVariable("matchup.nwp.fc.t0", 5, 1073498400, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.sea_surface_temperature", 4, 6, 293.3771057128906, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.surface_sensible_heat_flux", 5, 7, -279947.5, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.surface_latent_heat_flux", 6, 8, -2967699.75, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.mean_sea_level_pressure", 7, 0, 102026.4609375, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.boundary_layer_height", 8, 1, 348.9081115722656, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.10m_east_wind_component", 9, 2, 0.18119236826896667, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.10m_north_wind_component", 10, 3, 4.9406914710998535, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.2m_temperature", 11, 4, 292.26434326171875, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.2m_dew_point", 12, 5, 291.4545593261719, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.downward_surface_solar_radiation", 13, 6, 9262679.0, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.downward_surface_thermal_radiation", 14, 7, 1.2656222E7, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.surface_solar_radiation", 15, 8, 1.0799999783372982E-11, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.surface_thermal_radiation", 16, 0, -2792648.75, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.turbulent_stress_east_component", 17, 1, -973.2415161132812, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.turbulent_stress_north_component", 18, 2, -338.94049072265625, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.evaporation", 19, 3, -4.630652256309986E-4, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.total_precipitation", 20, 4, 8.657717262394726E-5, mmd);
        }
    }

    private void writeConfiguration() throws IOException {
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
                "            <cdo-home>/home/tom/Dev/cdo_installation/bin</cdo-home>\n" + // @todo move to test-confug tb 2017-01-11
                "            <nwp-aux-dir>" + eraInterimDir.getAbsolutePath() + "</nwp-aux-dir>\n" +
                "            <delete-on-exit>true</delete-on-exit>\n" +
                "            <analysis-steps>19</analysis-steps>\n" +
                "            <forecast-steps>33</forecast-steps>\n" +
                "            <time-variable-name>animal-sst_acquisition_time</time-variable-name>\n" +
                "            <longitude-variable-name>animal-sst_insitu.lon</longitude-variable-name>\n" +
                "            <latitude-variable-name>animal-sst_insitu.lon</latitude-variable-name>\n" +
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
