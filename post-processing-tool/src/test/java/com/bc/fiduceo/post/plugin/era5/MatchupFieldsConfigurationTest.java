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
    }

    @Test
    public void testSetGet_an_u10() {
        config.set_an_u10_name("windu");
        assertEquals("windu", config.get_an_u10_name());

        config.set_an_u10_name("wi.ndu");
        assertEquals("wi\\.ndu", config.get_an_u10_name());
    }

    @Test
    public void testSetGet_an_v10() {
        config.set_an_v10_name("viind");
        assertEquals("viind", config.get_an_v10_name());

        config.set_an_v10_name("vi.ind");
        assertEquals("vi\\.ind", config.get_an_v10_name());
    }

    @Test
    public void testSetGet_an_siconc() {
        config.set_an_siconc_name("wicki-sicki");
        assertEquals("wicki-sicki", config.get_an_siconc_name());

        config.set_an_siconc_name("wicki!sicki");
        assertEquals("wicki\\!sicki", config.get_an_siconc_name());
    }

    @Test
    public void testSetGet_an_sst() {
        config.set_an_sst_name("skintemp");
        assertEquals("skintemp", config.get_an_sst_name());

        config.set_an_sst_name("skin.temp");
        assertEquals("skin\\.temp", config.get_an_sst_name());
    }

    @Test
    public void testSetGet_fc_metss() {
        config.set_fc_metss_name("metessi");
        assertEquals("metessi", config.get_fc_metss_name());

        config.set_fc_metss_name("mete.ssi");
        assertEquals("mete\\.ssi", config.get_fc_metss_name());
    }

    @Test
    public void testSetGet_fc_mntss() {
        config.set_fc_mntss_name("mento");
        assertEquals("mento", config.get_fc_mntss_name());

        config.set_fc_mntss_name("men.to");
        assertEquals("men\\.to", config.get_fc_mntss_name());
    }

    @Test
    public void testSetGet_fc_mslhf() {
        config.set_fc_mslhf_name("heatison");
        assertEquals("heatison", config.get_fc_mslhf_name());

        config.set_fc_mslhf_name("heat.ison");
        assertEquals("heat\\.ison", config.get_fc_mslhf_name());
    }

    @Test
    public void testSetGet_fc_msnlwrf() {
        config.set_fc_msnlwrf_name("flaxpopax");
        assertEquals("flaxpopax", config.get_fc_msnlwrf_name());

        config.set_fc_msnlwrf_name("flax.popax");
        assertEquals("flax\\.popax", config.get_fc_msnlwrf_name());
    }

    @Test
    public void testSetGet_fc_msnswrf() {
        config.set_fc_msnswrf_name("shorty");
        assertEquals("shorty", config.get_fc_msnswrf_name());

        config.set_fc_msnswrf_name("shor!ty");
        assertEquals("shor\\!ty", config.get_fc_msnswrf_name());
    }

    @Test
    public void testSetGet_fc_msshf() {
        config.set_fc_msshf_name("heffalump.");
        assertEquals("heffalump\\.", config.get_fc_msshf_name());
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

        config.set_time_dim_name("!Rolex");
        assertEquals("\\!Rolex", config.get_time_dim_name());
    }

    @Test
    public void testSetGetTimeVariableName() {
        config.set_time_variable_name("clocktock");
        assertEquals("clocktock", config.get_time_variable_name());

        config.set_time_variable_name("clock.tock");
        assertEquals("clock\\.tock", config.get_time_variable_name());
    }

    @Test
    public void testSetGetLongitudeVariableName() {
        config.set_longitude_variable_name("longo");
        assertEquals("longo", config.get_longitude_variable_name());

        config.set_longitude_variable_name("longo.bongo");
        assertEquals("longo\\.bongo", config.get_longitude_variable_name());
    }

    @Test
    public void testSetGetLatitudeVariableName() {
        config.set_latitude_variable_name("latti");
        assertEquals("latti", config.get_latitude_variable_name());

        config.set_latitude_variable_name("lat.ti");
        assertEquals("lat\\.ti", config.get_latitude_variable_name());
    }

    @Test
    public void testSetGet_nwp_time_variable_name() {
        config.set_nwp_time_variable_name("the_time");
        assertEquals("the_time", config.get_nwp_time_variable_name());

        config.set_nwp_time_variable_name("the.time");
        assertEquals("the\\.time", config.get_nwp_time_variable_name());
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
