package com.bc.fiduceo.reader.insitu.tao.preproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

class POSProvider {

    private POSRecord[] posArray;

    void open(File posFile) {
        final ArrayList<POSRecord> posRecords = new ArrayList<>();

        try (final FileReader fileReader = new FileReader(posFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final POSRecord posRecord = new POSRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                posRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                posRecord.lon = Float.parseFloat(tokens[2]);
                posRecord.lat = Float.parseFloat(tokens[3]);

                posRecords.add(posRecord);
            }

            posRecords.sort(Comparator.comparing(POSRecord::getDate));
            posArray = posRecords.toArray(new POSRecord[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    POSRecord get(int date) {
        return null;
        // todo 1 tb/tb continue here
    }
}
