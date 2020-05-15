package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class TimeLocator_TAI1993ScanTest {

    @Test
    public void testGetTimeFor_10ScanLines() {
        final double[] timeData = {1.8, 2.9, 3.0, 4.2, 5.2, 6.3};
        final Array timeDataArray = NetCDFUtils.create(timeData);

        final TimeLocator_TAI1993Scan timeLocator = new TimeLocator_TAI1993Scan(timeDataArray, 10);
        assertEquals(725846374800L, timeLocator.getTimeFor(0, 0));
        assertEquals(725846374800L, timeLocator.getTimeFor(5, 6));
        assertEquals(725846374800L, timeLocator.getTimeFor(6, 9));

        assertEquals(725846379300L, timeLocator.getTimeFor(0, 50));
        assertEquals(725846379300L, timeLocator.getTimeFor(6, 59));
    }

    @Test
    public void testGetTimeFor_15ScanLines() {
        final double[] timeData = {1.8, 2.9, 3.0, 4.2, 5.2, 6.3};
        final Array timeDataArray = NetCDFUtils.create(timeData);

        final TimeLocator_TAI1993Scan timeLocator = new TimeLocator_TAI1993Scan(timeDataArray, 15);
        assertEquals(725846374800L, timeLocator.getTimeFor(0, 0));
        assertEquals(725846374800L, timeLocator.getTimeFor(5, 6));
        assertEquals(725846374800L, timeLocator.getTimeFor(6, 14));

        assertEquals(725846379300L, timeLocator.getTimeFor(0, 75));
        assertEquals(725846379300L, timeLocator.getTimeFor(6, 89));
    }
}
