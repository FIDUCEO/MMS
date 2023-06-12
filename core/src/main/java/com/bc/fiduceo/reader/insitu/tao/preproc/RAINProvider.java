package com.bc.fiduceo.reader.insitu.tao.preproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class RAINProvider {

    private HashMap<Integer, RAINRecord> rainMap;

    void open(File rainFile) {
        rainMap = new HashMap<>();

        if (rainFile == null){
            return;
        }

        try (final FileReader fileReader = new FileReader(rainFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                System.out.println("line = " + line);

                final RAINRecord rainRecord = new RAINRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                rainRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                rainRecord.RAIN = tokens[2];
                rainRecord.Q = tokens[3];
                rainRecord.M = tokens[4];

                rainMap.put(rainRecord.date, rainRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    RAINRecord get(int date) {
        RAINRecord sstRecord = rainMap.get(date);
        if (sstRecord == null) {
            sstRecord = new RAINRecord();
            sstRecord.date = date;
            sstRecord.RAIN = "-9.99";
            sstRecord.Q = "9";
            sstRecord.M = "D";
        }

        return sstRecord;
    }
}
