package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Test;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TimeSeriesStrategyTest {

    @Test
    public void testCreateAnalysisFileTemplateProperties() {
        final Properties analysisTemplateProperties = TimeSeriesStrategy.createAnalysisFileTemplateProperties("/home/tome/CiDiOh",
                "/home/tom/geo_file",
                "/home/tom/time/steps",
                "/home/tom/time-series",
                "/home/tom/time/gam_steps",
                "/home/tom/gam_time-series",
                "/home/tom/gam_remapped",
                "/usr/data/analysis-file");

        assertEquals("/home/tome/CiDiOh/cdo", analysisTemplateProperties.getProperty("CDO"));
        assertEquals("-M -R", analysisTemplateProperties.getProperty("CDO_OPTS"));
        assertEquals("1970-01-01,00:00:00,seconds", analysisTemplateProperties.getProperty("REFTIME"));
        assertEquals("/home/tom/geo_file", analysisTemplateProperties.getProperty("GEO"));
        assertEquals("/home/tom/time/steps", analysisTemplateProperties.getProperty("GGAS_TIMESTEPS"));
        assertEquals("/home/tom/time-series", analysisTemplateProperties.getProperty("GGAS_TIME_SERIES"));
        assertEquals("/home/tom/time/gam_steps", analysisTemplateProperties.getProperty("GGAM_TIMESTEPS"));
        assertEquals("/home/tom/gam_time-series", analysisTemplateProperties.getProperty("GGAM_TIME_SERIES"));
        assertEquals("/home/tom/gam_remapped", analysisTemplateProperties.getProperty("GGAM_TIME_SERIES_REMAPPED"));
        assertEquals("/usr/data/analysis-file", analysisTemplateProperties.getProperty("AN_TIME_SERIES"));
    }

    @Test
    public void testCreateForecastFileTemplateProperties() {
        final Properties fcTemplateProperties = TimeSeriesStrategy.createForecastFileTemplateProperties("/path/to/cdo/bin",
                "/path/to/geo.file",
                "/path/to/time.steps",
                "/path/to/ggfs.file",
                "/path/to/ggfm.file",
                "/path/to/gafs_series",
                "/path/to/ggfs_series",
                "/path/to/ggfs_series_remap",
                "/path/to/ggfm_series",
                "/path/to/ggfm_series_remap",
                "/path/to/forecast_file");

        assertEquals("/path/to/cdo/bin/cdo", fcTemplateProperties.getProperty("CDO"));
        assertEquals("-M -R", fcTemplateProperties.getProperty("CDO_OPTS"));
        assertEquals("1970-01-01,00:00:00,seconds", fcTemplateProperties.getProperty("REFTIME"));
        assertEquals("/path/to/geo.file", fcTemplateProperties.getProperty("GEO"));
        assertEquals("/path/to/time.steps", fcTemplateProperties.getProperty("GAFS_TIMESTEPS"));
        assertEquals("/path/to/ggfs.file", fcTemplateProperties.getProperty("GGFS_TIMESTEPS"));
        assertEquals("/path/to/ggfm.file", fcTemplateProperties.getProperty("GGFM_TIMESTEPS"));
        assertEquals("/path/to/gafs_series", fcTemplateProperties.getProperty("GAFS_TIME_SERIES"));
        assertEquals("/path/to/ggfs_series", fcTemplateProperties.getProperty("GGFS_TIME_SERIES"));
        assertEquals("/path/to/ggfs_series_remap", fcTemplateProperties.getProperty("GGFS_TIME_SERIES_REMAPPED"));
        assertEquals("/path/to/ggfm_series", fcTemplateProperties.getProperty("GGFM_TIME_SERIES"));
        assertEquals("/path/to/ggfm_series_remap", fcTemplateProperties.getProperty("GGFM_TIME_SERIES_REMAPPED"));
        assertEquals("/path/to/forecast_file", fcTemplateProperties.getProperty("FC_TIME_SERIES"));
    }

    @Test
    public void testPrepare()  {
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final Dimension matchupCountDimension = new Dimension(com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT, 7);

        when(netcdfFile.findDimension(com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT)).thenReturn(matchupCountDimension);

        final Variable variable = mock(Variable.class);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addVariable(any(), anyString(), any(), anyString())).thenReturn(variable);

        final Configuration configuration = createConfiguration();
        final Context context = new Context();
        context.setConfiguration(configuration);
        context.setReader(netcdfFile);
        context.setWriter(writer);
        context.setTemplateVariables(new TemplateVariables(configuration));

        final TimeSeriesStrategy timeSeriesStrategy = new TimeSeriesStrategy();
        timeSeriesStrategy.prepare(context);

        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        verify(writer, times(1)).hasDimension(null, "matchup.nwp.an.time");
        verify(writer, times(1)).hasDimension(null, "matchup.nwp.fc.time");

        verify(writer, times(1)).addDimension(null, "matchup.nwp.an.time", 13);
        verify(writer, times(1)).addDimension(null, "matchup.nwp.fc.time", 14);

        verify(writer, times(1)).addVariable(null, "matchup.nwp.an.t0", DataType.INT, com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT);
        verify(writer, times(1)).addVariable(null, "matchup.nwp.fc.t0", DataType.INT, com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT);

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
