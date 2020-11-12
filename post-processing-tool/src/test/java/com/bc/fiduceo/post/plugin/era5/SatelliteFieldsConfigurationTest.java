package com.bc.fiduceo.post.plugin.era5;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SatelliteFieldsConfigurationTest {

    private SatelliteFieldsConfiguration config;

    @Before
    public void setUp() {
        config = new SatelliteFieldsConfiguration();
    }

    @Test
    public void testConstructionAndDefaultValues() {
        assertEquals("nwp_q", config.get_an_q_name());
        assertEquals("nwp_t", config.get_an_t_name());
        assertEquals("nwp_o3", config.get_an_o3_name());
        assertEquals("nwp_lnsp", config.get_an_lnsp_name());
        assertEquals("nwp_t2m", config.get_an_t2m_name());
        assertEquals("nwp_siconc", config.get_an_siconc_name());
        assertEquals("nwp_u10", config.get_an_u10_name());
        assertEquals("nwp_v10", config.get_an_v10_name());
        assertEquals("nwp_msl", config.get_an_msl_name());
        assertEquals("nwp_skt", config.get_an_skt_name());
        assertEquals("nwp_sst", config.get_an_sst_name());
        assertEquals("nwp_tcc", config.get_an_tcc_name());
        assertEquals("nwp_tcwv", config.get_an_tcwv_name());
    }

    @Test
    public void testSetGet_an_q() {
        config.set_an_q_name("anku");
        assertEquals("anku", config.get_an_q_name());
    }

    @Test
    public void testSetGet_an_t() {
        config.set_an_t_name("tee");
        assertEquals("tee", config.get_an_t_name());
    }

    @Test
    public void testSetGet_an_o3() {
        config.set_an_o3_name("ozzi");
        assertEquals("ozzi", config.get_an_o3_name());
    }

    @Test
    public void testSetGet_an_lnsp() {
        config.set_an_lnsp_name("pratt");
        assertEquals("pratt", config.get_an_lnsp_name());
    }

    @Test
    public void testSetGet_an_t2m() {
        config.set_an_t2m_name("tempi");
        assertEquals("tempi", config.get_an_t2m_name());
    }

    @Test
    public void testSetGet_an_u10() {
        config.set_an_u10_name("windu");
        assertEquals("windu", config.get_an_u10_name());
    }

    @Test
    public void testSetGet_an_v10() {
        config.set_an_v10_name("Vicky");
        assertEquals("Vicky", config.get_an_v10_name());
    }

    @Test
    public void testSetGet_an_siconc() {
        config.set_an_siconc_name("sieglinde");
        assertEquals("sieglinde", config.get_an_siconc_name());
    }

    @Test
    public void testSetGet_an_mslc() {
        config.set_an_msl_name("meanSurf");
        assertEquals("meanSurf", config.get_an_msl_name());
    }

    @Test
    public void testSetGet_an_skt() {
        config.set_an_skt_name("scinny");
        assertEquals("scinny", config.get_an_skt_name());
    }

    @Test
    public void testSetGet_an_sst() {
        config.set_an_sst_name("seaTemp");
        assertEquals("seaTemp", config.get_an_sst_name());
    }

    @Test
    public void testSetGet_an_tcc() {
        config.set_an_tcc_name("cloudCover");
        assertEquals("cloudCover", config.get_an_tcc_name());
    }

    @Test
    public void testSetGet_an_tcwv() {
        config.set_an_tcwv_name("steamy");
        assertEquals("steamy", config.get_an_tcwv_name());
    }
}
