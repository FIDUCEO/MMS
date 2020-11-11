package com.bc.fiduceo.post.plugin.era5;

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

    String get_an_lnsp_name() {
        return an_lnsp_name;
    }

    String get_an_t2m_name() {
        return an_t2m_name;
    }

    String get_an_siconc_name() {
        return an_siconc_name;
    }

    String get_an_u10_name() {
        return an_u10_name;
    }

    String get_an_v10_name() {
        return an_v10_name;
    }

    String get_an_msl_name() {
        return an_msl_name;
    }

    String get_an_skt_name() {
        return an_skt_name;
    }

    String get_an_sst_name() {
        return an_sst_name;
    }

    String get_an_tcc_name() {
        return an_tcc_name;
    }

    String get_an_tcwv_name() {
        return an_tcwv_name;
    }
}
