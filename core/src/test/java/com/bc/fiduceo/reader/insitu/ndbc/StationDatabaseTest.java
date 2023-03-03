package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;
import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;
import static com.bc.fiduceo.reader.insitu.ndbc.StationType.COAST_STATION;
import static com.bc.fiduceo.reader.insitu.ndbc.StationType.OCEAN_BUOY;
import static org.junit.Assert.assertEquals;

public class StationDatabaseTest {

    @Test
    public void testLoadAndGet_CW() throws IOException {
        final String resourceFile = "# buoy ID | latitude | longitude | type          | anemometer height\n" +
                "41002     | 31.759   | -74.936   | OCEAN_BUOY    | 4.1\n" +
                "41004     | 32.502   | -79.099   | OCEAN_BUOY    | 4.1\n" +
                "41008     | 31.4     | -80.866   | OCEAN_BUOY    | 4.9\n" +
                "41010     | 28.878   | -78.485   | OCEAN_BUOY    | 4.1";

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getBytes());

        final StationDatabase stationDatabase = new StationDatabase();
        stationDatabase.load(inputStream);

        Station station = stationDatabase.get("41002");
        assertEquals("41002", station.getId());
        assertEquals(31.759f, station.getLat(), 1e-8);
        assertEquals(-74.936f, station.getLon(), 1e-8);
        assertEquals(OCEAN_BUOY, station.getType());
        assertEquals(CONSTANT_WIND, station.getMeasurementType());
        assertEquals(4.1f, station.getAnemometerHeight(), 1e-8);

        station = stationDatabase.get("41004");
        assertEquals("41004", station.getId());
        assertEquals(32.502f, station.getLat(), 1e-8);
        assertEquals(-79.099f, station.getLon(), 1e-8);
        assertEquals(OCEAN_BUOY, station.getType());
        assertEquals(CONSTANT_WIND, station.getMeasurementType());
        assertEquals(4.1f, station.getAnemometerHeight(), 1e-8);
    }

    @Test
    public void testLoadAndGet_SM() throws IOException {
        final String resourceFile = "AAMC1     | 37.772   | -122.3    | COAST_STATION  | 6.9               | 4.3             | 5.3              | 1.2\n" +
                "ADKA2     | 51.861   | -176.637  | COAST_STATION  | 7.0               | 6.3             | 7.0              | 2.8\n" +
                "AGMW3     | 44.608   | -87.433   | LAKE_STATION   | 9.0               | 9.0             | NaN              | NaN\n" +
                "AGXC1     | 33.716   | -118.246  | COAST_STATION  | 20.0              | NaN             | NaN              | NaN\n" +
                "AJXA2     | 58.287   | -134.398  | COAST_STATION  | 3.0               | 3.0             | 7.6              | NaN\n" +
                "ALIA2     | 56.898   | -154.247  | COAST_STATION  | 10.3              | 9.7             | 6.5              | 2.2";

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getBytes());

        final StationDatabase stationDatabase = new StationDatabase();
        stationDatabase.load(inputStream);

        Station station = stationDatabase.get("ADKA2");
        assertEquals("ADKA2", station.getId());
        assertEquals(51.861f, station.getLat(), 1e-8);
        assertEquals(-176.637f, station.getLon(), 1e-8);
        assertEquals(COAST_STATION, station.getType());
        assertEquals(STANDARD_METEOROLOGICAL, station.getMeasurementType());
        assertEquals(7.0f, station.getAnemometerHeight(), 1e-8);
        assertEquals(6.3f, station.getAirTemperatureHeight(), 1e-8);
        assertEquals(7.0f, station.getBarometerHeight(), 1e-8);
        assertEquals(2.8f, station.getSSTDepth(), 1e-8);
    }

    @Test
    public void testLoadAndGet_SM_smallLetters() throws IOException {
        final String resourceFile = "AAMC1     | 37.772   | -122.3    | COAST_STATION  | 6.9               | 4.3             | 5.3              | 1.2\n" +
                "ADKA2     | 51.861   | -176.637  | COAST_STATION  | 7.0               | 6.3             | 7.0              | 2.8\n";

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getBytes());

        final StationDatabase stationDatabase = new StationDatabase();
        stationDatabase.load(inputStream);

        Station station = stationDatabase.get("aamc1");
        assertEquals("AAMC1", station.getId());
    }
}
