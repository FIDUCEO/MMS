package com.bc.fiduceo.post.plugin.era5;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

        assertEquals(-1, config.get_time_steps_past());
        assertEquals(-1, config.get_time_steps_future());
        assertNull(config.getInsituRef());
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

    @Test
    public void testSetGetTimeStepsPast() {
        config.set_time_steps_past(12);
        assertEquals(12, config.get_time_steps_past());
    }

    @Test
    public void testSetGetTimeStepsFuture() {
        config.set_time_steps_future(13);
        assertEquals(13, config.get_time_steps_future());
    }

    @Test
    public void testSetGetTimeDimName() {
        config.set_time_dim_name("Rolex");
        assertEquals("Rolex", config.get_time_dim_name());
    }

    @Test
    public void testSetGetTimeVariableName() {
        config.set_time_variable_name("clocktock");
        assertEquals("clocktock", config.get_time_variable_name());
    }

    @Test
    public void testSetGetLongitudeVariableName() {
        config.set_longitude_variable_name("longo");
        assertEquals("longo", config.get_longitude_variable_name());
    }

    @Test
    public void testSetGetLatitudeVariableName() {
        config.set_latitude_variable_name("latti");
        assertEquals("latti", config.get_latitude_variable_name());
    }

    @Test
    public void testSetGet_nwp_time_variable_name() {
        config.set_nwp_time_variable_name("the_time");
        assertEquals("the_time", config.get_nwp_time_variable_name());
    }

    @Test
    public void testSetGetInsituRef() {
        config.setInsituRef("the_buoy");
        assertEquals("the_buoy", config.getInsituRef());
    }

    @Test
    public void testGetVariablesWithReplacement() {
        config.setInsituRef("A-ship-track");

        config.set_latitude_variable_name("{insitu-ref}_insitu.lat");
        assertEquals("A-ship-track_insitu.lat", config.get_latitude_variable_name());

        config.set_longitude_variable_name("{insitu-ref}_insitu.lon");
        assertEquals("A-ship-track_insitu.lon", config.get_longitude_variable_name());

        config.set_nwp_time_variable_name("tick-tack");
        assertEquals("tick-tack", config.get_nwp_time_variable_name());

        config.set_time_variable_name("once{insitu-ref}inbetween");
        assertEquals("onceA-ship-trackinbetween", config.get_time_variable_name());

        config.set_an_u10_name("{insitu-ref}_insitu.u10");
        assertEquals("A-ship-track_insitu.u10", config.get_an_u10_name());

        config.set_an_v10_name("{insitu-ref}_insitu.v10");
        assertEquals("A-ship-track_insitu.v10", config.get_an_v10_name());

        config.set_an_siconc_name("an_{insitu-ref}_insitu.siconc");
        assertEquals("an_A-ship-track_insitu.siconc", config.get_an_siconc_name());

        config.set_an_sst_name("an_{insitu-ref}_insitu.sst");
        assertEquals("an_A-ship-track_insitu.sst", config.get_an_sst_name());

        config.set_fc_metss_name("fc_{insitu-ref}_insitu.metss");
        assertEquals("fc_A-ship-track_insitu.metss", config.get_fc_metss_name());

        config.set_fc_mntss_name("{insitu-ref}_insitu.mntss");
        assertEquals("A-ship-track_insitu.mntss", config.get_fc_mntss_name());

        config.set_fc_mslhf_name("{insitu-ref}_insitu.mslhf");
        assertEquals("A-ship-track_insitu.mslhf", config.get_fc_mslhf_name());

        config.set_fc_msnlwrf_name("{insitu-ref}_insitu.msnlwrf");
        assertEquals("A-ship-track_insitu.msnlwrf", config.get_fc_msnlwrf_name());

        config.set_fc_msnswrf_name("{insitu-ref}_insitu.msnswrf");
        assertEquals("A-ship-track_insitu.msnswrf", config.get_fc_msnswrf_name());

        config.set_fc_msshf_name("{insitu-ref}_insitu.msshf");
        assertEquals("A-ship-track_insitu.msshf", config.get_fc_msshf_name());
    }

    @Test
    public void testVerify() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        assertTrue(config.verify());
    }

    @Test
    public void testVerify_missingTimeStepsPast() {
        // config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingTimeStepsFuture() {
        config.set_time_steps_past(13);
        // config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingTimeDimName() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
//         config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingTimeVariableName() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
//        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingLongitudeVariableName() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
//        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingLatitudeVariableName() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
//        config.set_latitude_variable_name("Lama");
        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_missingNwpTimeVariableName() {
        config.set_time_steps_past(13);
        config.set_time_steps_future(14);
        config.set_time_dim_name("ticktock");
        config.set_time_variable_name("yamas");
        config.set_longitude_variable_name("Lore");
        config.set_latitude_variable_name("Lama");
//        config.set_nwp_time_variable_name("sachupang");

        try {
            assertTrue(config.verify());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
