package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TAORecordTest {

    @Test
    public void testToString()  {
        TAORecord taoRecord = new TAORecord();
        taoRecord.date = 1543910594;
        taoRecord.lon = "-118.34";
        taoRecord.lat = "51.886";
        taoRecord.SSS = "23.5";
        taoRecord.SST = "24.6";
        taoRecord.AIRT = "25.7";
        taoRecord.RH = "26.8";
        taoRecord.WSPD = "27.9";
        taoRecord.WDIR = "29.0";
        taoRecord.BARO = "30.1";
        taoRecord.RAIN = "31.2";
        taoRecord.Q = "2342345";
        taoRecord.M = "DMDMDMD";

        assertEquals("1543910594 -118.34 51.886 23.5 24.6 25.7 26.8 27.9 29.0 30.1 31.2 2342345 DMDMDMD", taoRecord.toLine());
    }
}
