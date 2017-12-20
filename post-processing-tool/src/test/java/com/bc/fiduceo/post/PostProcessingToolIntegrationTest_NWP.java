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
    public void testAddNWPVariables_timeSeries() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration_timeSeries();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2004-008", "-end", "2004-012",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertDimension("matchup.nwp.an.time", 19, mmd);
            NCTestUtils.assertDimension("matchup.nwp.fc.time", 33, mmd);

            NCTestUtils.assertVectorVariable("matchup.nwp.an.t0", 0, 1073692800, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.sea_ice_fraction", 0, 1, 0.0, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.sea_surface_temperature", 1, 2, 293.451416015625, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.10m_east_wind_component", 2, 3, 6.577442169189453, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.10m_north_wind_component", 3, 4, -3.9324848651885986, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.total_column_water_vapour", 4, 5, 40.191993713378906, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.an.cloud_liquid_water_content", 5, 6, 0.0, mmd);

            NCTestUtils.assertVectorVariable("matchup.nwp.fc.t0", 5, 1073876400, mmd);
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
            NCTestUtils.assert2DVariable("matchup.nwp.fc.total_column_water_vapour", 21, 5, 28.09221076965332, mmd);
            NCTestUtils.assert2DVariable("matchup.nwp.fc.cloud_liquid_water_content", 22, 6, 0.0, mmd);
        }
    }

    @Test
    public void testAddNWPVariables_sensorExtract() throws ParseException, IOException, InvalidRangeException {
        final File inputDir = getInputDirectory();

        writeConfiguration_sensorExtract();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2004-008", "-end", "2004-012",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("amsre.nwp.10m_east_wind_component", 1, 0, 1, 6.077203273773193, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.10m_north_wind_component", 2, 0, 2, -10.37724781036377, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.2m_dew_point", 3, 0, 3, 277.2325439453125, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.2m_temperature", 4, 0, 4, 278.2607421875, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.albedo", 0, 1, 5, 0.06999999, mmd);

            NCTestUtils.assert4DVariable("amsre.nwp.cloud_ice_water", 1, 1, 6, 1, 0.0, mmd);
            NCTestUtils.assert4DVariable("amsre.nwp.cloud_liquid_water", 2, 1, 7, 2, 0.0, mmd);

            NCTestUtils.assert3DVariable("specific_lnsp_name", 3, 1, 8, 11.510656356811523, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.mean_sea_level_pressure", 4, 1, 0, 100534.4765625, mmd);

            NCTestUtils.assert4DVariable("amsre.nwp.ozone_profile", 0, 2, 1, 3, 2.187581230828073E-6, mmd);

            NCTestUtils.assert3DVariable("amsre.nwp.sea_surface_temperature", 0, 3, 2, 276.4276428222656, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.seaice_fraction", 1, 3, 3, 0.0, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.skin_temperature", 2, 3, 4, 276.2925109863281, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.snow_albedo", 3, 3, 5, 0.8499984741210938, mmd);

            NCTestUtils.assert4DVariable("amsre.nwp.temperature_profile", 4, 3, 6, 4, 273.0578918457031, mmd);
            NCTestUtils.assert4DVariable("amsre.nwp.water_vapour_profile", 0, 4, 7, 5, 3.7358231566031463E-6, mmd);

            NCTestUtils.assert3DVariable("amsre.nwp.total_cloud_cover", 1, 4, 8, 1.0, mmd);
            NCTestUtils.assert3DVariable("amsre.nwp.total_column_water_vapour", 2, 4, 0, 19.767866134643555, mmd);
        }
    }

    private void writeConfiguration_timeSeries() throws IOException {
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
                "            <time-series-extraction>\n" +
                "                <analysis-steps>19</analysis-steps>\n" +
                "                <forecast-steps>33</forecast-steps>\n" +
                "                <time-variable-name>animal-sst_acquisition_time</time-variable-name>\n" +
                "                <longitude-variable-name>animal-sst_insitu.lon</longitude-variable-name>\n" +
                "                <latitude-variable-name>animal-sst_insitu.lon</latitude-variable-name>\n" +
                "            </time-series-extraction>\n" +
                "        </nwp>" +
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
                "                <an-lnsp-name>specific_lnsp_name</an-lnsp-name>\n" +
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
        return new File(testDataDirectory, "post-processing/mmd06c");
    }
}
