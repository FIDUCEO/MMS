package com.bc.fiduceo.reader.insitu.tao.preproc;

import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

class RHProvider {

    private HashMap<Integer, RHRecord> rhMap;

    void open(File rhFile) {
        rhMap = new HashMap<>();

        if (rhFile == null) {
            return;
        }

        try (final FileReader fileReader = new FileReader(rhFile)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!Character.isDigit(line.charAt(0))) {
                    continue;
                }

                final RHRecord rhRecord = new RHRecord();
                final String[] tokens = TaoPreProcessor.tokenize(line);
                rhRecord.date = TaoPreProcessor.toUnixEpoch(tokens[0], tokens[1]);
                rhRecord.RH = tokens[2];
                rhRecord.Q = tokens[3];
                rhRecord.M = tokens[4];

                rhMap.put(rhRecord.date, rhRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    RHRecord get(int date) {
        RHRecord rhRecord = rhMap.get(date);
        if (rhRecord == null) {
            rhRecord = new RHRecord();
            rhRecord.date = date;
            rhRecord.RH = "-9.99";
            rhRecord.Q = "9";
            rhRecord.M = "D";
        }

        return rhRecord;
    }
}
