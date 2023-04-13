package com.bc.fiduceo.reader.insitu.tao.preproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

class POSProvider {

    private POSRecord[] posArray;
    private float nominalLon;
    private float nominalLat;

    void open(File posFile, float nominalLon, float nominalLat) {
        this.nominalLon = nominalLon;
        this.nominalLat = nominalLat;

        if (posFile == null) {
            posArray = new POSRecord[0];
            return;
        }

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
        for (int i = 0; i < posArray.length; i++) {
            if (posArray[i].date < date) {
                continue;
            }

            if (posArray[i].date == date) {
                return posArray[i];
            }

            if (i > 0) {
                return interpolate(posArray[i - 1], posArray[i], date);
            } else {
                return posArray[0];
            }
        }

        final POSRecord posRecord = new POSRecord();
        posRecord.lon = nominalLon;
        posRecord.lat = nominalLat;
        return posRecord;
    }

    static POSRecord interpolate(POSRecord before, POSRecord after, int date) {
        final POSRecord posRecord = new POSRecord();
        posRecord.date = date;

        final float factor = (float) (date - before.date) / (float) (after.date - before.date);

        posRecord.lon = before.lon + factor * (after.lon - before.lon);
        posRecord.lat = before.lat + factor * (after.lat - before.lat);
        return posRecord;
    }
}
