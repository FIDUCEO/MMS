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

package com.bc.fiduceo.post.plugin.nwp;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class FileMerger_IO_Test {

    private File testDataDirectory;
    private NetcdfFile netcdfFile;
    private NetcdfFileWriter netcdfFileWriter;
    private Configuration configuration;
    private TemplateVariables templateVariables;

    @Before
    public void setUp() throws IOException, InvalidRangeException {
        TestUtil.createTestDirectory();
        testDataDirectory = TestUtil.getTestDataDirectory();

        final String mmd06Path = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "post-processing", "mmd06c", "animal-sst_amsre-aq", "mmd6c_sst_animal-sst_amsre-aq_2004-008_2004-014.nc"}, true);
        final File mmd06OriginalFile = new File(mmd06Path);
        assertTrue(mmd06OriginalFile.isFile());
        final File mmd06File = TestUtil.copyFileTOTestDir(mmd06OriginalFile);

        netcdfFile = NetCDFUtils.openReadOnly(mmd06OriginalFile.getAbsolutePath());
        netcdfFileWriter = NetcdfFileWriter.openExisting(mmd06File.getAbsolutePath());
        netcdfFileWriter.setRedefineMode(true);

        configuration = new Configuration();
        configuration.setTimeVariableName("animal-sst_acquisition_time");

        templateVariables = new TemplateVariables(configuration);


        final NwpPostProcessing postProcessing = new NwpPostProcessing(configuration);
        postProcessing.prepare(netcdfFile, netcdfFileWriter);

        netcdfFileWriter.setRedefineMode(false);
    }

    @After
    public void tearDown() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
        }
        if (netcdfFileWriter != null) {
            netcdfFileWriter.close();
        }

        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testMergeAnalysisFile_MMD6() throws IOException, InvalidRangeException {
        final String analysisPath = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "post-processing", "nwp_preprocessed", "analysis1007035016016011699.nc"}, true);
        final File analysisFile = new File(analysisPath);
        assertTrue(analysisFile.isFile());

        final FileMerger fileMerger = new FileMerger(configuration, templateVariables);

        try (NetcdfFile analysis = NetcdfFile.open(analysisFile.getAbsolutePath())) {
            final int[] centerTimes = fileMerger.mergeAnalysisFile(netcdfFileWriter, analysis);
            assertEquals(9, centerTimes.length);
            assertEquals(1073433600, centerTimes[0]);
            assertEquals(1073584800, centerTimes[7]);

            netcdfFileWriter.flush();

            final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_ice_fraction"), 0, 0, 0.0, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_surface_temperature"), 1, 1, 293.1509094238281, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_east_wind_component"), 2, 2, 3.2551252841949463, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_north_wind_component"), 3, 3, 4.974652290344238, netcdfFile);
        }
    }

    @Test
    public void testMergeForecastFile_MMD6() throws IOException, InvalidRangeException {
        final String forecastPath = TestUtil.assembleFileSystemPath(new String[]{testDataDirectory.getAbsolutePath(), "post-processing", "nwp_preprocessed", "forecast6637079242589816197.nc"}, true);
        final File forecastFile = new File(forecastPath);
        assertTrue(forecastFile.isFile());

        final FileMerger fileMerger = new FileMerger(configuration, templateVariables);

        try (NetcdfFile analysis = NetcdfFile.open(forecastFile.getAbsolutePath())) {
            final int[] centerTimes = fileMerger.mergeForecastFile(netcdfFileWriter, analysis);
            assertEquals(9, centerTimes.length);
            assertEquals(1073444400, centerTimes[0]);
            assertEquals(1073520000, centerTimes[7]);

            netcdfFileWriter.flush();

            final NetcdfFile netcdfFile = netcdfFileWriter.getNetcdfFile();
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.sea_surface_temperature"), 0, 0, 291.5697937011719, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_sensible_heat_flux"), 1, 1, 240214.265625, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_latent_heat_flux"), 2, 2, -662554.375, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.mean_sea_level_pressure"), 3, 3, 100859.28125, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.boundary_layer_height"), 4, 4, 395.1171875, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_east_wind_component"), 5, 5, -4.530746936798096, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_north_wind_component"), 6, 6, 5.478193283081055, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_temperature"), 7, 7, 291.12896728515625, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_dew_point"), 8, 8, 287.94354248046875, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_solar_radiation"), 9, 0, 8987282.0, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_thermal_radiation"), 10, 1, 1.3298496E7, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_solar_radiation"), 11, 2, 1.8746058E7, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_thermal_radiation"), 12, 3, -775141.375, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_east_component"), 13, 4, -1091.51611328125, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_north_component"), 14, 5, -5850.00830078125, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.evaporation"), 15, 6, -4.936744808219373E-4, netcdfFile);
            NCTestUtils.assert2DVariable(NetcdfFile.makeValidCDLName("matchup.nwp.fc.total_precipitation"), 16, 7, 0.0037760320119559765, netcdfFile);
        }
    }
}
