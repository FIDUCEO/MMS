/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorExtractConfigurationTest {

    private SensorExtractConfiguration config;

    @Before
    public void setUp() {
        config = new SensorExtractConfiguration();
    }

    @Test
    public void testSetGetTimeVariableName() {
        final String variableName = "ti-hime";

        config.setTimeVariableName(variableName);
        assertEquals(variableName, config.getTimeVariableName());
    }

    @Test
    public void testSetGetAn_CI_name() {
        final String variableName = "fraction";

        config.setAn_CI_name(variableName);
        assertEquals(variableName, config.getAn_CI_name());
    }

    @Test
    public void testSetGetAn_ASN_name() {
        final String variableName = "albedo";

        config.setAn_ASN_name(variableName);
        assertEquals(variableName, config.getAn_ASN_name());
    }

    @Test
    public void testSetGetAn_SSTK_name() {
        final String variableName = "warm_water";

        config.setAn_SSTK_name(variableName);
        assertEquals(variableName, config.getAn_SSTK_name());
    }

    @Test
    public void testSetGetAn_TCWV_name() {
        final String variableName = "steamy";

        config.setAn_TCWV_name(variableName);
        assertEquals(variableName, config.getAn_TCWV_name());
    }

    @Test
    public void testSetGetAn_MSL_name() {
        final String variableName = "pressure";

        config.setAn_MSL_name(variableName);
        assertEquals(variableName, config.getAn_MSL_name());
    }

    @Test
    public void testSetGetAn_TCC_name() {
        final String variableName = "cloudiness";

        config.setAn_TCC_name(variableName);
        assertEquals(variableName, config.getAn_TCC_name());
    }

    @Test
    public void testSetGetAn_U10_name() {
        final String variableName = "east_wind";

        config.setAn_U10_name(variableName);
        assertEquals(variableName, config.getAn_U10_name());
    }

    @Test
    public void testSetGetAn_V10_name() {
        final String variableName = "north_wind";

        config.setAn_V10_name(variableName);
        assertEquals(variableName, config.getAn_V10_name());
    }

    @Test
    public void testSetGetAn_T2_name() {
        final String variableName = "2m_temp";

        config.setAn_T2_name(variableName);
        assertEquals(variableName, config.getAn_T2_name());
    }

    @Test
    public void testSetGetAn_D2_name() {
        final String variableName = "dew_pt";

        config.setAn_D2_name(variableName);
        assertEquals(variableName, config.getAn_D2_name());
    }

    @Test
    public void testSetGetAn_AL_name() {
        final String variableName = "albedo";

        config.setAn_AL_name(variableName);
        assertEquals(variableName, config.getAn_AL_name());
    }

    @Test
    public void testSetGetAn_SKT_name() {
        final String variableName = "skin-temp";

        config.setAn_SKT_name(variableName);
        assertEquals(variableName, config.getAn_SKT_name());
    }

    @Test
    public void testSetGetAn_LNSP_name() {
        final String variableName = "log-pressure";

        config.setAn_LNSP_name(variableName);
        assertEquals(variableName, config.getAn_LNSP_name());
    }

    @Test
    public void testSetGetAn_T_name() {
        final String variableName = "temp-profil";

        config.setAn_T_name(variableName);
        assertEquals(variableName, config.getAn_T_name());
    }

    @Test
    public void testSetGetAn_Q_name() {
        final String variableName = "vapour-profil";

        config.setAn_Q_name(variableName);
        assertEquals(variableName, config.getAn_Q_name());
    }

    @Test
    public void testSetGetAn_03_name() {
        final String variableName = "ozone-profil";

        config.setAn_O3_name(variableName);
        assertEquals(variableName, config.getAn_O3_name());
    }

    @Test
    public void testSetGetAn_CLWC_name() {
        final String variableName = "liquid_water";

        config.setAn_CLWC_name(variableName);
        assertEquals(variableName, config.getAn_CLWC_name());
    }

    @Test
    public void testSetGetAn_CIWC_name() {
        final String variableName = "cloud-ice";

        config.setAn_CIWC_name(variableName);
        assertEquals(variableName, config.getAn_CIWC_name());
    }

    @Test
    public void testSetGetAn_TP_name() {
        final String variableName = "precip";

        config.setAn_TP_name(variableName);
        assertEquals(variableName, config.getAn_TP_name());
    }

    @Test
    public void testGetDefaultValues() {
        assertEquals("amsre.nwp.cloud_ice_water", config.getAn_CI_name());
        assertEquals("amsre.nwp.snow_albedo", config.getAn_ASN_name());
        assertEquals("amsre.nwp.sea_surface_temperature", config.getAn_SSTK_name());
        assertEquals("amsre.nwp.total_column_water_vapour", config.getAn_TCWV_name());
        assertEquals("amsre.nwp.mean_sea_level_pressure", config.getAn_MSL_name());
        assertEquals("amsre.nwp.total_cloud_cover", config.getAn_TCC_name());
        assertEquals("amsre.nwp.10m_east_wind_component", config.getAn_U10_name());
        assertEquals("amsre.nwp.10m_north_wind_component", config.getAn_V10_name());
        assertEquals("amsre.nwp.2m_temperature", config.getAn_T2_name());
        assertEquals("amsre.nwp.2m_dew_point", config.getAn_D2_name());
        assertEquals("amsre.nwp.albedo", config.getAn_AL_name());
        assertEquals("amsre.nwp.skin_temperature", config.getAn_SKT_name());
        assertEquals("amsre.nwp.log_surface_pressure", config.getAn_LNSP_name());
        assertEquals("amsre.nwp.temperature_profile", config.getAn_T_name());
        assertEquals("amsre.nwp.water_vapour_profile", config.getAn_Q_name());
        assertEquals("amsre.nwp.ozone_profile", config.getAn_O3_name());
        assertEquals("amsre.nwp.cloud_liquid_water", config.getAn_CLWC_name());
        assertEquals("amsre.nwp.cloud_ice_water", config.getAn_CIWC_name());
        assertEquals("amsre.nwp.total_precip", config.getAn_TP_name());
    }
}
