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
        taoRecord.AIRT = "25.7";
        taoRecord.RH = "26.8";
        taoRecord.Q = "2342";
        taoRecord.M = "DMDM";

        assertEquals("1543910594 23.5 24.6 25.7 26.8 2342 DMDM", taoRecord.toLine());

    }
}
