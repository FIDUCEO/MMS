package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.util.StringUtils;

class SatelliteFieldsConfiguration {

    private String an_q_name;
    private String an_t_name;
    private String an_o3_name;
    private String an_lnsp_name;
    private String an_siconc_name;
    private String an_t2m_name;
    private String an_u10_name;
    private String an_v10_name;
    private String an_msl_name;
    private String an_skt_name;
    private String an_sst_name;
    private String an_tcc_name;
    private String an_tcwv_name;

    private int x_dim;
    private int y_dim;
    private int z_dim;
    private String x_dim_name;
    private String y_dim_name;
    private String z_dim_name;

    private String nwp_time_variable_name;
    private String longitude_variable_name;
    private String latitude_variable_name;
    private String time_variable_name;

    SatelliteFieldsConfiguration() {
        an_q_name = "nwp_q";
        an_t_name = "nwp_t";
        an_o3_name = "nwp_o3";
        an_lnsp_name = "nwp_lnsp";
        an_siconc_name = "nwp_siconc";
        an_t2m_name = "nwp_t2m";
        an_u10_name = "nwp_u10";
        an_v10_name = "nwp_v10";
        an_msl_name = "nwp_msl";
        an_skt_name = "nwp_skt";
        an_sst_name = "nwp_sst";
        an_tcc_name = "nwp_tcc";
        an_tcwv_name = "nwp_tcwv";

        x_dim = -1;
        y_dim = -1;
        z_dim = -1;
    }

    String get_an_q_name() {
        return an_q_name;
    }

    void set_an_q_name(String an_q_name) {
        this.an_q_name = an_q_name;
    }

    String get_an_t_name() {
        return an_t_name;
    }

    void set_an_t_name(String an_t_name) {
        this.an_t_name = an_t_name;
    }

    String get_an_o3_name() {
        return an_o3_name;
    }

    void set_an_o3_name(String an_o3_name) {
        this.an_o3_name = an_o3_name;
    }

    String get_an_lnsp_name() {
        return an_lnsp_name;
    }

    void set_an_lnsp_name(String an_lnsp_name) {
        this.an_lnsp_name = an_lnsp_name;
    }

    String get_an_t2m_name() {
        return an_t2m_name;
    }

    void set_an_t2m_name(String an_t2m_name) {
        this.an_t2m_name = an_t2m_name;
    }

    String get_an_siconc_name() {
        return an_siconc_name;
    }

    void set_an_siconc_name(String an_siconc_name) {
        this.an_siconc_name = an_siconc_name;
    }

    String get_an_u10_name() {
        return an_u10_name;
    }

    void set_an_u10_name(String an_u10_name) {
        this.an_u10_name = an_u10_name;
    }

    String get_an_v10_name() {
        return an_v10_name;
    }

    void set_an_v10_name(String an_v10_name) {
        this.an_v10_name = an_v10_name;
    }

    String get_an_msl_name() {
        return an_msl_name;
    }

    void set_an_msl_name(String an_msl_name) {
        this.an_msl_name = an_msl_name;
    }

    String get_an_skt_name() {
        return an_skt_name;
    }

    void set_an_skt_name(String an_skt_name) {
        this.an_skt_name = an_skt_name;
    }

    String get_an_sst_name() {
        return an_sst_name;
    }

    void set_an_sst_name(String an_sst_name) {
        this.an_sst_name = an_sst_name;
    }

    String get_an_tcc_name() {
        return an_tcc_name;
    }

    void set_an_tcc_name(String an_tcc_name) {
        this.an_tcc_name = an_tcc_name;
    }

    String get_an_tcwv_name() {
        return an_tcwv_name;
    }

    void set_an_tcwv_name(String an_tcwv_name) {
        this.an_tcwv_name = an_tcwv_name;
    }

    int get_x_dim() {
        return x_dim;
    }

    void set_x_dim(int x_dim) {
        this.x_dim = x_dim;
    }

    int get_y_dim() {
        return y_dim;
    }

    void set_y_dim(int y_dim) {
        this.y_dim = y_dim;
    }

    int get_z_dim() {
        return z_dim;
    }

    void set_z_dim(int z_dim) {
        this.z_dim = z_dim;
    }

    String get_x_dim_name() {
        return x_dim_name;
    }

    void set_x_dim_name(String x_dim_name) {
        this.x_dim_name = x_dim_name;
    }

    String get_y_dim_name() {
        return y_dim_name;
    }

    void set_y_dim_name(String y_dim_name) {
        this.y_dim_name = y_dim_name;
    }

    String get_z_dim_name() {
        return z_dim_name;
    }

    void set_z_dim_name(String z_dim_name) {
        this.z_dim_name = z_dim_name;
    }

    String get_nwp_time_variable_name() {
        return nwp_time_variable_name;
    }

    void set_nwp_time_variable_name(String nwp_time_variable_name) {
        this.nwp_time_variable_name =nwp_time_variable_name;
    }

    String get_time_variable_name() {
        return time_variable_name;
    }
    
    void set_time_variable_name(String time_variable_name) {
        this.time_variable_name = time_variable_name;
    }

    String get_longitude_variable_name() {
        return longitude_variable_name;
    }

    void set_longitude_variable_name(String longitude_variable_name) {
        this.longitude_variable_name = longitude_variable_name;
    }

    String get_latitude_variable_name() {
        return latitude_variable_name;
    }

    void set_latitude_variable_name(String latitude_variable_name) {
        this.latitude_variable_name = latitude_variable_name;
    }


    void verify() {
        if (x_dim < 1 || y_dim < 1) {
            // do not check z-dimension, this might be not configured tb 2020-11-16
            throw new IllegalArgumentException("dimensions incorrect: x:" + x_dim + " y:" + y_dim);
        }

        if (StringUtils.isNullOrEmpty(x_dim_name)) {
            throw new IllegalArgumentException("x dimension name not configured");
        }

        if (StringUtils.isNullOrEmpty(y_dim_name)) {
            throw new IllegalArgumentException("y dimension name not configured");
        }

        if (StringUtils.isNullOrEmpty(z_dim_name)) {
            throw new IllegalArgumentException("z dimension name not configured");
        }

        if (StringUtils.isNullOrEmpty(nwp_time_variable_name)) {
            throw new IllegalArgumentException("era-5 time variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(time_variable_name)) {
            throw new IllegalArgumentException("satellite time variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(longitude_variable_name)) {
            throw new IllegalArgumentException("satellite lon variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(latitude_variable_name)) {
            throw new IllegalArgumentException("satellite lat variable name not configured");
        }
    }
}
