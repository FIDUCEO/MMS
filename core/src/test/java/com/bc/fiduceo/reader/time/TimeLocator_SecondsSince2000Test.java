package com.bc.fiduceo.reader.time;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import static org.junit.Assert.assertEquals;

public class TimeLocator_SecondsSince2000Test {

    private TimeLocator timeLocator;

    @Before
    public void setUp() {
        final double[] secsSince2K = {-9999.0, 5.783390980892617E8, 5.783390982892617E8, -9999.0,
                5.783390958892617E8, 5.783390962892617E8, -9999.0, 5.783390964892616E8,
                5.783390940892617E8, 5.783390934892616E8, 5.783390936892617E8, 5.783390940892617E8};

        final Array secsSince2KArray = Array.factory(DataType.DOUBLE, new int[]{3, 4}, secsSince2K);

        timeLocator = new TimeLocator_SecondsSince2000(secsSince2KArray, -9999.0);
    }

    @Test
    public void testGetTimes() {
        assertEquals(1525023896000L, timeLocator.getTimeFor(0, 1));
        assertEquals(1525023893000L, timeLocator.getTimeFor(1, 2));
        assertEquals(1525023898000L, timeLocator.getTimeFor(2, 0));
    }

    @Test
    public void testGetTimes_fillValue() {
        assertEquals(-1, timeLocator.getTimeFor(0, 0));
        assertEquals(-1, timeLocator.getTimeFor(3, 0));
    }

    @Test
    public void testGetTimes_outSide() {
        assertEquals(-1, timeLocator.getTimeFor(-1, 0));
        assertEquals(-1, timeLocator.getTimeFor(4, 0));

        assertEquals(-1, timeLocator.getTimeFor(1, -1));
        assertEquals(-1, timeLocator.getTimeFor(1, 3));
    }
}
