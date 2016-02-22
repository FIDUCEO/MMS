package com.bc.fiduceo.reader;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReaderUtilsTest {
    @Test
    public void testParseSensorType(){
        assertEquals(ReaderUtils.parseSensorType("NOAA_15"),"NOAA-15");
        assertEquals(ReaderUtils.parseSensorType("noaa_15"),"NOAA-15");
        assertEquals(ReaderUtils.parseSensorType("noaa-15"),"NOAA-15");
    }
}