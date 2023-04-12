package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class SSTProvider {

    private HashMap<Integer, SSTRecord> sstMap;

    void open(File sstFile) {
        sstMap = new HashMap<>();

        try (final FileReader fileReader = new FileReader(sstFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final SSTRecord sstRecord = new SSTRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                sstRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                sstRecord.SST = tokens[2];
                sstRecord.Q = tokens[3];
                sstRecord.M = tokens[4];

                sstMap.put(sstRecord.date, sstRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    SSTRecord get(int date) {
        SSTRecord sstRecord = sstMap.get(date);
        if (sstRecord == null) {
            sstRecord = new SSTRecord();
            sstRecord.date = date;
            sstRecord.SST = "-9.999";
            sstRecord.Q = "9";
            sstRecord.M = "D";
        }

        return sstRecord;
    }
}
