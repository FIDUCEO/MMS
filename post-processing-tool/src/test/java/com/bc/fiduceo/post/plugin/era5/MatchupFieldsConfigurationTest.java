package com.bc.fiduceo.post.plugin.era5;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatchupFieldsConfigurationTest {

    private MatchupFieldsConfiguration config;

    @Before
    public void setUp() {
        config = new MatchupFieldsConfiguration();
    }

    @Test
    public void testConstructionAndDefaultValues() {
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

    @Test
    public void testSetGet_an_u10() {
        config.set_an_u10_name("windu");
        assertEquals("windu", config.get_an_u10_name());
    }

    @Test
    public void testSetGet_an_v10() {
        config.set_an_v10_name("viind");
        assertEquals("viind", config.get_an_v10_name());
    }

    @Test
    public void testSetGet_an_siconc() {
        config.set_an_siconc_name("wicki-sicki");
        assertEquals("wicki-sicki", config.get_an_siconc_name());
    }

    @Test
    public void testSetGet_an_sst() {
        config.set_an_sst_name("skintemp");
        assertEquals("skintemp", config.get_an_sst_name());
    }

    @Test
    public void testSetGet_fc_metss() {
        config.set_fc_metss_name("metessi");
        assertEquals("metessi", config.get_fc_metss_name());
    }

    @Test
    public void testSetGet_fc_mntss() {
        config.set_fc_mntss_name("mento");
        assertEquals("mento", config.get_fc_mntss_name());
    }

    @Test
    public void testSetGet_fc_mslhf() {
        config.set_fc_mslhf_name("heatison");
        assertEquals("heatison", config.get_fc_mslhf_name());
    }

    @Test
    public void testSetGet_fc_msnlwrf() {
        config.set_fc_msnlwrf_name("flaxpopax");
        assertEquals("flaxpopax", config.get_fc_msnlwrf_name());
    }

    @Test
    public void testSetGet_fc_msnswrf() {
        config.set_fc_msnswrf_name("shorty");
        assertEquals("shorty", config.get_fc_msnswrf_name());
    }

    @Test
    public void testSetGet_fc_msshf() {
        config.set_fc_msshf_name("heffalump");
        assertEquals("heffalump", config.get_fc_msshf_name());
    }
}
