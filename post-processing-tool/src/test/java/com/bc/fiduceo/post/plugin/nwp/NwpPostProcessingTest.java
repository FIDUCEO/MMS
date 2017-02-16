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
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NwpPostProcessingTest {

    @Test
    public void testExtractTimeRange() {
        final int[] times = {100000000, 110000000, 120000000, 120000000, 130000000, 140000000, 110000000, 150000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = NwpPostProcessing.extractTimeRange(timesArray, 12);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1973, 3, 3, 9, 46, 40, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1974, 10, 3, 2, 40, 0, timeRange.getStopDate());
    }

    @Test
    public void testExtractTimeRange_withFillValue() {
        final int[] times = {200000000, 210000000, -32768, 220000000, 230000000, 240000000, -32768, 210000000, 250000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = NwpPostProcessing.extractTimeRange(timesArray, -32768);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1976, 5, 3, 19, 33, 20, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1977, 12, 3, 12, 26, 40, timeRange.getStopDate());
    }

    @Test
    public void testToDirectoryNameList() {
        final Date startDate = TimeUtils.parse("2007-04-01", "yyyy-MM-dd");
        final Date endDate = TimeUtils.parse("2007-04-12", "yyyy-MM-dd");
        final TimeRange timeRange = new TimeRange(startDate, endDate);

        final List<String> directoryNames = NwpPostProcessing.toDirectoryNamesList(timeRange);
        assertEquals(17, directoryNames.size());
        assertEquals("2007/03/29", directoryNames.get(0));
        assertEquals("2007/04/07", directoryNames.get(9));
        assertEquals("2007/04/14", directoryNames.get(16));
    }

    @Test
    public void testCreateAnalysisFileTemplateProperties() {
         final Properties analysisTemplateProperties = NwpPostProcessing.createAnalysisFileTemplateProperties("/home/tome/CiDiOh",
                 "/home/tom/geo_file",
                 "/home/tom/time/steps",
                 "/home/tom/time-series",
                 "/usr/data/analysis-file");

         assertEquals("/home/tome/CiDiOh/cdo", analysisTemplateProperties.getProperty("CDO"));
         assertEquals("-M -R", analysisTemplateProperties.getProperty("CDO_OPTS"));
         assertEquals("1970-01-01,00:00:00,seconds", analysisTemplateProperties.getProperty("REFTIME"));
         assertEquals("/home/tom/geo_file", analysisTemplateProperties.getProperty("GEO"));
         assertEquals("/home/tom/time/steps", analysisTemplateProperties.getProperty("GGAS_TIMESTEPS"));
         assertEquals("/home/tom/time-series", analysisTemplateProperties.getProperty("GGAS_TIME_SERIES"));
         assertEquals("/usr/data/analysis-file", analysisTemplateProperties.getProperty("AN_TIME_SERIES"));
    }

    @Test
    public void testCreateForecastFileTemplateProperties() {
        final Properties fcTemplateProperties = NwpPostProcessing.createForecastFileTemplateProperties("/path/to/cdo/bin",
                "/path/to/geo.file",
                "/path/to/time.steps",
                "/path/to/ggfs.file",
                "/path/to/gafs_series",
                "/path/to/ggfs_series",
                "/path/to/ggfs_series_remap",
                "/path/to/forecast_file");

        assertEquals("/path/to/cdo/bin/cdo", fcTemplateProperties.getProperty("CDO"));
        assertEquals("-M -R", fcTemplateProperties.getProperty("CDO_OPTS"));
        assertEquals("1970-01-01,00:00:00,seconds", fcTemplateProperties.getProperty("REFTIME"));
        assertEquals("/path/to/geo.file", fcTemplateProperties.getProperty("GEO"));
        assertEquals("/path/to/time.steps", fcTemplateProperties.getProperty("GAFS_TIMESTEPS"));
        assertEquals("/path/to/ggfs.file", fcTemplateProperties.getProperty("GGFS_TIMESTEPS"));
        assertEquals("/path/to/gafs_series", fcTemplateProperties.getProperty("GAFS_TIME_SERIES"));
        assertEquals("/path/to/ggfs_series", fcTemplateProperties.getProperty("GGFS_TIME_SERIES"));
        assertEquals("/path/to/ggfs_series_remap", fcTemplateProperties.getProperty("GGFS_TIME_SERIES_REMAPPED"));
        assertEquals("/path/to/forecast_file", fcTemplateProperties.getProperty("FC_TIME_SERIES"));
    }

    @Test
    public void testPrepare() throws IOException, InvalidRangeException {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension matchupCountDimension = new Dimension("matchup_count", 7);

        when(netcdfFile.findDimension("matchup_count")).thenReturn(matchupCountDimension);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);

        final Configuration configuration = createConfiguration();
        final NwpPostProcessing postProcessing = new NwpPostProcessing(configuration);
        postProcessing.prepare(netcdfFile, writer);

        verify(writer, times(1)).addDimension(null, "matchup.nwp.an.time", 13);
        verify(writer, times(1)).addDimension(null, "matchup.nwp.fc.time", 14);

        verify(writer, times(1)).addVariable(null, "matchup.nwp.an.t0", DataType.INT, "matchup_count");
        verify(writer, times(1)).addVariable(null, "matchup.nwp.fc.t0", DataType.INT, "matchup_count");

        verify(writer, times(1)).addVariable(null, configuration.getAnSeaIceFractionName(), DataType.FLOAT, "matchup_count matchup.nwp.an.time");
    }

    private Configuration createConfiguration() {
        final Configuration configuration = new Configuration();

        configuration.setAnalysisSteps(13);
        configuration.setForecastSteps(14);
        return configuration;
    }
}
