package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;

class ModisUtils {

    static int[] extractYearMonthDayFromFilename(String fileName) {
        final String yearString = fileName.substring(10, 14);
        final String doyString = fileName.substring(14, 17);
        final String doyPattern = yearString + "-" + doyString;

        final Date date = TimeUtils.parseDOYBeginOfDay(doyPattern);
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(date);

        final int[] ymd = new int[3];
        ymd[0] = utcCalendar.get(Calendar.YEAR);
        ymd[1] = utcCalendar.get(Calendar.MONTH) + 1;
        ymd[2] = utcCalendar.get(Calendar.DAY_OF_MONTH);
        return ymd;
    }
}
