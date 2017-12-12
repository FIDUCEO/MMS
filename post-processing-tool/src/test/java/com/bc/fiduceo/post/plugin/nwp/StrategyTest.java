package com.bc.fiduceo.post.plugin.nwp;


import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StrategyTest {

    @Test
    public void testToDirectoryNameList() {
        final Date startDate = TimeUtils.parse("2007-04-01", "yyyy-MM-dd");
        final Date endDate = TimeUtils.parse("2007-04-12", "yyyy-MM-dd");
        final TimeRange timeRange = new TimeRange(startDate, endDate);

        final List<String> directoryNames = Strategy.toDirectoryNamesList(timeRange);
        assertEquals(17, directoryNames.size());
        assertEquals("2007/03/29", directoryNames.get(0));
        assertEquals("2007/04/07", directoryNames.get(9));
        assertEquals("2007/04/14", directoryNames.get(16));
    }

    @Test
    public void testExtractTimeRange() {
        final int[] times = {100000000, 110000000, 120000000, 120000000, 130000000, 140000000, 110000000, 150000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = Strategy.extractTimeRange(timesArray, 12);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1973, 3, 3, 9, 46, 40, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1974, 10, 3, 2, 40, 0, timeRange.getStopDate());
    }

    @Test
    public void testExtractTimeRange_withFillValue() {
        final int[] times = {200000000, 210000000, -32768, 220000000, 230000000, 240000000, -32768, 210000000, 250000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = Strategy.extractTimeRange(timesArray, -32768);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1976, 5, 3, 19, 33, 20, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1977, 12, 3, 12, 26, 40, timeRange.getStopDate());
    }

    @Test
    public void testExtractTimeRange_3D_dataset_fillValue() {
        final int[] times = {-2147483647, 1362719282, 1362719282, -2147483647, 1362719289, 1362719289, -2147483647, 1362719295, 1362719295,
                -2147483647, 1362719289, 1362719289, -2147483647, 1362719295, 1362719295, -2147483647, 1362719301, 1362719301
        };
        final Array timesArray = Array.factory(DataType.INT, new int[]{2, 3, 3}, times);

        final TimeRange timeRange = Strategy.extractTimeRange(timesArray, -2147483647);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(2013, 3, 8, 5, 8, 2, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(2013, 3, 8, 5, 8, 21, timeRange.getStopDate());
    }

    @Test
    public void testCreateBaseTemplateProperties() {
        final Properties properties = Strategy.createBaseTemplateProperties("CDO-HOME", "geo_file_loc");
        assertNotNull(properties);

        assertEquals("CDO-HOME/cdo", properties.getProperty("CDO"));
        assertEquals("-M -R", properties.getProperty("CDO_OPTS"));
        assertEquals("1970-01-01,00:00:00,seconds", properties.getProperty("REFTIME"));
        assertEquals("geo_file_loc", properties.getProperty("GEO"));
    }
}
