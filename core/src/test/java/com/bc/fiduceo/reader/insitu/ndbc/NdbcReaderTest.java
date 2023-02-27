package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Test;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;
import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;
import static com.bc.fiduceo.reader.insitu.ndbc.StationType.*;
import static org.junit.Assert.assertEquals;

public class NdbcReaderTest {

    @Test
    public void testStationTypeToByte() {
        assertEquals(0, NdbcReader.toByte(OCEAN_BUOY));
        assertEquals(1, NdbcReader.toByte(COAST_BUOY));
        assertEquals(2, NdbcReader.toByte(LAKE_BUOY));
        assertEquals(3, NdbcReader.toByte(OCEAN_STATION));
        assertEquals(4, NdbcReader.toByte(COAST_STATION));
        assertEquals(5, NdbcReader.toByte(LAKE_STATION));
    }

    @Test
    public void testMeasurementTypeToByte() {
        assertEquals(0, NdbcReader.toByte(CONSTANT_WIND));
        assertEquals(1, NdbcReader.toByte(STANDARD_METEOROLOGICAL));
    }
}
