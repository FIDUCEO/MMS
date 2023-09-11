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
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SMAP_L2C_tao_sss extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_SMAP_L2C_sss__tao_sss() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("tao-sss")
                .withTimeDeltaSeconds(3600, null)
                .withMaxPixelDistanceKm(14, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-xxx.xml");

        insert_tao_sss();
        insert_smap_sss_for();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2018-030", "-end", "2018-040"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2018-030", "2018-040");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {

            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(2, matchupCount);

            NCTestUtils.assert3DVariable("smap-sss-for_time_for", 0, 0, 0, 5.710216511556213E8, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_cellat_for", 1, 0, 0, -0.12047244f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_cellon_for", 2, 0, 0, 249.86536f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_gland_for", 0, 1, 0, -1.4906111E-7f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_fland_for", 1, 1, 0, 8.9630685E-9f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_gice_est", 2, 1, 0, 0.0f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_surtep", 0, 2, 0, 296.19537f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_winspd", 1, 2, 0, 2.6554973f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_sss_ref", 2, 2, 0, 34.5771f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_tran", 0, 0, 1, 0.9898046f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_tbup", 1, 0, 1, 2.7570956f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_tbdw", 2, 0, 1, 2.7582273f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_windir", 0, 1, 1, 83.879845f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_rain", 1, 1, 1, 0.0f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_solar_flux", 2, 1, 1, 43.89048f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_temp_ant_for_V", 0, 2, 1, 381.10342f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_temp_ant_for_H", 1, 2, 1, 381.51367f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_zang_for", 2, 2, 1, 88.04912f, mmd);

            NCTestUtils.assert3DVariable("smap-sss-for_cellat_for", 1, 1, 0, 0.12653376f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_cellat_for", 1, 1, 1, 0.12653376f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_cellon_for", 1, 1, 0, 250.11238f, mmd);
            NCTestUtils.assert3DVariable("smap-sss-for_cellon_for", 1, 1, 1, 250.11238f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_longitude", 0, 0, 0, -109.89f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_latitude", 0, 0, 0, 0.05f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_longitude", 0, 0, 1, -109.89f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_latitude", 0, 0, 1, 0.040273894f, mmd);

            NCTestUtils.assert3DVariable("tao-sss_time", 0, 0, 0, 1517706000, mmd);
            NCTestUtils.assert3DVariable("tao-sss_time", 0, 0, 1, 1517709600, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SSS", 0, 0, 0, 34.754f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SSS", 0, 0, 1, 34.751f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SST", 0, 0, 0, 23.243f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_SST", 0, 0, 1, 23.272f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_AIRT", 0, 0, 0, -9.99f, mmd);
            NCTestUtils.assert3DVariable("tao-sss_RH", 0, 0, 0, -9.99f, mmd);

            final String[] names = mmd.getVariables().stream()
                    .map(variable -> variable.getShortName())
                    .collect(Collectors.toList()).toArray(new String[0]);
            assertArrayEquals(new String[]{
                    "smap-sss-for_time_for",
                    "smap-sss-for_cellat_for",
                    "smap-sss-for_cellon_for",
                    "smap-sss-for_gland_for",
                    "smap-sss-for_fland_for",
                    "smap-sss-for_gice_est",
                    "smap-sss-for_surtep",
                    "smap-sss-for_winspd",
                    "smap-sss-for_sss_ref",
                    "smap-sss-for_tran",
                    "smap-sss-for_tbup",
                    "smap-sss-for_tbdw",
                    "smap-sss-for_windir",
                    "smap-sss-for_rain",
                    "smap-sss-for_solar_flux",
                    "smap-sss-for_temp_ant_for_V",
                    "smap-sss-for_temp_ant_for_H",
                    "smap-sss-for_zang_for",
                    "smap-sss-for_alpha_for",
                    "smap-sss-for_eaa_for",
                    "smap-sss-for_eia_for",
                    "smap-sss-for_pra_for",
                    "smap-sss-for_sunglt_for",
                    "smap-sss-for_monglt_for",
                    "smap-sss-for_gallat_for",
                    "smap-sss-for_gallon_for",
                    "smap-sss-for_sun_beta_for",
                    "smap-sss-for_sun_alpha_for",
                    "smap-sss-for_ta_ant_filtered_for_V",
                    "smap-sss-for_ta_ant_filtered_for_H",
                    "smap-sss-for_ta_ant_filtered_for_S3",
                    "smap-sss-for_ta_ant_filtered_for_S4",
                    "smap-sss-for_ta_ant_for_V",
                    "smap-sss-for_ta_ant_for_H",
                    "smap-sss-for_ta_ant_for_S3",
                    "smap-sss-for_ta_ant_for_S4",
                    "smap-sss-for_dtemp_ant_for_V",
                    "smap-sss-for_dtemp_ant_for_H",
                    "smap-sss-for_ta_sun_dir_for_I",
                    "smap-sss-for_ta_sun_dir_for_Q",
                    "smap-sss-for_ta_sun_dir_for_S3",
                    "smap-sss-for_ta_sun_ref_for_I",
                    "smap-sss-for_ta_sun_ref_for_Q",
                    "smap-sss-for_ta_sun_ref_for_S3",
                    "smap-sss-for_ta_gal_dir_for_I",
                    "smap-sss-for_ta_gal_dir_for_Q",
                    "smap-sss-for_ta_gal_dir_for_S3",
                    "smap-sss-for_ta_gal_ref_for_I",
                    "smap-sss-for_ta_gal_ref_for_Q",
                    "smap-sss-for_ta_gal_ref_for_S3",
                    "smap-sss-for_ta_ant_calibrated_for_V",
                    "smap-sss-for_ta_ant_calibrated_for_H",
                    "smap-sss-for_ta_ant_calibrated_for_S3",
                    "smap-sss-for_ta_ant_calibrated_for_S4",
                    "smap-sss-for_ta_earth_for_V",
                    "smap-sss-for_ta_earth_for_H",
                    "smap-sss-for_ta_earth_for_S3",
                    "smap-sss-for_ta_earth_for_S4",
                    "smap-sss-for_tb_toi_for_V",
                    "smap-sss-for_tb_toi_for_H",
                    "smap-sss-for_tb_toi_for_S3",
                    "smap-sss-for_tb_toi_for_S4",
                    "smap-sss-for_tb_toa_for_V",
                    "smap-sss-for_tb_toa_for_H",
                    "smap-sss-for_tb_toa_for_S3",
                    "smap-sss-for_tb_toa_for_S4",
                    "smap-sss-for_tb_toa_lc_for_V",
                    "smap-sss-for_tb_toa_lc_for_H",
                    "smap-sss-for_tb_toa_lc_for_S3",
                    "smap-sss-for_tb_toa_lc_for_S4",
                    "smap-sss-for_dtb_land_correction_for_V",
                    "smap-sss-for_dtb_land_correction_for_H",
                    "smap-sss-for_dtb_sea_ice_correction_V",
                    "smap-sss-for_dtb_sea_ice_correction_H",
                    "smap-sss-for_tb_sur_for_V",
                    "smap-sss-for_tb_sur_for_H",
                    "smap-sss-for_tb_sur_for_S3",
                    "smap-sss-for_tb_sur_for_S4",
                    "smap-sss-for_tb_sur0_for_V",
                    "smap-sss-for_tb_sur0_for_H",
                    "smap-sss-for_tb_sur0_for_S3",
                    "smap-sss-for_tb_sur0_for_S4",
                    "smap-sss-for_tb_sur0_sic_for_V",
                    "smap-sss-for_tb_sur0_sic_for_H",
                    "smap-sss-for_tb_sur0_sic_for_S3",
                    "smap-sss-for_tb_sur0_sic_for_S4",
                    "smap-sss-for_sss_smap_for",
                    "smap-sss-for_sss_smap_40km_for",
                    "smap-sss-for_iqc_flag_for",
                    "smap-sss-for_anc_sea_ice_flag_ice1",
                    "smap-sss-for_anc_sea_ice_flag_ice2",
                    "smap-sss-for_anc_sea_ice_flag_ice3",
                    "smap-sss-for_sea_ice_zones",
                    "smap-sss-for_tb_consistency_for",
                    "smap-sss-for_ta_ant_exp_for_V",
                    "smap-sss-for_ta_ant_exp_for_H",
                    "smap-sss-for_ta_ant_exp_for_S3",
                    "smap-sss-for_ta_ant_exp_for_S4",
                    "smap-sss-for_tb_sur0_exp_for_V",
                    "smap-sss-for_tb_sur0_exp_for_H",
                    "smap-sss-for_tb_sur0_exp_for_S3",
                    "smap-sss-for_tb_sur0_exp_for_S4",
                    "smap-sss-for_pratot_exp_for",
                    "smap-sss-for_TEC_for",
                    "smap-sss-for_sss_smap_unc_for",
                    "smap-sss-for_sss_smap_40km_unc_for",
                    "smap-sss-for_sss_smap_unc_comp_ws-ran_for",
                    "smap-sss-for_sss_smap_unc_comp_nedt-v_for",
                    "smap-sss-for_sss_smap_unc_comp_nedt-h_for",
                    "smap-sss-for_sss_smap_unc_comp_sst_for",
                    "smap-sss-for_sss_smap_unc_comp_wdir_for",
                    "smap-sss-for_sss_smap_unc_comp_ref-gal_for",
                    "smap-sss-for_sss_smap_unc_comp_lnd-ctn_for",
                    "smap-sss-for_sss_smap_unc_comp_si-ctn_for",
                    "smap-sss-for_sss_smap_unc_comp_ws-sys_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_ws-ran_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_nedt-v_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_nedt-h_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_sst_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_wdir_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_ref-gal_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_lnd-ctn_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_si-ctn_for",
                    "smap-sss-for_sss_smap_40km_unc_comp_ws-sys_for",
                    "smap-sss-for_x",
                    "smap-sss-for_y",
                    "smap-sss-for_file_name",
                    "smap-sss-for_processing_version",
                    "smap-sss-for_acquisition_time",
                    "tao-sss_longitude",
                    "tao-sss_latitude",
                    "tao-sss_time",
                    "tao-sss_SSS",
                    "tao-sss_SST",
                    "tao-sss_AIRT",
                    "tao-sss_RH",
                    "tao-sss_WSPD",
                    "tao-sss_WDIR",
                    "tao-sss_BARO",
                    "tao-sss_RAIN",
                    "tao-sss_Q",
                    "tao-sss_M",
                    "tao-sss_x",
                    "tao-sss_y",
                    "tao-sss_file_name",
                    "tao-sss_processing_version",
                    "tao-sss_acquisition_time"
            }, names);
        }
    }

    private void insert_tao_sss() throws IOException, SQLException {
        final String sensorKey = "tao-sss";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", sensorKey, "v1", "2018", "02", "TAO_T0N110W_DM234A-20170610_2018-02.txt"}, true);
        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_smap_sss_for() throws IOException, SQLException {
        final String sensorKey = "smap-sss-for";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v05.0", "2018", "02", "04", "RSS_SMAP_SSS_L2C_r16080_20180204T004140_2018035_FNL_V05.0.nc"}, true);
        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v05.0");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String insituName) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(insituName);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("smap-sss-for"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(insituName, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("smap-sss-for", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmdxxx")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-xxx").getPath())
                .withDimensions(dimensions);
    }
}
