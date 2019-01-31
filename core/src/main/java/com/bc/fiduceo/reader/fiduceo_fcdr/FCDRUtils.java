package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.util.TimeUtils;

import java.util.Date;

class FCDRUtils {

    private static final int START_DATE_INDEX = 5;
    private static final int STOP_DATE_INDEX = 6;

    static Date parseStartDate(String fileName) {
        return parseDateToken(fileName, START_DATE_INDEX);
    }

    static Date parseStopDate(String fileName) {
        return parseDateToken(fileName, STOP_DATE_INDEX);
    }

    private static Date parseDateToken(String fileName, int index) {
        final String[] tokens = fileName.split("_");

        return TimeUtils.parse(tokens[index], "yyyyMMddHHmmss");
    }
}
