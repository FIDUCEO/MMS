package com.bc.fiduceo.reader.smos;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import static org.junit.Assert.assertEquals;

public class SmosL1CTimeLocatorTest {

    @Test
    public void testGetTimeFor() {
        final Array days = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{6533, 6533, 6533, 6533, 6533, 6533, 6533, 6533, 6533});
        final Array seconds = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{85318, 85317, 85317, 85307, 85306, 85306, 85297, 85296, 85295});
        final Array micros = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{154074,	954065, 354059, 153977, 553973, 353961, 353903, 153887, 953875});

        final SmosL1CTimeLocator timeLocator = new SmosL1CTimeLocator(days, seconds, micros);

        long time = timeLocator.getTimeFor(0, 0);
        assertEquals(1511221318154L, time);

        time = timeLocator.getTimeFor(1, 0);
        assertEquals(1511221317954L, time);

        time = timeLocator.getTimeFor(2, 2);
        assertEquals(1511221295953L, time);
    }

    @Test
    public void testGetTimeFor_outside() {
        final Array days = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{6533, 6533, 6533, 6533, 6533, 6533, 6533, 6533, 6533});
        final Array seconds = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{85318, 85317, 85317, 85307, 85306, 85306, 85297, 85296, 85295});
        final Array micros = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{154074, 954065, 354059, 153977, 553973, 353961, 353903, 153887, 953875});

        final SmosL1CTimeLocator timeLocator = new SmosL1CTimeLocator(days, seconds, micros);

        long time = timeLocator.getTimeFor(-1, 0);
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(3, 0);
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(1, -1);
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(1, 3);
        assertEquals(-1L, time);
    }

    @Test
    public void testGetTimeFor_fillValue() {
        final Array days = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{-2147483647, 6533, 6533, 6533, 6533, 6533, 6533, 6533, 6533});
        final Array seconds = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{85318, -2147483647, 85317, 85307, 85306, 85306, 85297, 85296, 85295});
        final Array micros = Array.factory(DataType.INT, new int[]{3, 3}, new int[]{154074, 954065, -2147483647, 153977, 553973, 353961, 353903, 153887, 953875});

        final SmosL1CTimeLocator timeLocator = new SmosL1CTimeLocator(days, seconds, micros);
        long time = timeLocator.getTimeFor(0, 0);   // day has fill value
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(1, 0);    // second has fill value
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(2, 0);    // micro has fill value
        assertEquals(-1L, time);

        time = timeLocator.getTimeFor(0, 1);
        assertEquals(1511221307153L, time);
    }
}
