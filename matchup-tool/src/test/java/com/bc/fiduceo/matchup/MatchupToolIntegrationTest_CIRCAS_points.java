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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_CIRCAS_points extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_CIRCAS_location_extracts() throws IOException, ParseException, SQLException, InvalidRangeException {
        final File mmdWriterConfig = new File(configDir, "mmd-writer-config.xml");
        if (!mmdWriterConfig.delete()) {
            fail("unable to delete test file");
        }
        TestUtil.writeMmdWriterConfig(configDir, MODIS_EXCLUDES_TAG);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withLocationElement(-58.230045, 36.67764)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-circas.xml");

        insert_MOD06_Terra();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2013-036", "-end", "2013-038"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2013-036", "2013-038");
        assertTrue(mmdFile.isFile());


        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("mod06-te_Cloud_Fraction", 0, 0, 0, 28, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Cloud_Mask_5km", 0, 0, 0, 16277, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Cloud_Optical_Thickness", 0, 0, 0, -9999, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Cloud_Optical_Thickness_Uncertainty", 0, 0, 0, -9999, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Cloud_Phase_Infrared", 0, 0, 0, 1, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Latitude", 0, 0, 0, 36.67763900756836, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Longitude", 0, 0, 0, -58.230045318603516, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Quality_Assurance_5km_03", 0, 0, 0, 7, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Quality_Assurance_5km_04", 0, 0, 0, 18, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Quality_Assurance_5km_05", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("mod06-te_Quality_Assurance_5km_09", 0, 0, 0, -53, mmd);
        }
    }

    private void insert_MOD06_Terra() throws IOException, SQLException {
        final String sensorKey = "mod06-te";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v006", "2013", "037", "MOD06_L2.A2013037.1435.006.2015066015540.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, "v006");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("mod06-te");
        primary.setPrimary(true);
        sensorList.add(primary);

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("mod06-te", 1, 1));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("circas")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "circas").getPath())
                .withDimensions(dimensions);
    }

    private static final String MODIS_EXCLUDES_TAG ="<variables-configuration> \n" +
            "            <sensors names=\"mod06-te, myd06-aq\">\n" +
            "            <exclude source-name=\"Solar_Zenith\"/>\n" +
            "            <exclude source-name=\"Solar_Zenith_Day\"/>\n" +
            "            <exclude source-name=\"Solar_Zenith_Night\"/>\n" +
            "            <exclude source-name=\"Solar_Azimuth\"/>\n" +
            "            <exclude source-name=\"Solar_Azimuth_Day\"/>\n" +
            "            <exclude source-name=\"Solar_Azimuth_Night\"/>\n" +
            "            <exclude source-name=\"Sensor_Zenith\"/>\n" +
            "            <exclude source-name=\"Sensor_Zenith_Day\"/>\n" +
            "            <exclude source-name=\"Sensor_Zenith_Night\"/>\n" +
            "            <exclude source-name=\"Sensor_Azimuth\"/>\n" +
            "            <exclude source-name=\"Sensor_Azimuth_Day\"/>\n" +
            "            <exclude source-name=\"Sensor_Azimuth_Night\"/>\n" +
            "            <exclude source-name=\"Surface_Temperature\"/>\n" +
            "            <exclude source-name=\"Surface_Pressure\"/>\n" +
            "            <exclude source-name=\"Cloud_Height_Method\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Height\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Height_Nadir\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Height_Nadir_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Height_Nadir_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Nadir\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Nadir_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Nadir_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature_Nadir\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature_Nadir_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Temperature_Nadir_Day\"/>\n" +
            "            <exclude source-name=\"Tropopause_Height\"/>\n" +
            "            <exclude source-name=\"Cloud_Fraction_Nadir\"/>\n" +
            "            <exclude source-name=\"Cloud_Fraction_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Fraction_Nadir_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Fraction_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Fraction_Nadir_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity_Nadir\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity_Nadir_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Emissivity_Nadir_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Top_Pressure_Infrared\"/>\n" +
            "            <exclude source-name=\"Radiance_Variance\"/>\n" +
            "            <exclude source-name=\"Cloud_Phase_Infrared_Night\"/>\n" +
            "            <exclude source-name=\"Cloud_Phase_Infrared_Day\"/>\n" +
            "            <exclude source-name=\"Cloud_Phase_Infrared_1km\"/>\n" +
            "            <exclude source-name=\"IRP_CTH_Consistency_Flag_1km\"/>\n" +
            "            <exclude source-name=\"os_top_flag_1km\"/>\n" +
            "            <exclude source-name=\"cloud_top_pressure_1km\"/>\n" +
            "            <exclude source-name=\"cloud_top_height_1km\"/>\n" +
            "            <exclude source-name=\"cloud_top_temperature_1km\"/>\n" +
            "            <exclude source-name=\"cloud_emissivity_1km\"/>\n" +
            "            <exclude source-name=\"cloud_top_method_1km\"/>\n" +
            "            <exclude source-name=\"surface_temperature_1km\"/>\n" +
            "            <exclude source-name=\"cloud_emiss11_1km\"/>\n" +
            "            <exclude source-name=\"cloud_emiss12_1km\"/>\n" +
            "            <exclude source-name=\"cloud_emiss13_1km\"/>\n" +
            "            <exclude source-name=\"cloud_emiss85_1km\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_16_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_37\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_37_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_16_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_37\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_37_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_1621_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_1621_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_1621_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_16_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_37\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_37_PCL\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_Uncertainty\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_Uncertainty_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_Uncertainty_37\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_Uncertainty_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_Uncertainty_37\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_Uncertainty\"/>\n" +
            "            <exclude source-name=\"Cloud_Effective_Radius_Uncertainty_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Optical_Thickness_Uncertainty_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_Uncertainty_1621\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_Uncertainty_16\"/>\n" +
            "            <exclude source-name=\"Cloud_Water_Path_Uncertainty_37\"/>\n" +
            "            <exclude source-name=\"Above_Cloud_Water_Vapor_094\"/>\n" +
            "            <exclude source-name=\"IRW_Low_Cloud_Temperature_From_COP\"/>\n" +
            "            <exclude source-name=\"Cloud_Phase_Optical_Properties\"/>\n" +
            "            <exclude source-name=\"Cloud_Multi_Layer_Flag\"/>\n" +
            "            <exclude source-name=\"Cirrus_Reflectance\"/>\n" +
            "            <exclude source-name=\"Cirrus_Reflectance_Flag\"/>\n" +
            "        </sensors>\n" +
            "    </variables-configuration>";
}
