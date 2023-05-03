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

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SMOSL1_tao_sss extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_SMOS_TAO() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(7220, null)
                .withMaxPixelDistanceKm(10, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-45.xml");

        insert_TAO_SSS();
        insert_miras_CDF3TA_June();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-156", "-end", "2016-156"};
        MatchupToolMain.main(args);


        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-156", "2016-156");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(4, matchupCount);

            NCTestUtils.assert3DVariable("tao-sss_AIRT", 0, 0, 0, 25.8f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_BARO", 0, 0, 1, -9.9f, mmd);
            NCTestUtils.assertStringVariable("tao-sss_M", 8, 2, "DDDDDDDD", mmd);
            NCTestUtils.assert3DVariable("tao-sss_Q", 0, 0, 3, 11111199, mmd);
            NCTestUtils.assert3DVariable("tao-sss_RAIN", 0, 0, 0, -9.99f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_RH", 0, 0, 1, 87.33f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SSS", 0, 0, 2, 35.49f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SST", 0, 0, 3, 26.438f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_WDIR", 0, 0, 0, 257.f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_WSPD", 0, 0, 1, 4.7f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_acquisition_time", 0, 0, 2, 1465056000, mmd);
            NCTestUtils.assertStringVariable("tao-sss_file_name", 128, 3, "TAO_T2S140W_DM167A-20160228_2016-06.txt", mmd);
            NCTestUtils.assert3DVariable("tao-sss_latitude", 0, 0, 0, -2.04f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_longitude", 0, 0, 1, -139.99f, mmd);
            NCTestUtils.assertStringVariable("tao-sss_processing_version", 30, 2, "v1", mmd);
            NCTestUtils.assert3DVariable("tao-sss_time", 0, 0, 3, 1465059600, mmd);
            NCTestUtils.assertVectorVariable("tao-sss_x", 0, 0, mmd);
            NCTestUtils.assertVectorVariable("tao-sss_y", 1, 87, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Azimuth_Angle_175", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_3_125", 1, 0, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_175", 2, 0, 3, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_H_425", 0, 1, 3, -19819, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Days_175", 1, 1, 0, -2147483647, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Eta_325", 2, 1, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis2_125", 0, 2, 2, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Incidence_Angle_400", 1, 2, 3, -3629, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_RFI_Flags_425", 2, 2, 0, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nviews_575", 0, 0, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_H_075", 1, 0, 2, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_V_525", 2, 0, 3, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_H_025", 0, 1, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Microseconds_400", 1, 1, 0, 289244, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Xi_400", 2, 1, 2, 15486, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_lon", 0, 2, 3, -140.18731689453125, mmd);
        }
    }

    private void insert_TAO_SSS() throws IOException, SQLException {
        final String sensorKey = "tao-sss";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", sensorKey, "v1", "2016", "06", "TAO_T2S140W_DM167A-20160228_2016-06.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TA_June() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TA";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2016", "156", "SM_RE07_MIR_CDF3TA_20160604T000000_20160604T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "re07");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("tao-sss");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("miras-smos-CDF3TA"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("tao-sss", 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("miras-smos-CDF3TA", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd45")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-45").getPath())
                .withDimensions(dimensions);
    }
}
