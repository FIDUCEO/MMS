package com.bc.fiduceo.reader.insitu.sic_cci;

import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

// @todo 1 tb/tb write tests! 2022-11-08
class SectionCache {

    private final ArrayList<String> lineList;
    private final AbstractSectionParser[] sectionParsers;
    private HashMap<String, Section[]> sectionMap;
    private final ArrayList<String> additionalSectionKeys;

    SectionCache(ArrayList<String> linelist, AbstractSectionParser[] sectionParsers) {
        this.lineList = linelist;
        this.sectionParsers = sectionParsers;

        sectionMap = new HashMap<>();
        additionalSectionKeys = new ArrayList<>();
        for (final AbstractSectionParser parser : sectionParsers) {
            final String namePrefix = parser.getNamePrefix();
            sectionMap.put(namePrefix, new Section[linelist.size()]);
            if (!namePrefix.equals("REF")) {
                additionalSectionKeys.add(namePrefix);
            }
        }
    }

    void close() {
        sectionMap.clear();
        sectionMap = null;
    }

    Array get(String variableName, int y) throws ParseException {
        final int splitIdx = variableName.indexOf("_");
        String sectionKey;
        if (splitIdx < 0) {
            sectionKey = "REF";
        } else {
            sectionKey = variableName.substring(0, splitIdx);
            if (!additionalSectionKeys.contains(sectionKey)) {
                sectionKey = "REF";
            }
        }

        final Section[] sections = sectionMap.get(sectionKey);
        if (sections[y] == null) {
            final String line = lineList.get(y);
            final String[] tokens = StringUtils.split(line, new char[]{','}, true);

            int tokenOffset = 0;
            for (final AbstractSectionParser parser : sectionParsers) {
                final Section section = parser.parse(tokens, tokenOffset);
                tokenOffset += parser.getNumVariables();

                final Section[] fillSections = sectionMap.get(parser.getNamePrefix());
                fillSections[y] = section;
            }
        }

        return sections[y].get(variableName);
    }
}
