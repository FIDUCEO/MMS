package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TAORecordTest {

    @Test
    public void testToString()  {
        TAORecord taoRecord = new TAORecord();
        taoRecord.date = 1543910594;
        taoRecord.SSS = "23.5";
        taoRecord.SST = "24.6";
        taoRecord.Q = "23";
        taoRecord.M = "DM";

        assertEquals("1543910594 23.5 24.6 23 DM", taoRecord.toLine());

    }
}
