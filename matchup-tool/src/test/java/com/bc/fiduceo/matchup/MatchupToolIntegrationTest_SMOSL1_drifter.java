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
public class MatchupToolIntegrationTest_SMOSL1_drifter extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(7200, null)
                .withMaxPixelDistanceKm(2, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-43.xml");

        insert_SIRDS();
        insert_miras_CDF3TD();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2017-324", "-end", "2017-324"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2017-324", "2017-324");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(15, matchupCount);

            NCTestUtils.assert3DVariable("drifter-sirds_acquisition_time", 0, 0, 0, 1511146800, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_collection", 0, 0, 1, 1, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_depth", 0, 0, 2, 0.2, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_depth_corr", 0, 0, 3, 0.0, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_latitude", 0, 0, 4, -25.63, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_longitude", 0, 0, 5, -151.32001, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_prof_id", 0, 0, 6, 633946, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_qc1", 0, 0, 7, 0, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_qc2", 0, 0, 8, -99, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst", 0, 0, 9, 30.100000381469727, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst_comb_unc", 0, 0, 10, 0.3895, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst_plat_corr", 0, 0, 11, 0.0, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst_plat_corr", 0, 0, 12, 0.0, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst_plat_corr_unc", 0, 0, 12, 0.29, mmd);
            NCTestUtils.assert3DVariable("drifter-sirds_sst_rand_unc", 0, 0, 13, 0.26, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Azimuth_Angle_025", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_BT_3_075", 1, 0, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_BT_4_125", 2, 0, 2, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_BT_H_175", 0, 1, 3, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_BT_V_225", 1, 1, 4, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Days_275", 2, 1, 5, 6533, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Eta_325", 0, 2, 6, -10148, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Footprint_Axis1_375", 1, 2, 7, -16670, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Footprint_Axis2_400", 2, 2, 8, -19158, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Incidence_Angle_425", 0, 0, 9, -2487, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Nb_RFI_Flags_475", 1, 0, 10, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Nb_SUN_Flags_525", 2, 0, 11, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Nviews_575", 0, 1, 12, 14, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TD_Pixel_BT_Standard_Deviation_3_625", 1, 1, 13, -32768, mmd);
        }
    }

    private void insert_SIRDS() throws IOException, SQLException {
        final String sensorKey = "drifter-sirds";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sirds", "v1.0", "SSTCCI2_refdata_drifter_201711.nc"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TD() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TD";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2017", "324", "SM_RE07_MIR_CDF3TD_20171120T000000_20171120T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "re07");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("drifter-sirds");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("miras-smos-CDF3TD"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("drifter-sirds", 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("miras-smos-CDF3TD", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd43")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-43").getPath())
                .withDimensions(dimensions);
    }
}
