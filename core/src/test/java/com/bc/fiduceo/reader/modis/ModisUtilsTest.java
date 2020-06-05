package com.bc.fiduceo.reader.modis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModisUtilsTest {

    @Test
    public void testExtractYMDFromFileName() {
        int[] ymd = ModisUtils.extractYearMonthDayFromFilename("MOD06_L2.A2013037.1435.006.2015066015540.hdf");
        assertEquals(2013, ymd[0]);
        assertEquals(2, ymd[1]);
        assertEquals(6, ymd[2]);

        ymd = ModisUtils.extractYearMonthDayFromFilename("MYD06_L2.A2009133.1035.006.2014062050327.hdf");
        assertEquals(2009, ymd[0]);
        assertEquals(5, ymd[1]);
        assertEquals(13, ymd[2]);

        ymd = ModisUtils.extractYearMonthDayFromFilename("MYD021KM.A2011168.2210.061.2018032001033.hdf");
        assertEquals(2011, ymd[0]);
        assertEquals(6, ymd[1]);
        assertEquals(17, ymd[2]);

        ymd = ModisUtils.extractYearMonthDayFromFilename("MOD021KM.A2003142.1445.061.2017194130122.hdf");
        assertEquals(2003, ymd[0]);
        assertEquals(5, ymd[1]);
        assertEquals(22, ymd[2]);
    }
}
