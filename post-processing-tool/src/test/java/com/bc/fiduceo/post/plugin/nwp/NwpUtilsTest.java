package com.bc.fiduceo.post.plugin.nwp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NwpUtilsTest {

    @Test
    public void testComputeFutureTimeStepCount() {
        assertEquals(12, NwpUtils.computeFutureTimeStepCount(33));
        assertEquals(6, NwpUtils.computeFutureTimeStepCount(17));
    }
}
