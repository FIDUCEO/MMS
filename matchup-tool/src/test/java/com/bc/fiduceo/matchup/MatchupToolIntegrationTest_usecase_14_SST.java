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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_usecase_14_SST extends AbstractUsecaseIntegrationTest {

    @Before
    public void setUp() throws SQLException {
        // we overwrite the base class set-up to write our own mmd-writer configuration for this test.
        // the remainder in set-up is duplicated from the base class
        // @todo 3 tb/** find a more clever way to handle this 2016-10-05
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);

        storage = Storage.create(TestUtil.getdatasourceMongoDb(), geometryFactory);
        storage.clear();
        storage.initialize();
    }

    @Test
    public void testMatchup_overlappingSensingTimes() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);
        TestUtil.writeMmdWriterConfig(configDir);


        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                    .withTimeDeltaSeconds(2100, null)
                    .withMaxPixelDistanceKm(1.41f, null)
                    .withPixelValueScreening(null, "(cloud_flags_nadir & 1 == 0) && (cloud_flags_fward & 1 == 0)")   // select AATSR water pixel tb 2016-09-08
                    .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-14_sst.xml");

        insert_AATSR();
        insert_AMSRE();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("aatsr-en_acquisition_time", 0, 0, 0, 1108623419, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_altitude", 1, 0, 1, 239.0496826171875, mmd);
            NCTestUtils.assert3DVariable("aatsr-en_btemp_fward_0370", 2, 0, 2, 23927, mmd);

            NCTestUtils.assert3DVariable("amsre-aq_10_7H_Res_1_TB", 1, 0, 55, -13132, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_10_7V_Res_1_TB", 2, 0, 56, -9683, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_18_7H_Res_1_TB", 3, 0, 57, -11825, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_Sun_Glint_Angle", 4, 0, 58, 4287, mmd);
        }
    }

    @Test
    public void testMatchup_overlappingSensingTimes_withVariablesRenamed() throws IOException, ParseException, SQLException, InvalidRangeException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);
        final String writerConfigXml = "<mmd-writer-config>" +
                                       "    <overwrite>false</overwrite>" +
                                       "    <cache-size>2048</cache-size>" +
                                       "    <netcdf-format>N3</netcdf-format>" +
                                       "    <variables-configuration>" +
                                       "        <sensor-rename source-name = \"aatsr-en\" target-name = \"atsr.3\" />" +
                                       "        <separator sensor-names = \"aatsr-en\" separator = \".\" />" +
                                       "        <sensors names = \"aatsr-en\">" +
                                       "            <rename source-name = \"acquisition_time\" target-name = \"time\" />" +
                                       "            <rename source-name = \"altitude\" target-name = \"altitude\" />" +
                                       "            <rename source-name = \"btemp_fward_0370\" target-name = \"brightness_temperature_37_forward\" />" +
                                       "            <rename source-name = \"btemp_nadir_0370\" target-name = \"brightness_temperature_37_nadir\" />" +
                                       "            <rename source-name = \"btemp_fward_1100\" target-name = \"brightness_temperature_11_forward\" />" +
                                       "            <rename source-name = \"btemp_nadir_1100\" target-name = \"brightness_temperature_11_nadir\" />" +
                                       "            <rename source-name = \"btemp_fward_1200\" target-name = \"brightness_temperature_12_forward\" />" +
                                       "            <rename source-name = \"btemp_nadir_1200\" target-name = \"brightness_temperature_12_nadir\" />" +
                                       "            <rename source-name = \"reflec_fward_0550\" target-name = \"reflectance_55_forward\" />" +
                                       "            <rename source-name = \"reflec_nadir_0550\" target-name = \"reflectance_55_nadir\" />" +
                                       "            <rename source-name = \"reflec_fward_0670\" target-name = \"reflectance_66_forward\" />" +
                                       "            <rename source-name = \"reflec_nadir_0670\" target-name = \"reflectance_66_nadir\" />" +
                                       "            <rename source-name = \"reflec_fward_0870\" target-name = \"reflectance_87_forward\" />" +
                                       "            <rename source-name = \"reflec_nadir_0870\" target-name = \"reflectance_87_nadir\" />" +
                                       "            <rename source-name = \"reflec_fward_1600\" target-name = \"reflectance_16_forward\" />" +
                                       "            <rename source-name = \"reflec_nadir_1600\" target-name = \"reflectance_16_nadir\" />" +
                                       "            <rename source-name = \"confid_flags_nadir\" target-name = \"confidence_word_nadir\" />" +
                                       "            <rename source-name = \"confid_flags_fward\" target-name = \"confidence_word_forward\" />" +
                                       "            <rename source-name = \"cloud_flags_nadir\" target-name = \"cloud_flags_nadir\" />" +
                                       "            <rename source-name = \"cloud_flags_fward\" target-name = \"cloud_flags_forward\" />" +
                                       "        </sensors>" +
                                       "    </variables-configuration>" +
                                       "</mmd-writer-config>";
        TestUtil.writeMmdWriterConfig(configDir, writerConfigXml);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                    .withTimeDeltaSeconds(2100, null)
                    .withMaxPixelDistanceKm(1.41f, null)
                    .withPixelValueScreening(null, "(cloud_flags_nadir & 1 == 0) && (cloud_flags_fward & 1 == 0)")   // select AATSR water pixel tb 2016-09-08
                    .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-14_sst.xml");

        insert_AATSR();
        insert_AMSRE();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2005-048", "-end", "2005-048"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2005-048", "2005-048");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            NCTestUtils.assert3DVariable("atsr.3.time", 0, 0, 0, 1108623419, mmd);
            NCTestUtils.assert3DVariable("atsr.3.altitude", 1, 0, 1, 239.0496826171875, mmd);

            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_37_forward", 2, 0, 2, 23927, mmd);
            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_37_nadir", 3, 0, 3, 24187, mmd);
            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_11_forward", 4, 0, 4, 24240, mmd);
            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_11_nadir", 5, 0, 5, 23729, mmd);
            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_12_forward", 6, 0, 6, 23945, mmd);
            NCTestUtils.assert3DVariable("atsr.3.brightness_temperature_12_nadir", 7, 0, 7, 24068, mmd);

            NCTestUtils.assert3DVariable("atsr.3.reflectance_55_forward", 8, 0, 8, 214, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_55_nadir", 9, 0, 9, 131, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_66_forward", 10, 0, 10, 238, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_66_nadir", 0, 1, 11, 183, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_87_forward", 1, 1, 12, 469, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_87_nadir", 2, 1, 13, 440, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_16_forward", 3, 1, 14, 107, mmd);
            NCTestUtils.assert3DVariable("atsr.3.reflectance_16_nadir", 4, 1, 15, 73, mmd);

            NCTestUtils.assert3DVariable("atsr.3.confidence_word_nadir", 5, 1, 16, 0, mmd);
            NCTestUtils.assert3DVariable("atsr.3.confidence_word_forward", 6, 1, 17, 2, mmd);
            NCTestUtils.assert3DVariable("atsr.3.cloud_flags_nadir", 7, 1, 18, 2402, mmd);
            NCTestUtils.assert3DVariable("atsr.3.cloud_flags_forward", 8, 1, 19, 354, mmd);

            NCTestUtils.assert3DVariable("amsre-aq_10_7H_Res_1_TB", 1, 0, 55, -13132, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_10_7V_Res_1_TB", 2, 0, 56, -9683, mmd);
            NCTestUtils.assert3DVariable("amsre-aq_18_7H_Res_1_TB", 3, 0, 57, -11825, mmd);
        }
    }

    private MatchupToolUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("amsre-aq");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("aatsr-en"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("amsre-aq", 21, 21));
        dimensions.add(new Dimension("aatsr-en", 11, 11));

        return (MatchupToolUseCaseConfigBuilder) new MatchupToolUseCaseConfigBuilder("mmd14_sst")
                    .withSensors(sensorList)
                    .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-14_sst").getPath())
                    .withDimensions(dimensions);
    }

    private void insert_AATSR() throws IOException, SQLException {
        final String sensorKey = "aatsr-en";
        final String version = "v2.1";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "17", "ATS_TOA_1PUUPA20050217_053700_000065272034_00434_15518_9023.N1"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_AMSRE() throws IOException, SQLException {
        final String sensorKey = "amsre-aq";
        final String version = "v12";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2005", "02", "17", "AMSR_E_L2A_BrightnessTemperatures_V12_200502170536_D.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }
}
