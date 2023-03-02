package com.bc.fiduceo.reader.insitu.ndbc;

import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;
import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;

class StationDatabase {

    private static final int CW_STATION_TOKENS = 5;
    private static final int SM_STATION_TOKENS = 8;

    private final HashMap<String, Station> stationMap;

    StationDatabase() {
        stationMap = new HashMap<>();
    }

    void load(InputStream inputStream) throws IOException {
        final ArrayList<String> lineList = readInputLines(inputStream);

        final String firstLine = lineList.get(0);
        final String[] tokens = StringUtils.split(firstLine, new char[]{'|'}, false);
        if (tokens.length == CW_STATION_TOKENS) {
            parseCwStation(lineList);
        } else if (tokens.length == SM_STATION_TOKENS) {
            parseSmStations(lineList);
        } else {
            throw new IllegalArgumentException("unknown input data format");
        }

    }

    private void parseSmStations(ArrayList<String> lineList) {
        for (final String line : lineList) {
            final String[] tokens = StringUtils.split(line, new char[]{'|'}, true);
            final String stationId = tokens[0];

            final Station station = new Station(stationId, STANDARD_METEOROLOGICAL);
            parseBasicProperties(tokens, station);

            station.setAirTemperatureHeight(Float.parseFloat(tokens[5]));
            station.setBarometerHeight(Float.parseFloat(tokens[6]));
            station.setSSTDepth(Float.parseFloat(tokens[7]));

            stationMap.put(stationId, station);
        }
    }

    private void parseCwStation(ArrayList<String> lineList) {
        for (final String line : lineList) {
            final String[] tokens = StringUtils.split(line, new char[]{'|'}, true);
            final String stationId = tokens[0];

            final Station station = new Station(stationId, CONSTANT_WIND);
            parseBasicProperties(tokens, station);

            stationMap.put(tokens[0], station);
        }
    }

    private static void parseBasicProperties(String[] tokens, Station station) {
        station.setLat(Float.parseFloat(tokens[1]));
        station.setLon(Float.parseFloat(tokens[2]));

        final StationType stationType = StationType.valueOf(tokens[3]);
        station.setType(stationType);

        station.setAnemometerHeight(Float.parseFloat(tokens[4]));
    }

    private static ArrayList<String> readInputLines(InputStream inputStream) throws IOException {
        final ArrayList<String> lineList = new ArrayList<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                // skip comment lines tb 2023-03-02
                continue;
            }
            lineList.add(line);
        }
        return lineList;
    }

    Station get(String id) {
        return stationMap.get(id.toUpperCase());
    }
}
