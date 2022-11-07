package com.bc.fiduceo.reader.insitu.sic_cci;

import ucar.ma2.Array;

import java.util.HashMap;

class Section {

    private final HashMap<String, Array> dataMap;

    Section() {
        dataMap = new HashMap<>();
    }

    void add(String name, Array data) {
        dataMap.put(name, data);
    }

    Array get(String name) {
        final Array data = dataMap.get(name);

        if(data == null) {
            throw new RuntimeException("invalid variable name: " + name);
        }

        return data;
    }
}
