package com.bc.fiduceo.matchup;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
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

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_MxD35_SLSTR extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup() throws IOException, SQLException, ParseException, InvalidRangeException {
        final File mmdWriterConfig = new File(configDir, "mmd-writer-config.xml");
        if (!mmdWriterConfig.delete()) {
            fail("unable to delete test file");
        }
        TestUtil.writeMmdWriterConfig(configDir, MOD35_SCALED_VARIABLES);

        insert_MOD35();
        insert_SLSTR();

        final MatchupToolTestUseCaseConfigBuilder useCaseConfigBuilder = createUseCaseConfigBuilder();
        final UseCaseConfig useCaseConfig = useCaseConfigBuilder.withTimeDeltaSeconds(2000400, null)
                .withMaxPixelDistanceKm(0.2f, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase_mod35_slstr.xml");

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2022-110", "-end", "2022-120"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2022-110", "2022-120");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(4833, matchupCount);

            NCTestUtils.assert3DVariable("mod35-te_Latitude", 2, 2, 4781, 43.30309f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Longitude", 2, 2, 4781, -8.199804f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Scan_Start_Time", 2, 2, 4781, 9.25039660322495E8, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Solar_Zenith", 2, 2, 4781, 32.967995f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Solar_Azimuth", 2, 2, 4781, 150.8268f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Sensor_Zenith", 2, 2, 4781, 5.9159994f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Sensor_Azimuth", 2, 2, 4781, -76.51706f, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_SPI_0", 2, 2, 4781, 15.25999965891242, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_SPI_1", 2, 2, 4781, 19.509999563917518, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_0", 2, 2, 4781, -3, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_1", 2, 2, 4781, -49, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_2", 2, 2, 4781, 29, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_3", 2, 2, 4781, 16, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_4", 2, 2, 4781, -1, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Cloud_Mask_5", 2, 2, 4781, -1, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_0", 2, 2, 4781, 13, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_1", 2, 2, 4781, -33, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_2", 2, 2, 4781, 29, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_3", 2, 2, 4781, 16, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_4", 2, 2, 4781, -1, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_5", 2, 2, 4781, -1, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_6", 2, 2, 4781, 11, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_7", 2, 2, 4781, 0, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_8", 2, 2, 4781, 0, mmd);
            NCTestUtils.assert3DVariable("mod35-te_Quality_Assurance_9", 2, 2, 4781, 0, mmd);
        }
    }

    private void insert_MOD35() throws IOException, SQLException {
        final String sensorKey = "mod35-te";
        final String version = "v61";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2022", "115", "MOD35_L2.A2022115.1125.061.2022115193707.hdf"}, true);
        final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
        storage.insert(satelliteObservation);
    }

    private void insert_SLSTR() throws IOException, SQLException {
        final String sensorKey = "slstr-s3a";
        final String version = "1.0";
        final String[] products = {
                "S3B_SL_1_RBT____20220425T222331_20220425T222631_20220426T125142_0179_065_158_0540_PS2_O_NT_004.zip",
//                "S3A_SL_1_RBT____20220425T212458_20220425T212758_20220427T071339_0180_084_300_0720_PS1_O_NT_004.zip",
//                "S3A_SL_1_RBT____20220425T212158_20220425T212458_20220427T071339_0179_084_300_0540_PS1_O_NT_004.zip"
        };
        for (String product : products) {
            final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, version, "2022", "04", "25", product}, true);
            final String absolutePath = TestUtil.getTestDataDirectory().getAbsolutePath() + relativeArchivePath;

            final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, absolutePath, version);
            storage.insert(satelliteObservation);
        }
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("mod35-te");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("slstr-s3a"));

        final List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(new Dimension("mod35-te", 5, 5));
        dimensions.add(new Dimension("slstr-s3a", 9, 9));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("usecase_mod35_slstr")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase_mod35_slstr").getPath())
                .withDimensions(dimensions);
    }

    private static final String MOD35_SCALED_VARIABLES = "<variables-configuration> \n" +
                                                         "    <sensors names=\"mod35-te, myd35-aq\" >\n" +
                                                         "        <writeScaled source-name=\"Cloud_Mask_SPI_0\" />" +
                                                         "        <writeScaled source-name=\"Cloud_Mask_SPI_1\" />" +
                                                         "    </sensors>\n" +
                                                         "</variables-configuration>";
}
