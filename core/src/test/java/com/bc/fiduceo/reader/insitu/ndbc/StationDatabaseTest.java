package com.bc.fiduceo.reader.insitu.ndbc;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        // @todo 1 tb/tb continue here 2023-02-22
        //assertEquals(31.759f, station.getLat(), 1e-8);

        station = stationDatabase.get("41004");
        assertEquals("41004", station.getId());
    }
}
