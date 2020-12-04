package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.StringUtils;

class MatchupFieldsConfiguration {

    private String an_u10_name;
    private String an_v10_name;
    private String an_siconc_name;
    private String an_sst_name;
    private String fc_metss_name;
    private String fc_mntss_name;
    private String fc_mslhf_name;
    private String fc_msnlwrf_name;
    private String fc_msnswrf_name;
    private String fc_msshf_name;

    private int time_steps_past;
    private int time_steps_future;
    private String time_dim_name;

    MatchupFieldsConfiguration() {
        an_u10_name = "nwp_mu_u10";
        an_v10_name = "nwp_mu_v10";
        an_siconc_name = "nwp_mu_siconc";
        an_sst_name = "nwp_mu_sst";
        fc_metss_name = "nwp_mu_metss";
        fc_mntss_name = "nwp_mu_mntss";
        fc_mslhf_name = "nwp_mu_mslhf";
        fc_msnlwrf_name = "nwp_mu_msnlwrf";
        fc_msnswrf_name = "nwp_mu_msnswrf";
        fc_msshf_name = "nwp_mu_msshf";

        time_steps_past = -1;
        time_steps_future = -1;
        time_dim_name = null;
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

    String get_an_siconc_name() {
        return an_siconc_name;
    }

    void set_an_siconc_name(String an_siconc_name) {
        this.an_siconc_name = an_siconc_name;
    }

    String get_an_sst_name() {
        return an_sst_name;
    }

    void set_an_sst_name(String an_sst_name) {
        this.an_sst_name = an_sst_name;
    }

    String get_fc_metss_name() {
        return fc_metss_name;
    }

    void set_fc_metss_name(String fc_metss_name) {
        this.fc_metss_name = fc_metss_name;
    }

    String get_fc_mntss_name() {
        return fc_mntss_name;
    }

    void set_fc_mntss_name(String fc_mntss_name) {
        this.fc_mntss_name = fc_mntss_name;
    }

    String get_fc_mslhf_name() {
        return fc_mslhf_name;
    }

    void set_fc_mslhf_name(String fc_mslhf_name) {
        this.fc_mslhf_name = fc_mslhf_name;
    }

    String get_fc_msnlwrf_name() {
        return fc_msnlwrf_name;
    }

    void set_fc_msnlwrf_name(String fc_msnlwrf_name) {
        this.fc_msnlwrf_name = fc_msnlwrf_name;
    }

    String get_fc_msnswrf_name() {
        return fc_msnswrf_name;
    }

    void set_fc_msnswrf_name(String fc_msnswrf_name) {
        this.fc_msnswrf_name = fc_msnswrf_name;
    }

    String get_fc_msshf_name() {
        return fc_msshf_name;
    }

    void set_fc_msshf_name(String fc_msshf_name) {
        this.fc_msshf_name = fc_msshf_name;
    }

    int getTime_steps_past() {
        return time_steps_past;
    }

    void setTime_steps_past(int time_steps_past) {
        this.time_steps_past = time_steps_past;
    }

    int getTime_steps_future() {
        return time_steps_future;
    }

    void setTime_steps_future(int time_steps_future) {
        this.time_steps_future = time_steps_future;
    }

    String getTime_dim_name() {
        return time_dim_name;
    }

    void setTime_dim_name(String time_dim_name) {
        this.time_dim_name = time_dim_name;
    }

    boolean verify() {
        if (time_steps_past < 0) {
            throw new IllegalArgumentException("time steps past not configured");
        }
        if (time_steps_future < 0) {
            throw new IllegalArgumentException("time steps future not configured");
        }
        if (StringUtils.isNullOrEmpty(time_dim_name)){
            throw new IllegalArgumentException("time dimension name not configured");
        }
        return true;
    }
}
