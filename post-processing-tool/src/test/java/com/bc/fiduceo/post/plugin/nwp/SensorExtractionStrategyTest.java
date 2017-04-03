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

import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SensorExtractionStrategyTest {

    @Test
    public void testPrepare() {
        final SensorExtractionStrategy strategy = new SensorExtractionStrategy();

        final Variable variable = mock(Variable.class);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addVariable(anyObject(), anyString(), anyObject(), anyString())).thenReturn(variable);

        final Context context = new Context();
        final Configuration configuration = createConfiguration();
        context.setConfiguration(configuration);
        context.setTemplateVariables(new TemplateVariables(configuration));
        context.setWriter(writer);

        strategy.prepare(context);

        final SensorExtractConfiguration sensorExtractConfiguration = configuration.getSensorExtractConfiguration();

        verify(writer, times(1)).hasDimension(null, "amsre.nwp.nx");
        verify(writer, times(1)).addDimension(null, "amsre.nwp.nx", 9);

        verify(writer, times(1)).hasDimension(null, "amsre.nwp.ny");
        verify(writer, times(1)).addDimension(null, "amsre.nwp.ny", 10);

        verify(writer, times(1)).hasDimension(null, "amsre.nwp.nz");
        verify(writer, times(1)).addDimension(null, "amsre.nwp.nz", 11);

        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_CI_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_SSTK_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_U10_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_V10_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_MSL_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_T2_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_D2_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_TP_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_CLWC_name(), DataType.FLOAT, "matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_TCWV_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");

        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_ASN_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_CIWC_name(), DataType.FLOAT, "matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_TCC_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_AL_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_SKT_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_LNSP_name(), DataType.FLOAT, "matchup_count amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_T_name(), DataType.FLOAT, "matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_Q_name(), DataType.FLOAT, "matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx");
        verify(writer, times(1)).addVariable(null, sensorExtractConfiguration.getAn_O3_name(), DataType.FLOAT, "matchup_count amsre.nwp.nz amsre.nwp.ny amsre.nwp.nx");

        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testCalculateStride() {
        assertEquals(1, SensorExtractionStrategy.calculateStride(108, 0));
        assertEquals(1, SensorExtractionStrategy.calculateStride(108, 1));

        assertEquals(107, SensorExtractionStrategy.calculateStride(108, 2));
        assertEquals(53, SensorExtractionStrategy.calculateStride(108, 3));

        assertEquals(200, SensorExtractionStrategy.calculateStride(201, 2));
        assertEquals(149, SensorExtractionStrategy.calculateStride(300, 3));
    }

    @Test
    public void testCreateAnalysisProperties() {
        final Properties properties = SensorExtractionStrategy.createAnalysisFileTemplateProperties("cdo-home", "geo-file", "ggas-steps", "ggam_time",
                "spam_steps", "gafs_steps", "ggas_series", "ggam_series", "spam_serise", "gafs_series", "ggam_series_rem", "spam_series_rem",
                "gafs_series_rem", "nwp_file");

        assertNotNull(properties);
        assertEquals("cdo-home/cdo", properties.getProperty("CDO"));
        assertEquals("-M -R", properties.getProperty("CDO_OPTS"));
        assertEquals("1970-01-01,00:00:00,seconds", properties.getProperty("REFTIME"));
        assertEquals("geo-file", properties.getProperty("GEO"));

        assertEquals("ggas-steps", properties.getProperty("GGAS_TIMESTEPS"));
        assertEquals("ggam_time", properties.getProperty("GGAM_TIMESTEPS"));
        assertEquals("spam_steps", properties.getProperty("SPAM_TIMESTEPS"));
        assertEquals("gafs_steps", properties.getProperty("GAFS_TIMESTEPS"));
        assertEquals("ggas_series", properties.getProperty("GGAS_TIME_SERIES"));
        assertEquals("ggam_series", properties.getProperty("GGAM_TIME_SERIES"));
        assertEquals("spam_serise", properties.getProperty("SPAM_TIME_SERIES"));
        assertEquals("gafs_series", properties.getProperty("GAFS_TIME_SERIES"));
        assertEquals("ggam_series_rem", properties.getProperty("GGAM_TIME_SERIES_REMAPPED"));
        assertEquals("spam_series_rem", properties.getProperty("SPAM_TIME_SERIES_REMAPPED"));
        assertEquals("gafs_series_rem", properties.getProperty("GAFS_TIME_SERIES_REMAPPED"));
        assertEquals("nwp_file", properties.getProperty("NWP_TIME_SERIES"));
    }

    private Configuration createConfiguration() {
        final Configuration configuration = new Configuration();
        final SensorExtractConfiguration sensorExtractConfiguration = new SensorExtractConfiguration();
        sensorExtractConfiguration.setX_Dimension(9);
        sensorExtractConfiguration.setY_Dimension(10);
        sensorExtractConfiguration.setZ_Dimension(11);
        configuration.setSensorExtractConfiguration(sensorExtractConfiguration);
        return configuration;
    }
}
