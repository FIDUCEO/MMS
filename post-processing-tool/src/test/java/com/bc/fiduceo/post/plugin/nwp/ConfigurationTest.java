/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

    private Configuration config;

    @Before
    public void setUp() {
        config = new Configuration();
    }

    @Test
    public void testSetGetDeleteOnExit() {
        config.setDeleteOnExit(false);
        assertFalse(config.isDeleteOnExit());

        config.setDeleteOnExit(true);
        assertTrue(config.isDeleteOnExit());
    }

    @Test
    public void testSetGetCDOHome() {
        final String cdoHome = "/here/is/it";

        config.setCDOHome(cdoHome);
        assertEquals(cdoHome, config.getCDOHome());
    }

    @Test
    public void testSetGetNWPAuxDir() {
        final String nwpAuxDir = "/here/are/the/files";

        config.setNWPAuxDir(nwpAuxDir);
        assertEquals(nwpAuxDir, config.getNWPAuxDir());
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
    public void testSetGetAnTotalColumnWaterVapourName() {
        final String variableName = "foggy";

        config.setAnTotalColumnWaterVapourName(variableName);
        assertEquals(variableName, config.getAnTotalColumnWaterVapourName());
    }

    @Test
    public void testSetGetAnCloudLiquidWaterContentName() {
        final String variableName = "wet";

        config.setAnCloudLiquidWaterContentName(variableName);
        assertEquals(variableName, config.getAnCloudLiquidWaterContentName());
    }

    @Test
    public void testSetGetFcSurfSensibleHeatFluxName() {
        final String variableName = "surface_sensible_heat_flux";

        config.setFcSurfSensibleHeatFluxName(variableName);
        assertEquals(variableName, config.getFcSurfSensibleHeatFluxName());
    }

    @Test
    public void testSetGetFcSurfLatentHeatFluxName() {
        final String variableName = "surface_latent_heat_flux";

        config.setFcSurfLatentHeatFluxName(variableName);
        assertEquals(variableName, config.getFcSurfLatentHeatFluxName());
    }

    @Test
    public void testSetGetFcBoundaryLayerHeightName() {
        final String variableName = "high_rise";

        config.setFcBoundaryLayerHeightName(variableName);
        assertEquals(variableName, config.getFcBoundaryLayerHeightName());
    }

    @Test
    public void testSetGetFc2mTemperatureName() {
        final String variableName = "warm";

        config.setFc2mTemperatureName(variableName);
        assertEquals(variableName, config.getFc2mTemperatureName());
    }

    @Test
    public void testSetGetFc2mDewPointName() {
        final String variableName = "there";

        config.setFc2mDewPointName(variableName);
        assertEquals(variableName, config.getFc2mDewPointName());
    }

    @Test
    public void testSetGetFcDownSurfSolarRadiationName() {
        final String variableName = "radia-tion";

        config.setFcDownSurfSolarRadiationName(variableName);
        assertEquals(variableName, config.getFcDownSurfSolarRadiationName());
    }

    @Test
    public void testSetGetFcDownSurfThermalRadiationName() {
        final String variableName = "thermolation";

        config.setFcDownSurfThermalRadiationName(variableName);
        assertEquals(variableName, config.getFcDownSurfThermalRadiationName());
    }

    @Test
    public void testSetGetFcSurfSolarRadiationName() {
        final String variableName = "radia-tion";

        config.setFcSurfSolarRadiationName(variableName);
        assertEquals(variableName, config.getFcSurfSolarRadiationName());
    }

    @Test
    public void testSetGetFcSurfThermalRadiationName() {
        final String variableName = "thermo-schlermo";

        config.setFcSurfThermalRadiationName(variableName);
        assertEquals(variableName, config.getFcSurfThermalRadiationName());
    }

    @Test
    public void testSetGetFcTurbStressEastName() {
        final String variableName = "stresso";

        config.setFcTurbStressEastName(variableName);
        assertEquals(variableName, config.getFcTurbStressEastName());
    }

    @Test
    public void testSetGetFcTurbStressNorthName() {
        final String variableName = "northern_stress";

        config.setFcTurbStressNorthName(variableName);
        assertEquals(variableName, config.getFcTurbStressNorthName());
    }

    @Test
    public void testSetGetFcEvaporationName() {
        final String variableName = "vapoclean";

        config.setFcEvaporationName(variableName);
        assertEquals(variableName, config.getFcEvaporationName());
    }

    @Test
    public void testSetGetFcTotalPrecipName() {
        final String variableName = "prcipate";

        config.setFcTotalPrecipName(variableName);
        assertEquals(variableName, config.getFcTotalPrecipName());
    }

    @Test
    public void testSetGetFcTotalColumnWaterVapourName() {
        final String variableName = "total_water_thing";

        config.setFcTotalColumnWaterVapourName(variableName);
        assertEquals(variableName, config.getFcTotalColumnWaterVapourName());
    }

    @Test
    public void testSetGetFcCloudLiquidWaterContentName() {
        final String variableName = "cloud_water_thing";

        config.setFcCloudLiquidWaterContentName(variableName);
        assertEquals(variableName, config.getFcCloudLiquidWaterContentName());
    }

    @Test
    public void testIsTimeSeriesExtraction() {
        config.setTimeSeriesConfiguration(new TimeSeriesConfiguration());
        assertTrue(config.isTimeSeriesExtraction());

        config.setTimeSeriesConfiguration(null);
        assertFalse(config.isTimeSeriesExtraction());
    }

    @Test
    public void testSetGetTimeSeriesConfiguration() {
        final TimeSeriesConfiguration timeSeriesConfiguration = new TimeSeriesConfiguration();

        config.setTimeSeriesConfiguration(timeSeriesConfiguration);
        assertNotNull(config.getTimeSeriesConfiguration());
    }

    @Test
    public void testDefaultValues() {
        assertTrue(config.isDeleteOnExit());
        assertNull(config.getCDOHome());
        assertNull(config.getNWPAuxDir());

        assertFalse(config.isTimeSeriesExtraction());

        assertEquals("matchup.nwp.an.t0", config.getAnCenterTimeName());
        assertEquals("matchup.nwp.fc.t0", config.getFcCenterTimeName());

        assertEquals("matchup.nwp.an.total_column_water_vapour", config.getAnTotalColumnWaterVapourName());
        assertEquals("matchup.nwp.an.cloud_liquid_water_content", config.getAnCloudLiquidWaterContentName());

        assertEquals("matchup.nwp.fc.surface_sensible_heat_flux", config.getFcSurfSensibleHeatFluxName());
        assertEquals("matchup.nwp.fc.surface_latent_heat_flux", config.getFcSurfLatentHeatFluxName());
        assertEquals("matchup.nwp.fc.boundary_layer_height", config.getFcBoundaryLayerHeightName());
        assertEquals("matchup.nwp.fc.2m_temperature", config.getFc2mTemperatureName());
        assertEquals("matchup.nwp.fc.2m_dew_point", config.getFc2mDewPointName());
        assertEquals("matchup.nwp.fc.downward_surface_solar_radiation", config.getFcDownSurfSolarRadiationName());
        assertEquals("matchup.nwp.fc.downward_surface_thermal_radiation", config.getFcDownSurfThermalRadiationName());
        assertEquals("matchup.nwp.fc.surface_solar_radiation", config.getFcSurfSolarRadiationName());
        assertEquals("matchup.nwp.fc.surface_thermal_radiation", config.getFcSurfThermalRadiationName());
        assertEquals("matchup.nwp.fc.turbulent_stress_east_component", config.getFcTurbStressEastName());
        assertEquals("matchup.nwp.fc.turbulent_stress_north_component", config.getFcTurbStressNorthName());
        assertEquals("matchup.nwp.fc.evaporation", config.getFcEvaporationName());
        assertEquals("matchup.nwp.fc.total_precipitation", config.getFcTotalPrecipName());
        assertEquals("matchup.nwp.fc.total_column_water_vapour", config.getFcTotalColumnWaterVapourName());
        assertEquals("matchup.nwp.fc.cloud_liquid_water_content", config.getFcCloudLiquidWaterContentName());
    }
}
