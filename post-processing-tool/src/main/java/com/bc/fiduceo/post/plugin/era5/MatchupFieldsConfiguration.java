package com.bc.fiduceo.post.plugin.era5;

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

    String get_fc_mntss_name() {
        return fc_mntss_name;
    }

    String get_fc_mslhf_name() {
        return fc_mslhf_name;
    }

    String get_fc_msnlwrf_name() {
        return fc_msnlwrf_name;
    }

    String get_fc_msnswrf_name() {
        return fc_msnswrf_name;
    }

    String get_fc_msshf_name() {
        return fc_msshf_name;
    }
}
