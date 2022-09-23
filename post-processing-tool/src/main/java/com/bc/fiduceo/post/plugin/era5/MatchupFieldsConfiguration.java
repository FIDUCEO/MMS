package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.StringUtils;

class MatchupFieldsConfiguration extends FieldsConfiguration {

    public static final String INSITU_REF = "{insitu-ref}";
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
    private String time_variable_name;
    private String longitude_variable_name;
    private String latitude_variable_name;
    private String nwp_time_variable_name;
    private String insituRef;

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
        time_variable_name = null;
        longitude_variable_name = null;
        latitude_variable_name = null;
        nwp_time_variable_name = null;
        insituRef = null;
    }

    String get_an_u10_name() {
        return expand(an_u10_name);
    }

    void set_an_u10_name(String an_u10_name) {
        this.an_u10_name = an_u10_name;
    }

    String get_an_v10_name() {
        return expand(an_v10_name);
    }

    void set_an_v10_name(String an_v10_name) {
        this.an_v10_name = an_v10_name;
    }

    String get_an_siconc_name() {
        return expand(an_siconc_name);
    }

    void set_an_siconc_name(String an_siconc_name) {
        this.an_siconc_name = an_siconc_name;
    }

    String get_an_sst_name() {
        return expand(an_sst_name);
    }

    void set_an_sst_name(String an_sst_name) {
        this.an_sst_name = an_sst_name;
    }

    String get_fc_metss_name() {
        return expand(fc_metss_name);
    }

    void set_fc_metss_name(String fc_metss_name) {
        this.fc_metss_name = fc_metss_name;
    }

    String get_fc_mntss_name() {
        return expand(fc_mntss_name);
    }

    void set_fc_mntss_name(String fc_mntss_name) {
        this.fc_mntss_name = fc_mntss_name;
    }

    String get_fc_mslhf_name() {
        return expand(fc_mslhf_name);
    }

    void set_fc_mslhf_name(String fc_mslhf_name) {
        this.fc_mslhf_name = fc_mslhf_name;
    }

    String get_fc_msnlwrf_name() {
        return expand(fc_msnlwrf_name);
    }

    void set_fc_msnlwrf_name(String fc_msnlwrf_name) {
        this.fc_msnlwrf_name = fc_msnlwrf_name;
    }

    String get_fc_msnswrf_name() {
        return expand(fc_msnswrf_name);
    }

    void set_fc_msnswrf_name(String fc_msnswrf_name) {
        this.fc_msnswrf_name = fc_msnswrf_name;
    }

    String get_fc_msshf_name() {
        return expand(fc_msshf_name);
    }

    void set_fc_msshf_name(String fc_msshf_name) {
        this.fc_msshf_name = fc_msshf_name;
    }

    int get_time_steps_past() {
        return time_steps_past;
    }

    void set_time_steps_past(int time_steps_past) {
        this.time_steps_past = time_steps_past;
    }

    int get_time_steps_future() {
        return time_steps_future;
    }

    void set_time_steps_future(int time_steps_future) {
        this.time_steps_future = time_steps_future;
    }

    String get_time_dim_name() {
        return time_dim_name;
    }

    void set_time_dim_name(String time_dim_name) {
        this.time_dim_name = time_dim_name;
    }

    String get_time_variable_name() {
        return expand(time_variable_name);
    }

    void set_time_variable_name(String time_variable_name) {
        this.time_variable_name = time_variable_name;
    }

    String get_longitude_variable_name() {
        return expand(longitude_variable_name);
    }

    void set_longitude_variable_name(String longitude_variable_name) {
        this.longitude_variable_name = longitude_variable_name;
    }

    String get_latitude_variable_name() {
        return expand(latitude_variable_name);
    }

    void set_latitude_variable_name(String latitude_variable_name) {
        this.latitude_variable_name = latitude_variable_name;
    }

    String get_nwp_time_variable_name() {
        return expand(nwp_time_variable_name);
    }

    void set_nwp_time_variable_name(String nwp_time_variable_name) {
        this.nwp_time_variable_name = nwp_time_variable_name;
    }

    public String getInsituRef() {
        return insituRef;
    }

    public void setInsituRef(String insituRef) {
        this.insituRef = insituRef;
    }

    boolean verify() {
        if (time_steps_past < 0) {
            throw new IllegalArgumentException("time steps past not configured");
        }
        if (time_steps_future < 0) {
            throw new IllegalArgumentException("time steps future not configured");
        }
        if (StringUtils.isNullOrEmpty(time_dim_name)) {
            throw new IllegalArgumentException("time dimension name not configured");
        }
        if (StringUtils.isNullOrEmpty(time_variable_name)) {
            throw new IllegalArgumentException("time variable name not configured");
        }
        if (StringUtils.isNullOrEmpty(longitude_variable_name)) {
            throw new IllegalArgumentException("longitude variable name not configured");
        }
        if (StringUtils.isNullOrEmpty(latitude_variable_name)) {
            throw new IllegalArgumentException("latitude variable name not configured");
        }
        if (StringUtils.isNullOrEmpty(nwp_time_variable_name)) {
            throw new IllegalArgumentException("nwp time variable name not configured");
        }
        return true;
    }

    private String expand(String variableName) {
        return expand(variableName, INSITU_REF, insituRef);
    }
}
