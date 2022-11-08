package com.bc.fiduceo.reader.insitu.sic_cci;

import ucar.ma2.Array;

import java.text.ParseException;
import java.util.ArrayList;

// @todo 1 tb/tb write tests! 2022-11-08
class SectionCache {

    private final ArrayList<String> lineList;
    private Section[] referenceSections;
    private AbstractSectionParser[] parser;

    SectionCache(ArrayList<String> linelist, AbstractSectionParser[] parser) {
        this.lineList = linelist;
        this.parser = parser;

        referenceSections = new Section[linelist.size()];
    }

    void close() {
        referenceSections = null;
    }

    Array get(String variableName, int y) throws ParseException {
        if (referenceSections[y] == null) {
            final String line = lineList.get(y);
            final String[] tokens = line.split(",");
            referenceSections[y] = parser[0].parse(tokens);
            // @todo 1 tb/tb run over all parsers and store the sections

        }
        return referenceSections[y].get(variableName);
    }
}
