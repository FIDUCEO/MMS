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

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NwpPostProcessingTest {

    @Test
    public void testExtractTimeRange() {
        final int[] times = {100000000, 110000000, 120000000, 120000000, 130000000, 140000000, 110000000, 150000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = NwpPostProcessing.extractTimeRange(timesArray, 12);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1970, 1, 2, 3, 46, 40, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1970, 1, 2, 17, 40, 0, timeRange.getStopDate());
    }

    @Test
    public void testExtractTimeRange_withFillValue() {
        final int[] times = {200000000, 210000000, -32768, 220000000, 230000000, 240000000, -32768, 210000000, 250000000};
        final Array timesArray = Array.factory(times);

        final TimeRange timeRange = NwpPostProcessing.extractTimeRange(timesArray, -32768);
        assertNotNull(timeRange);

        TestUtil.assertCorrectUTCDate(1970, 1, 3, 7, 33, 20, timeRange.getStartDate());
        TestUtil.assertCorrectUTCDate(1970, 1, 3, 21, 26, 40, timeRange.getStopDate());
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
}
