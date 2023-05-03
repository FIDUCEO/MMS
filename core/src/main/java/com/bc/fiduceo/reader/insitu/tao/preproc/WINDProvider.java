package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class WINDProvider {

    private HashMap<Integer, WINDRecord> windMap;

    void open(File windFile) {
        windMap = new HashMap<>();

        if (windFile == null) {
            return;
        }

        try (final FileReader fileReader = new FileReader(windFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final WINDRecord windRecord = new WINDRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                windRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                windRecord.WSPD = tokens[4];
                windRecord.WDIR = tokens[5];
                windRecord.Q = tokens[6].substring(2, 4);
                windRecord.M = tokens[7].substring(2, 4);

                windMap.put(windRecord.date, windRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    WINDRecord get(int date) {
        WINDRecord windRecord = windMap.get(date);
        if (windRecord == null) {
            windRecord = new WINDRecord();
            windRecord.date = date;
            windRecord.WSPD = "-99.9";
            windRecord.WDIR = "-99.9";
            windRecord.Q = "99";
            windRecord.M = "DD";
        }

        return windRecord;
    }
}
