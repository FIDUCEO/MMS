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
        taoRecord.WSPD = "27.9";
        taoRecord.WDIR = "29.0";
        taoRecord.BARO = "30.1";
        taoRecord.Q = "2342345";
        taoRecord.M = "DMDMDMD";

        assertEquals("1543910594 23.5 24.6 25.7 26.8 27.9 29.0 30.1 2342345 DMDMDMD", taoRecord.toLine());
    }

    @Test
    public void testDeleteMe() {
        System.out.println("0123".substring(2, 4));
    }
}
