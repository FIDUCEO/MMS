package com.bc.fiduceo.reader.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeLocator_MillisSince1970Test {

    @Test
    public void testGetTimes() {
        final long[] time = {1451610000000L, 1451613600000L, 1451620800000L, 1451620800000L, 1451620800000L, 1451624400000L};

        final TimeLocator_MillisSince1970 timeLocator = new TimeLocator_MillisSince1970(time);

        assertEquals(1451610000000L, timeLocator.getTimeFor(0, 0));
        assertEquals(1451620800000L, timeLocator.getTimeFor(100, 2));
        assertEquals(1451624400000L, timeLocator.getTimeFor(200, 5));
    }

    @Test
    public void testGetTimes_outside_data() {
        final long[] time = {1451610000000L, 1451613600000L, 1451620800000L, 1451620800000L, 1451620800000L, 1451624400000L};

        final TimeLocator_MillisSince1970 timeLocator = new TimeLocator_MillisSince1970(time);

        assertEquals(-1L, timeLocator.getTimeFor(0, -1));
        assertEquals(-1L, timeLocator.getTimeFor(200, 6));
    }
}
