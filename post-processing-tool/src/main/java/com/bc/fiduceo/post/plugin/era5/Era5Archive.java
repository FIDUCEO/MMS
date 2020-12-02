package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.TimeUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

class Era5Archive {

    private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");
    private static final DecimalFormat threeDigitsFormat = new DecimalFormat("000");
    private static final String FILE_NAME_BEGIN = "ecmwf-era5_oper_";

    private final String rootPath;

    Era5Archive(String rootPath) {
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

    static String getTimeString(String collection, Calendar utcCalendar) {
        final Calendar clonedCalendar = (Calendar) utcCalendar.clone();

        int hour = clonedCalendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 6 && collection.startsWith("fc_")) {
            clonedCalendar.add(Calendar.DATE, -1);
        }

        final int year = clonedCalendar.get(Calendar.YEAR);

        final int month = clonedCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = clonedCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        if (collection.startsWith("an_")) {
            final String hourString = twoDigitsFormat.format(hour);
            return year + monthString + dayString + hourString + "00";
        } else if (collection.startsWith("fc_")) {
            int forecastTimeStep;
            if (hour <= 6) {
                forecastTimeStep = 6 + hour;
                hour = 18;
            } else if (hour <= 18) {
                forecastTimeStep = hour - 6;
                hour = 6;
            } else {
                forecastTimeStep = hour - 18;
                hour = 18;
            }

            final String hourString = twoDigitsFormat.format(hour);
            return year + monthString + dayString + hourString + threeDigitsFormat.format(forecastTimeStep);
        } else {
            throw new IllegalArgumentException("Unknown era5 collection: " + collection);
        }
    }

    String get(String variableType, int timeStamp) {
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
                year + File.separator + monthString + File.separator + dayString + File.separator +
                fileName;
    }
}
