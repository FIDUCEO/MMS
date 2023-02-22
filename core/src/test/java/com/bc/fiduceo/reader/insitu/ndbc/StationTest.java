package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StationTest {

    @Test
    public void testConstruction() {
        final Station station = new Station("abgdzt");

        assertEquals("abgdzt", station.getId());
        assertEquals(Float.NaN, station.getLon(), 1e-8);
        assertEquals(Float.NaN, station.getLat(), 1e-8);
        assertNull(station.getType());
        assertEquals(Float.NaN, station.getAnemometerHeight(), 1e-8);
    }
}
