package com.bc.fiduceo.reader.insitu.tao.preproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class BAROProvider {

    private HashMap<Integer, BARORecord> baroMap;

    void open(File baroFile) {
        baroMap = new HashMap<>();

        if (baroFile == null) {
            return;
        }

        try (final FileReader fileReader = new FileReader(baroFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final BARORecord baroRecord = new BARORecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                baroRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                baroRecord.BARO = tokens[2];
                baroRecord.Q = tokens[3];
                baroRecord.M = tokens[4];

                baroMap.put(baroRecord.date, baroRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BARORecord get(int date) {
        BARORecord baroRecord = baroMap.get(date);
        if (baroRecord == null) {
            baroRecord = new BARORecord();
            baroRecord.date = date;
            baroRecord.BARO = "-9.9";
            baroRecord.Q = "9";
            baroRecord.M = "D";
        }

        return baroRecord;
    }
}
