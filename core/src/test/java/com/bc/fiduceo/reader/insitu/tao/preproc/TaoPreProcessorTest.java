package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaoPreProcessorTest {

    @Test
    public void testToUnixEpoch() {
        int unixEpoch = TaoPreProcessor.toUnixEpoch("20190223", "145623");
        assertEquals(1550933783, unixEpoch);

        unixEpoch = TaoPreProcessor.toUnixEpoch("20181204", "080314");
        assertEquals(1543910594, unixEpoch);
    }
}
