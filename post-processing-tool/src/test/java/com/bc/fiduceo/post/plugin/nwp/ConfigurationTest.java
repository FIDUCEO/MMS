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
import ucar.nc2.NetcdfFile;

import static org.junit.Assert.*;

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
    public void testSetGetAnSeaIceFractionName() {
        final String variableName = "fraction";

        config.setAnSeaIceFractionName(variableName);
        assertEquals(variableName, config.getAnSeaIceFractionName());
    }

    @Test
    public void testSetGetAnSSTName() {
        final String variableName = "sea_surface_temp";

        config.setAnSSTName(variableName);
        assertEquals(variableName, config.getAnSSTName());
    }

    @Test
    public void testSetGetAnEastWind() {
        final String variableName = "eastern_wind";

        config.setAnEastWindName(variableName);
        assertEquals(variableName, config.getAnEastWindName());
    }

    @Test
    public void testSetGetAnNorthWind() {
        final String variableName = "cold";

        config.setAnNorthWindName(variableName);
        assertEquals(variableName, config.getAnNorthWindName());
    }

    @Test
    public void testSetGetFcSSTName() {
        final String variableName = "sea_surface_temp";

        config.setFcSSTName(variableName);
        assertEquals(variableName, config.getFcSSTName());
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
    public void testSetGetFcMeanSeaLevelPressureName() {
        final String variableName = "sea-level-pressure";

        config.setFcMeanSeaLevelPressureName(variableName);
        assertEquals(variableName, config.getFcMeanSeaLevelPressureName());
    }

    @Test
    public void testSetGetFcBoundaryLayerHeightName() {
        final String variableName = "high_rise";

        config.setFcBoundaryLayerHeightName(variableName);
        assertEquals(variableName, config.getFcBoundaryLayerHeightName());
    }

    @Test
    public void testSetGetFc10mEastWindName() {
        final String variableName = "storm";

        config.setFc10mEastWindName(variableName);
        assertEquals(variableName, config.getFc10mEastWindName());
    }

    @Test
    public void testSetGetFc10mNorthWindName() {
        final String variableName = "blow-wind-blow";

        config.setFc10mNorthWindName(variableName);
        assertEquals(variableName, config.getFc10mNorthWindName());
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
    public void testDefaultValues() {
        assertTrue(config.isDeleteOnExit());
        assertNull(config.getCDOHome());
        assertEquals(17, config.getAnalysisSteps());
        assertEquals(33, config.getForecastSteps());
        assertNull(config.getNWPAuxDir());
        assertNull(config.getTimeVariableName());

        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_ice_fraction"), config.getAnSeaIceFractionName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.an.sea_surface_temperature"), config.getAnSSTName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_east_wind_component"), config.getAnEastWindName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.an.10m_north_wind_component"), config.getAnNorthWindName());

        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.sea_surface_temperature"), config.getFcSSTName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_sensible_heat_flux"), config.getFcSurfSensibleHeatFluxName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_latent_heat_flux"), config.getFcSurfLatentHeatFluxName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.mean_sea_level_pressure"), config.getFcMeanSeaLevelPressureName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.boundary_layer_height"), config.getFcBoundaryLayerHeightName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_east_wind_component"), config.getFc10mEastWindName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.10m_north_wind_component"), config.getFc10mNorthWindName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_temperature"), config.getFc2mTemperatureName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.2m_dew_point"), config.getFc2mDewPointName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_solar_radiation"), config.getFcDownSurfSolarRadiationName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.downward_surface_thermal_radiation"), config.getFcDownSurfThermalRadiationName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_solar_radiation"), config.getFcSurfSolarRadiationName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.surface_thermal_radiation"), config.getFcSurfThermalRadiationName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_east_component"), config.getFcTurbStressEastName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.turbulent_stress_north_component"), config.getFcTurbStressNorthName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.evaporation"), config.getFcEvaporationName());
        assertEquals(NetcdfFile.makeValidCDLName("matchup.nwp.fc.total_precipitation"), config.getFcTotalPrecipName());
    }
}
