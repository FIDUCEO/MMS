package com.bc.fiduceo.reader.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeLocator_MicrosSince2000Test {

    @Test
    public void testGetTimes() {
        final long[] time_2000 = {592784694225744L, 592784694525730L, 592784694825716L, 592784695125701L, 592784695425687L, 592784695725673L};

        final TimeLocator_MicrosSince2000 timeLocator = new TimeLocator_MicrosSince2000(time_2000);

        assertEquals(1539469494226L, timeLocator.getTimeFor(0, 0));
        assertEquals(1539469494826L, timeLocator.getTimeFor(1000, 2));
        assertEquals(1539469495726L, timeLocator.getTimeFor(2000, 5));
    }

    @Test
    public void testGetTimes_outside_data() {
        final long[] time_2000 = {592784694225744L, 592784694525730L, 592784694825716L, 592784695125701L, 592784695425687L, 592784695725673L};

        final TimeLocator_MicrosSince2000 timeLocator = new TimeLocator_MicrosSince2000(time_2000);

        assertEquals(-1L, timeLocator.getTimeFor(0, -1));
        assertEquals(-1L, timeLocator.getTimeFor(0, 6));
    }
}
