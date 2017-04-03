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
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TemplateVariablesTest {

    private TemplateVariables templateVariables;

    @Before
    public void setUp() {
        final Configuration configuration = new Configuration();
        configuration.setTimeSeriesConfiguration(new TimeSeriesConfiguration());
        configuration.setSensorExtractConfiguration(new SensorExtractConfiguration());

        templateVariables = new TemplateVariables(configuration);
    }

    @Test
    public void testGetTimeSeriesAnalysisVariables() {
        final List<TemplateVariable> analysisVars = templateVariables.getTimeSeriesAnalysisVariables();
        assertEquals(6, analysisVars.size());

        TemplateVariable variable = analysisVars.get(2);
        assertEquals("matchup.nwp.an.10m_east_wind_component", variable.getName());
        assertEquals("U10", variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.an.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        final List<Attribute> attributes = variable.getAttributes();
        assertEquals(5, attributes.size());

        variable = analysisVars.get(4);
        assertEquals("matchup.nwp.an.total_column_water_vapour", variable.getName());
        assertEquals("TCWV", variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.an.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = analysisVars.get(5);
        assertEquals("matchup.nwp.an.cloud_liquid_water_content", variable.getName());
        assertEquals(Constants.CLWC_NAME, variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.an.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetTimeSeriesAnalysisVariables_noTimeSeriesConfig() {
        final TemplateVariables templateVariables = new TemplateVariables(new Configuration());

        final List<TemplateVariable> timeSeriesAnalysisVariables = templateVariables.getTimeSeriesAnalysisVariables();
        assertEquals(0, timeSeriesAnalysisVariables.size());
    }

    @Test
    public void testGetTimeSeriesForecastVariables() {
        final List<TemplateVariable> forecastVariables = templateVariables.getTimeSeriesForecastVariables();
        assertEquals(19, forecastVariables.size());

        TemplateVariable variable = forecastVariables.get(5);
        assertEquals("matchup.nwp.fc.surface_solar_radiation", variable.getName());
        assertEquals("SSR", variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.fc.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        List<Attribute> attributes = variable.getAttributes();
        assertEquals(5, attributes.size());

        variable = forecastVariables.get(13);
        assertEquals("matchup.nwp.fc.2m_temperature", variable.getName());
        assertEquals("T2", variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.fc.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        attributes = variable.getAttributes();
        assertEquals(5, attributes.size());

        variable = forecastVariables.get(18);
        assertEquals("matchup.nwp.fc.cloud_liquid_water_content", variable.getName());
        assertEquals(Constants.CLWC_NAME, variable.getOriginalName());
        assertEquals("matchup_count matchup.nwp.fc.time", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }

    @Test
    public void testGetTimeSeriesForecastVariables_noTimeSeriesConfig() {
        final TemplateVariables templateVariables = new TemplateVariables(new Configuration());

        final List<TemplateVariable> timeSeriesAnalysisVariables = templateVariables.getTimeSeriesForecastVariables();
        assertEquals(0, timeSeriesAnalysisVariables.size());
    }

    @Test
    public void testGetAllTimeSeriesVariables() {
        final List<TemplateVariable> allVariables = templateVariables.getAllTimeSeriesVariables();
        assertEquals(25, allVariables.size());
    }

    @Test
    public void testGetAllTimeSeriesVariables_noTimeSeriesConfig() {
        final TemplateVariables templateVariables = new TemplateVariables(new Configuration());

        final List<TemplateVariable> timeSeriesAnalysisVariables = templateVariables.getAllTimeSeriesVariables();
        assertEquals(0, timeSeriesAnalysisVariables.size());
    }

    @Test
    public void testGetSensorExtractVariables() {
        final List<TemplateVariable> sensorExtractVariables = templateVariables.getSensorExtractVariables();
        assertEquals(19, sensorExtractVariables.size());

        TemplateVariable variable = sensorExtractVariables.get(2);
        assertEquals("amsre.nwp.10m_east_wind_component", variable.getName());
        assertEquals("U10", variable.getOriginalName());
        assertEquals("matchup_count amsre.nwp.ny amsre.nwp.nx", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = sensorExtractVariables.get(8);
        assertEquals("amsre.nwp.cloud_liquid_water", variable.getName());
        assertEquals("var246", variable.getOriginalName());
        assertEquals("matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());

        variable = sensorExtractVariables.get(10);
        assertEquals("amsre.nwp.snow_albedo", variable.getName());
        assertEquals("ASN", variable.getOriginalName());
        assertEquals("matchup_count amsre.nwp.ny amsre.nwp.nx", variable.getDimensions());
        assertEquals(DataType.FLOAT, variable.getDataType());
    }


    @Test
    public void testGetSensorExtractVariables_noSensorExtractConfig() {
        final TemplateVariables templateVariables = new TemplateVariables(new Configuration());

        final List<TemplateVariable> sensorExtractVariables = templateVariables.getSensorExtractVariables();
        assertEquals(0, sensorExtractVariables.size());
    }
}


