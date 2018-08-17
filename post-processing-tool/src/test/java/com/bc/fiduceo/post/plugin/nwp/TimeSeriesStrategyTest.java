package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import org.mockito.InOrder;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.util.Properties;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        //preparation
        final NetcdfFile netcdfFile = mock(NetcdfFile.class);
        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        final Configuration configuration = createConfiguration();
        final Context context = new Context();
        context.setConfiguration(configuration);
        context.setReader(netcdfFile);
        context.setWriter(writer);
        context.setTemplateVariables(new TemplateVariables(configuration));
        final TimeSeriesConfiguration timeSeriesConfiguration = configuration.getTimeSeriesConfiguration();

        final Dimension matchupCountDimension = new Dimension(com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT, 7);
        when(netcdfFile.findDimension(com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT)).thenReturn(matchupCountDimension);

        final Variable variable = mock(Variable.class);
        when(writer.addVariable(any(), anyString(), any(), anyString())).thenReturn(variable);

        final String varNameFcCenterTime = timeSeriesConfiguration.getFcCenterTimeName();
        final Variable varFcCenterTime = mock(Variable.class);
        when(varFcCenterTime.getDataType()).thenReturn(DataType.INT);
        when(writer.addVariable(any(), eq(varNameFcCenterTime), any(), anyString())).thenReturn(varFcCenterTime);

        //execution
        final TimeSeriesStrategy timeSeriesStrategy = new TimeSeriesStrategy();
        timeSeriesStrategy.prepare(context);

        //verification
        verify(writer, times(1)).hasDimension(null, "matchup.nwp.an.time");
        verify(writer, times(1)).hasDimension(null, "matchup.nwp.fc.time");

        verify(writer, times(1)).addDimension(null, "matchup.nwp.an.time", 13);
        verify(writer, times(1)).addDimension(null, "matchup.nwp.fc.time", 14);

        verify(writer, times(1)).addVariable(null, "matchup.nwp.an.t0", DataType.INT, com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT);

        final InOrder inOrder = inOrder(writer, varFcCenterTime);
        inOrder.verify(writer, times(1)).addVariable(null, varNameFcCenterTime, DataType.INT, com.bc.fiduceo.post.Constants.DIMENSION_NAME_MATCHUP_COUNT);
        inOrder.verify(varFcCenterTime, times(1)).findAttribute(CF_FILL_VALUE_NAME);
        inOrder.verify(varFcCenterTime, times(1)).getDataType();
        inOrder.verify(varFcCenterTime, times(1)).addAttribute(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        verifyNoMoreInteractions(varFcCenterTime);

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
