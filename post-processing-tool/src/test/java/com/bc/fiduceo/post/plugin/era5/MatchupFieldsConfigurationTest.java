package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatchupFieldsConfigurationTest {

    @Test
    public void testConstructionAndDefaultValues() {
        final MatchupFieldsConfiguration config = new MatchupFieldsConfiguration();

        assertEquals("nwp_mu_u10", config.get_an_u10_name());
        assertEquals("nwp_mu_v10", config.get_an_v10_name());
        assertEquals("nwp_mu_siconc", config.get_an_siconc_name());
        assertEquals("nwp_mu_sst", config.get_an_sst_name());

        assertEquals("nwp_mu_metss", config.get_fc_metss_name());
        assertEquals("nwp_mu_mntss", config.get_fc_mntss_name());
        assertEquals("nwp_mu_mslhf", config.get_fc_mslhf_name());
        assertEquals("nwp_mu_msnlwrf", config.get_fc_msnlwrf_name());
        assertEquals("nwp_mu_msnswrf", config.get_fc_msnswrf_name());
        assertEquals("nwp_mu_msshf", config.get_fc_msshf_name());
    }
}
