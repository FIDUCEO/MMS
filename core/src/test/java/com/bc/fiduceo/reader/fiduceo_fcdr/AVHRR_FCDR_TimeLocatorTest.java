package com.bc.fiduceo.reader.fiduceo_fcdr;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AVHRR_FCDR_TimeLocatorTest {

    private static final double[] TIMES = {7.03260852587127E8, 7.03260853091812E8, 7.0326085358963E8, 7.03260854090881E8, 7.03260854588699E8, 7.0326085508995E8, 7.03260855587768E8, 7.03260856089019E8, 7.0326085659027E8, 7.03260857091522E8, 7.03260857592773E8};
    private Array time;

    @Before
    public void setUp() {
        time = Array.factory(TIMES);
    }

    @Test
    public void testGetTime() {
        final AVHRR_FCDR_TimeLocator timeLocator = new AVHRR_FCDR_TimeLocator(time);

        long pixelTime = timeLocator.getTimeFor(0, 0);
        assertEquals(703260853, pixelTime);

        pixelTime = timeLocator.getTimeFor(108, 0);
        assertEquals(703260853, pixelTime);

        pixelTime = timeLocator.getTimeFor(0, 6);
        assertEquals(703260856, pixelTime);

        pixelTime = timeLocator.getTimeFor(3, 10);
        assertEquals(703260858, pixelTime);
    }

    @Test
    public void testGetTime_out_of_range() {
        final AVHRR_FCDR_TimeLocator timeLocator = new AVHRR_FCDR_TimeLocator(time);

        try {
            timeLocator.getTimeFor(0, -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            timeLocator.getTimeFor(0, 11);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }
}
