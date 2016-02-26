/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.util;

import org.esa.snap.core.datamodel.ProductData;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeUtils {

    private static ThreadLocal<Calendar> calendarThreadLocal = new CalendarThreadLocal();

    public static Date create(long millisSinceEpoch) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(millisSinceEpoch);
        return calendar.getTime();
    }

    public static String format(Date date) {
        return ProductData.UTC.createDateFormat().format(date);
    }

    public static String format(Date date, String pattern) {
        return ProductData.UTC.createDateFormat(pattern).format(date);
    }

    public static Date toDate(Timestamp timestamp) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(timestamp.getTime());
        return calendar.getTime();
    }

    public static Timestamp toTimestamp(Date date) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(date.getTime());
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static Date parse(String dateString, String pattern) {
        try {
            return ProductData.UTC.createDateFormat(pattern).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Unparseable date: " + dateString);
        }
    }

    public static Calendar getUTCCalendar() {
        return (Calendar) calendarThreadLocal.get().clone();
    }

    public static Date parseDOYBeginOfDay(String dateString) {
        return parse(dateString, "yyyy-DDD");
    }

    //todo:mba to get the product name then finilize on the seach (2016-02-23) option.
    public static List<Calendar[]> getIntervalofDate(Date startDate, Date endDate, int interval) {
        if (startDate == null || endDate == null) {
            throw new NullPointerException("The start date or end date is Null");
        }
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(startDate);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(endDate);
        long diff = calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis();
        diff = diff / (24 * 60 * 60 * 1000);
        if (diff < 0) {
            diff = diff < 0 ? -1 * diff : diff;
            Calendar temp;
            temp = calendarStart;
            calendarStart = calendarEnd;
            calendarEnd = temp;
        } else if (diff == 0) {
            throw new IllegalArgumentException("The starting aand ending date shall not me the same");
        }
        int start = calendarStart.get(Calendar.DAY_OF_YEAR);
        int startYear = calendarStart.get(Calendar.YEAR);

        int endYear = calendarEnd.get(Calendar.YEAR);

        List<Calendar[]> calendarList = new ArrayList<>();
        for (int i = 0; i < interval; i++) {
            Calendar instance[] = new Calendar[2];
            instance[0] = Calendar.getInstance();
            instance[0].set(Calendar.DAY_OF_YEAR, start);
            instance[0].set(Calendar.YEAR, startYear);

            int end = (int) (start + diff);
            instance[1] = Calendar.getInstance();
            if (end < 366) {
                instance[1].set(Calendar.DAY_OF_YEAR, end);
                instance[1].set(Calendar.YEAR, startYear);
            } else {
                endYear = (endYear - startYear) == 1 ? endYear : ++startYear;
                end = end - ((endYear % 400 == 0) || ((endYear % 100) != 0 && (endYear % 4 == 0)) ? 366 : 365);
                instance[1].set(Calendar.DAY_OF_YEAR, end);
                instance[1].set(Calendar.YEAR, endYear);
                startYear = endYear;
            }
            calendarList.add(instance);
            start = end + 1;
        }
        return calendarList;
    }

    public static Date parseDOYEndOfDay(String dateFormat) {
        final Date date = parseDOYBeginOfDay(dateFormat);
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 23);
        calendar.add(Calendar.MINUTE, 59);
        calendar.add(Calendar.SECOND, 59);
        calendar.add(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date addSeconds(int seconds, Date date) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    private static class CalendarThreadLocal extends ThreadLocal<Calendar> {
        @Override
        protected Calendar initialValue() {
            return ProductData.UTC.createCalendar();
        }
    }
}
