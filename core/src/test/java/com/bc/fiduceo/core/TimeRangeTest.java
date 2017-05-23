package com.bc.fiduceo.core;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TimeRangeTest {

    @Test
    public void testConstructAndGet() {
        final TimeRange timeRange = new TimeRange(new Date(1001), new Date(2001));

        assertEquals(1001, timeRange.getStartDate().getTime());
        assertEquals(2001, timeRange.getStopDate().getTime());
    }
}
