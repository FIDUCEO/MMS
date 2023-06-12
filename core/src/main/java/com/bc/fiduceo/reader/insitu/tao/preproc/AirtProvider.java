package com.bc.fiduceo.reader.insitu.tao.preproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class AirtProvider {

    private HashMap<Integer, AIRTRecord> airtMap;

    void open(File airtFile) {
        airtMap = new HashMap<>();

        if (airtFile == null) {
            return;
        }

        try (final FileReader fileReader = new FileReader(airtFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final AIRTRecord airtRecord = new AIRTRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                airtRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                airtRecord.AIRT = tokens[2];
                airtRecord.Q = tokens[3];
                airtRecord.M = tokens[4];

                airtMap.put(airtRecord.date, airtRecord);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    AIRTRecord get(int date) {
        AIRTRecord airtRecord = airtMap.get(date);
        if (airtRecord == null) {
            airtRecord = new AIRTRecord();
            airtRecord.date = date;
            airtRecord.AIRT = "-9.99";
            airtRecord.Q = "9";
            airtRecord.M = "D";
        }

        return airtRecord;
    }
}
