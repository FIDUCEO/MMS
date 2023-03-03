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
public class MatchupToolIntegrationTest_SMOSL1_ndbc_sm extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_ndbc_sm() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("ndbc-sm-cs")
                .withTimeDeltaSeconds(3600, null)
                .withMaxPixelDistanceKm(14, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-44.xml");

        insert_ndbc_sm_cs();
        insert_miras_CDF3TA_June();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-156", "-end", "2016-156"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-156", "2016-156");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {

            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(19, matchupCount);

            NCTestUtils.assert3DVariable("ndbc-sm-cs_APD", 0, 0, 0, 99.f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_DEWP", 0, 0, 1, 999.f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_GST", 0, 0, 2, 7.6f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_PRES", 0, 0, 3, 1009.4f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_VIS", 0, 0, 4, 99.f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_WSPD", 0, 0, 5, 5.5f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_acquisition_time", 0, 0, 6, 1465042320L, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_anemometer_height", 0, 0, 7, 10.f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_latitude", 0, 0, 8, 27.297f, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_measurement_type", 0, 0, 9, 1, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_sst_depth", 0, 0, 10, Float.NaN, mmd);
            NCTestUtils.assert3DVariable("ndbc-sm-cs_station_type", 0, 0, 11, 4, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Azimuth_Angle_175", 0, 0, 12, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_3_125", 1, 0, 13, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_175", 2, 0, 14, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_H_425", 0, 1, 15, 2595, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Days_175", 1, 1, 16, -2147483647, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Eta_325", 2, 1, 17, -7254, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis2_125", 0, 2, 18, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Incidence_Angle_400", 1, 2, 0, -3429, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_RFI_Flags_425", 2, 2, 1, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nviews_575", 0, 0, 2, 19, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_H_075", 1, 0, 3, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_V_525", 2, 0, 4, -28050, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_H_025", 0, 1, 5, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Microseconds_400", 1, 1, 6, 976345, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Xi_400", 2, 1, 7, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_lat", 0, 2, 8, 27.611055374145508, mmd);
        }
    }

    private void insert_ndbc_sm_cs() throws IOException, SQLException {
        final String sensorKey = "ndbc-sm-cs";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", sensorKey, "v1", "2016", "babt2h2016.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TA_June() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TA";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2016", "156", "SM_RE07_MIR_CDF3TA_20160604T000000_20160604T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "re07");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String sicSensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(sicSensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("miras-smos-CDF3TA"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(sicSensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("miras-smos-CDF3TA", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd44")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-44").getPath())
                .withDimensions(dimensions);
    }
}
