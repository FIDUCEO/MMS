package com.bc.fiduceo.reader.insitu.ndbc;

import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

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

    }

    private void parseCwStation(ArrayList<String> lineList) {

    }

    private static ArrayList<String> readInputLines(InputStream inputStream) throws IOException {
        final ArrayList<String> lineList = new ArrayList<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                // skip comment lines tb 2022-11-03
                continue;
            }
            lineList.add(line);
        }
        return lineList;
    }

    Station get(String id) {
        return new Station(id);
    }
}
