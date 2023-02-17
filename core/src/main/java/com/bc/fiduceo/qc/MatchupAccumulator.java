package com.bc.fiduceo.qc;

import com.bc.fiduceo.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

class MatchupAccumulator {

    private final Calendar utcCalendar;
    private final HashMap<String, Integer> daysMap;
    private int matchupSum;
    private int fileCount;

    public MatchupAccumulator() {
        utcCalendar = TimeUtils.getUTCCalendar();
        daysMap = new HashMap<>();
        matchupSum = 0;
        fileCount = 0;
    }

    HashMap<String, Integer> getDaysMap() {
        return daysMap;
    }

    public int getSummaryCount() {
        return matchupSum;
    }

    public void add(int timeStamp) {
        utcCalendar.setTimeInMillis(timeStamp * 1000L);
        final Date time = utcCalendar.getTime();
        final String dayString = TimeUtils.format(time, "yyyy-MM-dd");

        Integer count = daysMap.get(dayString);
        if (count == null) {
            count = 1;
            daysMap.put(dayString, count);
        } else {
            ++count;
            daysMap.replace(dayString, count);
        }

        matchupSum++;
    }

    int getFileCount() {
        return fileCount;
    }

    void countFile() {
        ++fileCount;
    }
}
