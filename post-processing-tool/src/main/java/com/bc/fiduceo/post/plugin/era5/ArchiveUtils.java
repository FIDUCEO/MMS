package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.TimeUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

class ArchiveUtils {

    private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");

    private static final String FILE_NAME_BEGIN = "ecmwf-era5_oper_";

    private final String rootPath;

    ArchiveUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    static String getFileName(String collection, String variable, String ymd, String hour) {
        return FILE_NAME_BEGIN + collection + "_" + ymd + hour + "00." + variable + ".nc";
    }

    public String get(String variableType, int timeStamp) {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTimeInMillis(timeStamp * 1000L);
        final int year = utcCalendar.get(Calendar.YEAR);

        final int month = utcCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        final int hour = utcCalendar.get(Calendar.HOUR_OF_DAY);
        final String hourString = twoDigitsFormat.format(hour);

        final int cutPoint = variableType.lastIndexOf("_");
        final String collection = variableType.substring(0, cutPoint);
        final String variable = variableType.substring(cutPoint + 1, variableType.length());

        final String ymd = year + monthString + dayString;
        final String fileName = getFileName(collection, variable, ymd, hourString);

        return rootPath + File.separator + collection + File.separator +
                year + File.separator + monthString + File.separator + dayString + File.separator +
                fileName;
    }
}
