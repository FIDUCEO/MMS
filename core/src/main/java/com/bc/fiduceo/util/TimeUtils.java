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
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    private static final String YEAR_DOY_PATTERN = "yyyy-DDD";

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
        return parse(dateString, YEAR_DOY_PATTERN);
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

    public static String formatToDOY(Date date) {
        return format(date, YEAR_DOY_PATTERN);
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
