package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HIRS_FCDR_TimeLocatorTest {

    private static final int[] TIMES = {123456, 123457, 123458, 123459, 123460, 123461, 123462, 123463, 123464, 123465, 123466};

    private Array time;

    @Before
    public void setUp() {
        time = NetCDFUtils.create(TIMES);
    }

    @Test
    public void testGetTime() {
        final HIRS_FCDR_TimeLocator timeLocator = new HIRS_FCDR_TimeLocator(time, 0.1, 1000);

        long pixelTime = timeLocator.getTimeFor(0, 0);
        assertEquals(13345600L, pixelTime);

        pixelTime = timeLocator.getTimeFor(108, 0);
        assertEquals(13345600L, pixelTime);

        pixelTime = timeLocator.getTimeFor(0, 6);
        assertEquals(13346200L, pixelTime);

        pixelTime = timeLocator.getTimeFor(3, 10);
        assertEquals(13346600L, pixelTime);
    }

    @Test
    public void testGetTime_out_of_range() {
        final HIRS_FCDR_TimeLocator timeLocator = new HIRS_FCDR_TimeLocator(time, 0.2, 100);

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
