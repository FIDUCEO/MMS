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
import static org.junit.Assert.assertNull;

public class TimeSeriesConfigurationTest {

    private TimeSeriesConfiguration config;

    @Before
    public void setUp() {
        config = new TimeSeriesConfiguration();
    }

    @Test
    public void testSetGetAnalysisSteps() {
        config.setAnalysisSteps(19);
        assertEquals(19, config.getAnalysisSteps());
    }

    @Test
    public void testSetGetForecastSteps() {
        config.setForecastSteps(31);
        assertEquals(31, config.getForecastSteps());
    }

    @Test
    public void testSetGetTimeVariableName() {
        final String timeVariableName = "clock";

        config.setTimeVariableName(timeVariableName);
        assertEquals(timeVariableName, config.getTimeVariableName());
    }

    @Test
    public void testSetGetLongitudeVariableName() {
        final String variableName = "longi-tuhude";

        config.setLongitudeVariableName(variableName);
        assertEquals(variableName, config.getLongitudeVariableName());
    }

    @Test
    public void testSetGetLatitudeVariableName() {
        final String variableName = "lat-popat";

        config.setLatitudeVariableName(variableName);
        assertEquals(variableName, config.getLatitudeVariableName());
    }

    @Test
    public void testSetGetAn_CI_name() {
        final String variableName = "fraction";

        config.setAn_CI_name(variableName);
        assertEquals(variableName, config.getAn_CI_name());
    }

    @Test
    public void testSetGetAn_SSTK_Name() {
        final String variableName = "sea_surface_temp";

        config.setAn_SSTK_name(variableName);
        assertEquals(variableName, config.getAn_SSTK_name());
    }

    @Test
    public void testSetGetFc_SSTK_name() {
        final String variableName = "sea_surface_temp";

        config.setFc_SSTK_name(variableName);
        assertEquals(variableName, config.getFc_SSTK_name());
    }

    @Test
    public void testSetGetAn_U10_name() {
        final String variableName = "eastern_wind";

        config.setAn_U10_name(variableName);
        assertEquals(variableName, config.getAn_U10_name());
    }

    @Test
    public void testSetGetFc_U10_name() {
        final String variableName = "storm";

        config.setFc_U10_name(variableName);
        assertEquals(variableName, config.getFc_U10_name());
    }

    @Test
    public void testSetGetAn_V10_name() {
        final String variableName = "cold";

        config.setAn_V10_name(variableName);
        assertEquals(variableName, config.getAn_V10_name());
    }

    @Test
    public void testSetGetFc_V10_Name() {
        final String variableName = "blow-wind-blow";

        config.setFc_V10_name(variableName);
        assertEquals(variableName, config.getFc_V10_name());
    }

    @Test
    public void testSetGetFc_MSL_Name() {
        final String variableName = "sea-level-pressure";

        config.setFc_MSL_name(variableName);
        assertEquals(variableName, config.getFc_MSL_name());
    }

    @Test
    public void testSetGetFc_T2_name() {
        final String variableName = "warm";

        config.setFc_T2_name(variableName);
        assertEquals(variableName, config.getFc_T2_name());
    }

    @Test
    public void testSetGetFc_D2_name() {
        final String variableName = "there";

        config.setFc_D2_name(variableName);
        assertEquals(variableName, config.getFc_D2_name());
    }

    @Test
    public void testSetGetFc_TP_name() {
        final String variableName = "prcipate";

        config.setFc_TP_name(variableName);
        assertEquals(variableName, config.getFc_TP_name());
    }

    @Test
    public void testSetGetAn_CLWC_name() {
        final String variableName = "wet";

        config.setAn_CLWC_name(variableName);
        assertEquals(variableName, config.getAn_CLWC_name());
    }

    @Test
    public void testSetGetFc_CLWC_name() {
        final String variableName = "cloud_water_thing";

        config.setFc_CLWC_name(variableName);
        assertEquals(variableName, config.getFc_CLWC_name());
    }

    @Test
    public void testSetGetAn_TCWV_name() {
        final String variableName = "foggy";

        config.setAn_TCWV_name(variableName);
        assertEquals(variableName, config.getAn_TCWV_name());
    }

    @Test
    public void testSetGetFc_TCWV_name() {
        final String variableName = "total_water_thing";

        config.setFc_TCWV_name(variableName);
        assertEquals(variableName, config.getFc_TCWV_name());
    }

    @Test
    public void testSetGetFc_SSHF_name() {
        final String variableName = "surface_sensible_heat_flux";

        config.setFc_SSHF_name(variableName);
        assertEquals(variableName, config.getFc_SSHF_name());
    }

    @Test
    public void testSetGetFc_SLHF_name() {
        final String variableName = "surface_latent_heat_flux";

        config.setFc_SLHF_name(variableName);
        assertEquals(variableName, config.getFc_SLHF_name());
    }

    @Test
    public void testSetGetFc_BLH_name() {
        final String variableName = "high_rise";

        config.setFc_BLH_name(variableName);
        assertEquals(variableName, config.getFc_BLH_name());
    }

    @Test
    public void testSetGetFc_SSRD_name() {
        final String variableName = "radia-tion";

        config.setFc_SSRD_name(variableName);
        assertEquals(variableName, config.getFc_SSRD_name());
    }

    @Test
    public void testSetGetFc_STRD_name() {
        final String variableName = "thermolation";

        config.setFc_STRD_name(variableName);
        assertEquals(variableName, config.getFc_STRD_name());
    }

    @Test
    public void testSetGetFc_SSR_name() {
        final String variableName = "radia-tion";

        config.setFc_SSR_name(variableName);
        assertEquals(variableName, config.getFc_SSR_name());
    }

    @Test
    public void testSetGetFc_STR_name() {
        final String variableName = "thermo-schlermo";

        config.setFc_STR_name(variableName);
        assertEquals(variableName, config.getFc_STR_name());
    }

    @Test
    public void testSetGetFc_EWSS_name() {
        final String variableName = "stresso";

        config.setFc_EWSS_name(variableName);
        assertEquals(variableName, config.getFc_EWSS_name());
    }

    @Test
    public void testSetGetFc_NSSS_name() {
        final String variableName = "northern_stress";

        config.setFc_NSSS_name(variableName);
        assertEquals(variableName, config.getFc_NSSS_name());
    }

    @Test
    public void testSetGetFc_E_name() {
        final String variableName = "vapoclean";

        config.setFc_E_name(variableName);
        assertEquals(variableName, config.getFc_E_name());
    }

    @Test
    public void testSetGetAnCenterTimeName() {
        final String timeVariableName = "center_minute";

        config.setAnCenterTimeName(timeVariableName);
        assertEquals(timeVariableName, config.getAnCenterTimeName());
    }

    @Test
    public void testSetGetFcCenterTimeName() {
        final String timeVariableName = "forecast_day";

        config.setFcCenterTimeName(timeVariableName);
        assertEquals(timeVariableName, config.getFcCenterTimeName());
    }

    @Test
    public void testDefaultValues() {
        assertEquals(17, config.getAnalysisSteps());
        assertEquals(33, config.getForecastSteps());
        assertNull(config.getTimeVariableName());
        assertNull(config.getLongitudeVariableName());
        assertNull(config.getLatitudeVariableName());

        assertEquals("matchup.nwp.an.sea_ice_fraction", config.getAn_CI_name());
        assertEquals("matchup.nwp.an.sea_surface_temperature", config.getAn_SSTK_name());
        assertEquals("matchup.nwp.fc.sea_surface_temperature", config.getFc_SSTK_name());
        assertEquals("matchup.nwp.an.10m_east_wind_component", config.getAn_U10_name());
        assertEquals("matchup.nwp.fc.10m_east_wind_component", config.getFc_U10_name());
        assertEquals("matchup.nwp.an.10m_north_wind_component", config.getAn_V10_name());
        assertEquals("matchup.nwp.fc.10m_north_wind_component", config.getFc_V10_name());
        assertEquals("matchup.nwp.fc.mean_sea_level_pressure", config.getFc_MSL_name());
        assertEquals("matchup.nwp.fc.2m_temperature", config.getFc_T2_name());
        assertEquals("matchup.nwp.fc.2m_dew_point", config.getFc_D2_name());
        assertEquals("matchup.nwp.fc.total_precipitation", config.getFc_TP_name());
        assertEquals("matchup.nwp.an.cloud_liquid_water_content", config.getAn_CLWC_name());
        assertEquals("matchup.nwp.fc.cloud_liquid_water_content", config.getFc_CLWC_name());
        assertEquals("matchup.nwp.an.total_column_water_vapour", config.getAn_TCWV_name());
        assertEquals("matchup.nwp.fc.total_column_water_vapour", config.getFc_TCWV_name());
        assertEquals("matchup.nwp.fc.surface_sensible_heat_flux", config.getFc_SSHF_name());
        assertEquals("matchup.nwp.fc.surface_latent_heat_flux", config.getFc_SLHF_name());
        assertEquals("matchup.nwp.fc.boundary_layer_height", config.getFc_BLH_name());
        assertEquals("matchup.nwp.fc.downward_surface_solar_radiation", config.getFc_SSRD_name());
        assertEquals("matchup.nwp.fc.downward_surface_thermal_radiation", config.getFc_STRD_name());
        assertEquals("matchup.nwp.fc.surface_solar_radiation", config.getFc_SSR_name());
        assertEquals("matchup.nwp.fc.surface_thermal_radiation", config.getFc_STR_name());
        assertEquals("matchup.nwp.fc.turbulent_stress_east_component", config.getFc_EWSS_name());
        assertEquals("matchup.nwp.fc.turbulent_stress_north_component", config.getFc_NSSS_name());
        assertEquals("matchup.nwp.fc.evaporation", config.getFc_E_name());

        assertEquals("matchup.nwp.an.t0", config.getAnCenterTimeName());
        assertEquals("matchup.nwp.fc.t0", config.getFcCenterTimeName());
    }
}
