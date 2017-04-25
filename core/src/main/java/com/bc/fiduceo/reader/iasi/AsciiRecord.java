package com.bc.fiduceo.reader.iasi;

import org.esa.snap.core.datamodel.MetadataElement;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.*;

abstract class AsciiRecord {

    private final Map<String, String> map;
    private final int fieldCount;

     AsciiRecord(int fieldCount) {
        this.map = new HashMap<>();
        this.fieldCount = fieldCount;
    }

    void readRecord(ImageInputStream iis) throws IOException {
        for (int i = 0; i < fieldCount; i++) {
            final String fieldString = iis.readLine();
            final KeyValuePair field = new KeyValuePair(fieldString);

            map.put(field.key, field.value);
        }
    }

    String getValue(String key) {
        return map.get(key);
    }

    int getIntValue(String key) {
        return Integer.parseInt(getValue(key));
    }

    long getLongValue(String key) {
        return Long.parseLong(getValue(key));
    }

    abstract MetadataElement getMetaData();

    private static class KeyValuePair {
        final String key;
        final String value;

        public KeyValuePair(String field) {
            key = field.substring(0, 30).trim();
            value = field.substring(32).trim();
        }
    }
}
