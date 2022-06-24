package com.bc.fiduceo.store;

import java.util.*;

public class InMemoryStore implements Store {
    private final Map<String, byte[]> map = new HashMap<>();

    @Override
    public byte[] getBytes(String key) {
        return map.get(key);
    }

    @Override
    public TreeSet<String> getKeysEndingWith(String suffix) {
        final Set<String> keySet = map.keySet();
        final TreeSet<String> arrayKeys = new TreeSet<>();
        for (String key : keySet) {
            if (key.endsWith(suffix)) {
                arrayKeys.add(key);
            }
        }
        return arrayKeys;
    }
}
