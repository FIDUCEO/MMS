package com.bc.fiduceo.reader.time;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class TimeLocator_TAI1993Test {

    @Test
    public void testGetTimeFor() {
        final double[][] timeData = {{1.8, 2.9, 3.0, 4.2, 5.2, 6.3},
                {1.9, 3.0, 3.1, 4.3, 5.3, 6.4},
                {2.0, 3.1, 3.2, 4.4, 5.4, 6.5}
        };
        final Array timeDataArray = NetCDFUtils.create(timeData);

        final TimeLocator_TAI1993 timeLocator = new TimeLocator_TAI1993(timeDataArray);

        assertEquals(725846401800L, timeLocator.getTimeFor(0, 0));
        assertEquals(725846403100L, timeLocator.getTimeFor(2, 1));
        assertEquals(725846404400L, timeLocator.getTimeFor(3, 2));
    }
}
