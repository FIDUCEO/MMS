package com.bc.fiduceo.post.plugin.nwp;


import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

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
}
