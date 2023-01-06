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
public class MatchupToolIntegrationTest_Windsat_sic_cci extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withTimeDeltaSeconds(28800, null)
                .withMaxPixelDistanceKm(8, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-45.xml");

        insert_SIC_CCI();
        insert_windsat();


        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2018-119", "-end", "2018-119"};
        MatchupToolMain.main(args);


        final File mmdFile = getMmdFilePath(useCaseConfig, "2018-119", "2018-119");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_AMSR2_10.7GHzH", 0, 0, 0, 88.99f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_AMSR2_23.8GHzV", 0, 0, 0, 195.95f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_AMSR2_7.3GHzH", 0, 0, 0, 83.66f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_AMSR2_Earth-Incidence", 0, 0, 0, 55.18f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_AMSR2_time", 0, 0, 0, 1525011246, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ASCAT_nb_samples", 0, 0, 0, 6, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ASCAT_time", 0, 0, 0, 1525003200, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ERA5_e", 0, 0, 0, -0.197f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ERA5_istl4", 0, 0, 0, 271.46f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ERA5_sf", 0, 0, 0, 0.f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ERA5_t2m", 0, 0, 0, 274.53f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_ERA5_tp", 0, 0, 0, 0.f, mmd);
            NCTestUtils.assert3DVariable("DMISIC0-sic-cci_SIC", 0, 0, 0, 0.f, mmd);

            NCTestUtils.assert3DVariable("windsat-coriolis_acquisition_time", 0, 0, 0, 1525026541, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_earth_azimuth_angle_187_aft", 1, 0, 0, 183.69f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_earth_azimuth_angle_370_fore", 2, 0, 0, 288.6f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_earth_incidence_angle_187_aft", 0, 1, 0, 54.984f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_earth_incidence_angle_370_fore", 1, 1, 0, 53.432f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_fra_107_fore", 2, 1, 0, -9999.f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_fra_370_aft", 0, 2, 0, 359.97f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_latitude", 1, 2, 0, 63.6875f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_pra_068_fore", 2, 2, 0, -9999.f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_pra_238_aft", 0, 0, 0, -9999.f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_quality_flag_068_aft", 1, 0, 0, 32, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_quality_flag_187_fore", 2, 0, 0, 32, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_scan_angle_068_aft", 0, 1, 0, 144.f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_scan_angle_187_fore", 1, 1, 0, 36.10000228881836f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_06_H_aft", 2, 1, 0, 3713, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_10_H_fore", 0, 2, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_10_P_aft", 1, 2, 0, 7762, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_10_V_fore", 2, 2, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_18_M_aft", 0, 0, 0, 9130, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_18_R_fore", 1, 0, 0, 9261, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_23_V_aft", 2, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_37_L_fore", 0, 1, 0, 12114, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_37_R_aft", 1, 1, 0, 12072, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_tb_37_R_aft", 1, 1, 0, 12072, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_time_068_fore", 2, 1, 0, -9999.f, mmd);
            NCTestUtils.assert3DVariable("windsat-coriolis_time_238_aft", 0, 2, 0, -9999.f, mmd);
        }
    }

    private void insert_SIC_CCI() throws IOException, SQLException {
        final String sensorKey = "DMISIC0-sic-cci";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", sensorKey, "v3", "ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-2018-N.text"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v3");
        storage.insert(satelliteObservation);
    }

    private void insert_windsat() throws IOException, SQLException {
        final String sensorKey = "windsat-coriolis";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("DMISIC0-sic-cci");
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("windsat-coriolis"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("DMISIC0-sic-cci", 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("windsat-coriolis", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd45")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-45").getPath())
                .withDimensions(dimensions);
    }
}
