package com.bc.fiduceo.post.plugin.era5;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

        assertEquals(-1, config.get_x_dim());
        assertEquals(-1, config.get_y_dim());
        assertEquals(-1, config.get_z_dim());
        assertNull(config.get_x_dim_name());
        assertNull(config.get_y_dim_name());
        assertNull(config.get_z_dim_name());
        assertNull(config.get_nwp_time_variable_name());
        assertNull(config.get_time_variable_name());
        assertNull(config.get_longitude_variable_name());
        assertNull(config.get_latitude_variable_name());
        assertNull(config.getSensorRef());
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

    @Test
    public void testSetGet_x_dim() {
        config.set_x_dim(12);
        assertEquals(12, config.get_x_dim());
    }

    @Test
    public void testSetGet_y_dim() {
        config.set_y_dim(13);
        assertEquals(13, config.get_y_dim());
    }

    @Test
    public void testSetGet_z_dim() {
        config.set_z_dim(14);
        assertEquals(14, config.get_z_dim());
    }

    @Test
    public void testSetGet_x_dim_name() {
        config.set_x_dim_name("watussi");
        assertEquals("watussi", config.get_x_dim_name());
    }

    @Test
    public void testSetGet_y_dim_name() {
        config.set_y_dim_name("yacanda");
        assertEquals("yacanda", config.get_y_dim_name());
    }

    @Test
    public void testSetGet_z_dim_name() {
        config.set_z_dim_name("zauberfee");
        assertEquals("zauberfee", config.get_z_dim_name());
    }

    @Test
    public void testSetGetSensorRef() {
        config.setSensorRef("radiometer");
        assertEquals("radiometer", config.getSensorRef());
    }

    @Test
    public void testGetVariablesWithReplacement() {
        config.setSensorRef("avhrr-n14");

        config.set_nwp_time_variable_name("{sensor-ref}_nwp_time");
        assertEquals("avhrr-n14_nwp_time", config.get_nwp_time_variable_name());

        config.set_longitude_variable_name("{sensor-ref}_nwp_lon");
        assertEquals("avhrr-n14_nwp_lon", config.get_longitude_variable_name());

        config.set_latitude_variable_name("{sensor-ref}_nwp_lat");
        assertEquals("avhrr-n14_nwp_lat", config.get_latitude_variable_name());

        config.set_time_variable_name("{sensor-ref}_acquisition-time");
        assertEquals("avhrr-n14_acquisition-time", config.get_time_variable_name());

        config.set_an_q_name("a_{sensor-ref}_n_q");
        assertEquals("a_avhrr-n14_n_q", config.get_an_q_name());

        config.set_an_t_name("an_{sensor-ref}_t");
        assertEquals("an_avhrr-n14_t", config.get_an_t_name());

        config.set_an_o3_name("{sensor-ref}_o3");
        assertEquals("avhrr-n14_o3", config.get_an_o3_name());

        config.set_an_lnsp_name("{sensor-ref}_lnsp");
        assertEquals("avhrr-n14_lnsp", config.get_an_lnsp_name());

        config.set_an_t2m_name("{sensor-ref}_t2m");
        assertEquals("avhrr-n14_t2m", config.get_an_t2m_name());

        config.set_an_siconc_name("{sensor-ref}_siconc");
        assertEquals("avhrr-n14_siconc", config.get_an_siconc_name());

        config.set_an_u10_name("{sensor-ref}_u10");
        assertEquals("avhrr-n14_u10", config.get_an_u10_name());

        config.set_an_v10_name("{sensor-ref}_v10");
        assertEquals("avhrr-n14_v10", config.get_an_v10_name());

        config.set_an_msl_name("{sensor-ref}_msl");
        assertEquals("avhrr-n14_msl", config.get_an_msl_name());

        config.set_an_skt_name("{sensor-ref}_skt");
        assertEquals("avhrr-n14_skt", config.get_an_skt_name());

        config.set_an_sst_name("{sensor-ref}_sst");
        assertEquals("avhrr-n14_sst", config.get_an_sst_name());

        config.set_an_tcc_name("{sensor-ref}_tcc");
        assertEquals("avhrr-n14_tcc", config.get_an_tcc_name());

        config.set_an_tcwv_name("{sensor-ref}_tcwv");
        assertEquals("avhrr-n14_tcwv", config.get_an_tcwv_name());
    }

    @Test
    public void testVerify() {
        prepareConfig();

        config.verify();
    }

    private void prepareConfig() {
        config.set_x_dim(3);
        config.set_x_dim_name("A");
        config.set_y_dim(4);
        config.set_y_dim_name("B");
        config.set_z_dim(4);
        config.set_z_dim_name("C");
        config.set_nwp_time_variable_name("D");
        config.set_time_variable_name("E");
        config.set_longitude_variable_name("F");
        config.set_latitude_variable_name("G");
    }

    @Test
    public void testVerify_x_dim() {
        prepareConfig();
        config.set_x_dim(-1);

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_x_dim_name() {
        prepareConfig();
        config.set_x_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_y_dim() {
        prepareConfig();
        config.set_y_dim(-2);

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_y_dim_name() {
        prepareConfig();
        config.set_y_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_z_dim_name() {
        prepareConfig();
        config.set_z_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_nwp_time_variable_name() {
        prepareConfig();
        config.set_nwp_time_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_time_variable_name() {
        prepareConfig();
        config.set_time_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_lon_variable_name() {
        prepareConfig();
        config.set_longitude_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_lat_variable_name() {
        prepareConfig();
        config.set_latitude_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSetGet_nwp_time_variable_name() {
        config.set_nwp_time_variable_name("tickTock");
        assertEquals("tickTock", config.get_nwp_time_variable_name());
    }

    @Test
    public void testSetGet_time_variable_name() {
        config.set_time_variable_name("twomins");
        assertEquals("twomins", config.get_time_variable_name());
    }
}
