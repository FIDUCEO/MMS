package com.bc.fiduceo.math;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TimeInfoTest {

    @Test
    public void testConstruction() {
        final TimeInfo timeInfo = new TimeInfo();

        assertNull(timeInfo.getTimeInterval());
        assertEquals(Integer.MAX_VALUE, timeInfo.getMinimalTimeDelta());
    }
}
