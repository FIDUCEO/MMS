package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Before;
import org.junit.Test;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;
import static com.bc.fiduceo.reader.insitu.ndbc.StationType.OCEAN_STATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StationTest {

    private Station station;

    @Before
    public void setUp() {
        station = new Station("abgdzt", STANDARD_METEOROLOGICAL);
    }

    @Test
    public void testConstruction() {
        assertEquals("abgdzt", station.getId());
        assertEquals(Float.NaN, station.getLon(), 1e-8);
        assertEquals(Float.NaN, station.getLat(), 1e-8);
        assertNull(station.getType());
        assertEquals(STANDARD_METEOROLOGICAL, station.getMeasurementType());
        assertEquals(Float.NaN, station.getAnemometerHeight(), 1e-8);
        assertEquals(Float.NaN, station.getAirTemperatureHeight(), 1e-8);
        assertEquals(Float.NaN, station.getBarometerHeight(), 1e-8);
        assertEquals(Float.NaN, station.getSSTDepth(), 1e-8);
    }

    @Test
    public void testSetGetLat() {
        station.setLat(23.56f);
        assertEquals(23.56f, station.getLat(), 1e-8);
    }

    @Test
    public void testSetGetLon() {
        station.setLon(34.56f);
        assertEquals(34.56f, station.getLon(), 1e-8);
    }

    @Test
    public void testSetGetType() {
        station.setType(OCEAN_STATION);
        assertEquals(OCEAN_STATION, station.getType());
    }

    @Test
    public void testSetGetAnemometerHeight() {
        station.setAnemometerHeight(2.8f);
        assertEquals(2.8f, station.getAnemometerHeight(), 1e-8);
    }

    @Test
    public void testSetGetAirTemperatureHeight() {
        station.setAirTemperatureHeight(3.9f);
        assertEquals(3.9f, station.getAirTemperatureHeight(), 1e-8);
    }

    @Test
    public void testSetGetBarometerHeight() {
        station.setBarometerHeight(4.0f);
        assertEquals(4.0f, station.getBarometerHeight(), 1e-8);
    }

    @Test
    public void testSetGetSSTDepth() {
        station.setSSTDepth(5.1f);
        assertEquals(5.1f, station.getSSTDepth(), 1e-8);
    }
}
