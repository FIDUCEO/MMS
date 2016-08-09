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
import java.util.GregorianCalendar;

public class TimeUtils {

    private static final String YEAR_DOY_PATTERN = "yyyy-DDD";

    private static final double EPOCH_MJD2000 = 10957.0;
    private static final double MILLIS_PER_DAY = 86400000.0;

    private static ThreadLocal<Calendar> calendarThreadLocal = new CalendarThreadLocal();

    public static Date create(long millisSinceEpoch) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTimeInMillis(millisSinceEpoch);
        return calendar.getTime();
    }

    /**
     * Creates a date that represents "now" as UTC
     *
     * @return utc-now
     */
    public static Date createNow() {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTime(new Date());
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

    public static Date getBeginOfMonth(Date date) {
        final Calendar utcCalendar = calendarThreadLocal.get();
        utcCalendar.setTime(date);
        utcCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return getBeginningOfDay(utcCalendar.getTime());
    }

    public static Date getEndOfMonth(Date date) {
        final Calendar utcCalendar = calendarThreadLocal.get();
        final int maxDay = utcCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        utcCalendar.setTime(date);
        utcCalendar.set(Calendar.DAY_OF_MONTH, maxDay);
        return getEndOfDay(utcCalendar.getTime());
    }

    public static Date getBeginningOfDay(Date day) {
        return calendarDayOf(day).getTime();
    }

    public static Date getEndOfDay(Date day) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    public static Date mjd2000ToDate(double mjd2000) {
        return new Date(Math.round((EPOCH_MJD2000 + mjd2000) * MILLIS_PER_DAY));
    }

    private static class CalendarThreadLocal extends ThreadLocal<Calendar> {
        @Override
        protected Calendar initialValue() {
            return ProductData.UTC.createCalendar();
        }
    }

    private static Calendar calendarDayOf(Date time) {
        final Calendar calendar = calendarThreadLocal.get();
        calendar.setTime(time);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar;
    }
}
