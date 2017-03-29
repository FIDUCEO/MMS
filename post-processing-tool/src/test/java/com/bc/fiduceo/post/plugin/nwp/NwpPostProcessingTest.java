/*
 * Copyright (C) 2017 Brockmann Consult GmbH
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


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.post.Constants;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class NwpPostProcessingTest {



    @Test
    public void testPrepare() throws IOException, InvalidRangeException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension matchupCountDimension = new Dimension(Constants.MATCHUP_COUNT, 7);

        when(netcdfFile.findDimension(Constants.MATCHUP_COUNT)).thenReturn(matchupCountDimension);

        final Variable variable = mock(Variable.class);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addVariable(anyObject(), anyString(), anyObject(), anyString())).thenReturn(variable);

        final Configuration configuration = createConfiguration();
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();
        final NwpPostProcessing postProcessing = new NwpPostProcessing(configuration);
        postProcessing.prepare(netcdfFile, writer);

        verify(writer, times(1)).hasDimension(null, "matchup.nwp.an.time");
        verify(writer, times(1)).hasDimension(null, "matchup.nwp.fc.time");

        verify(writer, times(1)).addDimension(null, "matchup.nwp.an.time", 13);
        verify(writer, times(1)).addDimension(null, "matchup.nwp.fc.time", 14);

        verify(writer, times(1)).addVariable(null, "matchup.nwp.an.t0", DataType.INT, Constants.MATCHUP_COUNT);
        verify(writer, times(1)).addVariable(null, "matchup.nwp.fc.t0", DataType.INT, Constants.MATCHUP_COUNT);

        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_CI_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_U10_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_V10_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_SSTK_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");

        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_E_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_SSTK_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_TP_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_T2_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_BLH_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_SSR_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_STR_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_EWSS_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_NSSS_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_D2_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_U10_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_V10_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_SSRD_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_STRD_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_MSL_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_SLHF_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_SSHF_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_CLWC_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_CLWC_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getAn_TCWV_name(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
        verify(writer, times(1)).addVariable(null, timeSeriesConfiguration.getFc_TCWV_name(), DataType.FLOAT, "matchup_count matchup.nwp.fc.time");

        verifyNoMoreInteractions(writer);
    }

    private Configuration createConfiguration() {
        final Configuration configuration = new Configuration();
        final TimeSeriesConfiguration timeSeriesConfiguration = new TimeSeriesConfiguration();

        timeSeriesConfiguration.setAnalysisSteps(13);
        timeSeriesConfiguration.setForecastSteps(14);

        configuration.setTimeSeriesConfiguration(timeSeriesConfiguration);

        return configuration;
    }
}
