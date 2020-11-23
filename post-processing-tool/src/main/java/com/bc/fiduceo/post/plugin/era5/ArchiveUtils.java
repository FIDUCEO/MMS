package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

class ArchiveUtils {

    private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");
    private static final DecimalFormat threeDigitsFormat = new DecimalFormat("000");

    private static final String FILE_NAME_BEGIN = "ecmwf-era5_oper_";

    private final String rootPath;

    ArchiveUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    static String getFileName(String collection, String variable, String timeString) {
        return FILE_NAME_BEGIN + collection + "_" + timeString + "." + variable + ".nc";
    }

    static String mapVariable(String variable) {
        switch (variable) {
            case "t2m":
                return "2t";
            case "u10":
                return "10u";
            case "v10":
                return "10v";
            case "siconc":
                return "ci";
            default:
                return variable;
        }
    }

    static String getForecastTimeString(Calendar utcCalendar) {
        return null;
    }

    static String getAnalysisTimeString(Calendar utcCalendar) {
        return null;
    }

    static String getTimeString(String collection, Calendar utcCalendar) throws IOException {
        final int year = utcCalendar.get(Calendar.YEAR);

        final int month = utcCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        int hour = utcCalendar.get(Calendar.HOUR_OF_DAY);

        if (collection.startsWith("an_")) {
            final String hourString = twoDigitsFormat.format(hour);
            return year + monthString + dayString + hourString + "00";
        } else if (collection.startsWith("fc_")) {
            int forecastTimeStep;
            if (hour <= 6) {
                utcCalendar.add(Calendar.HOUR_OF_DAY, -1);
                forecastTimeStep = 6 + hour;
                hour = 18;
            } else if(hour <= 18) {
                forecastTimeStep = hour - 6;
                hour = 6;
            } else {
                forecastTimeStep = hour - 18;
                hour = 18;
            }
            final String hourString = twoDigitsFormat.format(hour);

            return year + monthString + dayString + hourString + threeDigitsFormat.format(forecastTimeStep);
        } else {
            throw new IOException("Unknown era5 collection: " + collection);
        }
    }

    public String get(String variableType, int timeStamp) throws IOException {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTimeInMillis(timeStamp * 1000L);

        final int cutPoint = variableType.lastIndexOf("_");
        final String collection = variableType.substring(0, cutPoint);

        String variable = variableType.substring(cutPoint + 1, variableType.length());
        variable = mapVariable(variable);

        final String timeString = getTimeString(collection, utcCalendar);
        final String fileName = getFileName(collection, variable, timeString);

        final int year = utcCalendar.get(Calendar.YEAR);

        final int month = utcCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        return rootPath + File.separator + collection + File.separator +
                year + File.separator+ monthString + File.separator + dayString + File.separator +
                fileName;
    }
}
