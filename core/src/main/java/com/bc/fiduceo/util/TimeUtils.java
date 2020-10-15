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

    private static final double EPOCH_MJD2000 = 10957.0;
    private static final double MILLIS_PER_DAY = 86400000.0;
    private static final long TAI_REFERENCE_SECONDS = 725846400L;   // 1993-01-01T00:00:00 as UTC since Epoch

    private static ThreadLocal<Calendar> calendarThreadLocal = new CalendarThreadLocal();

    public static final long millisSince1978;
    public static final int secondsSince1978;

    static {
        final Calendar calendar = ProductData.UTC.createCalendar();
        calendar.clear();
        calendar.set(1978, Calendar.JANUARY, 1);
        millisSince1978 = calendar.getTime().getTime();
        secondsSince1978 = (int) (millisSince1978 / 1000);
    }

    private static final long millisSince2000;

    static {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, 2000);
        utcCalendar.set(Calendar.MONTH, 0);
        utcCalendar.set(Calendar.DAY_OF_MONTH, 1);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        millisSince2000 = utcCalendar.getTime().getTime();
    }

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
        utcCalendar.setTime(date);
        final int maxDay = utcCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        utcCalendar.set(Calendar.DAY_OF_MONTH, maxDay);
        utcCalendar.set(Calendar.MILLISECOND, 999);
        utcCalendar.set(Calendar.SECOND, 59);
        utcCalendar.set(Calendar.MINUTE, 59);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 23);
        return utcCalendar.getTime();
    }

    public static Date getBeginningOfDay(Date day) {
        return calendarDayOf(day).getTime();
    }

    public static Date mjd2000ToDate(double mjd2000) {
        return new Date(Math.round((EPOCH_MJD2000 + mjd2000) * MILLIS_PER_DAY));
    }

    public static long millisSince2000ToUnixEpoch(double timeStampSecs2000) {
        long timeStamp = Math.round(timeStampSecs2000 / 1000.0);
        return millisSince2000 + timeStamp;
    }

    public static Date tai1993ToUtc(double taiSeconds) {
        final double utcInstant = tai1993ToUtcInstantSeconds(taiSeconds);
        return new Date((long) (utcInstant * 1000L));
    }

    public static double tai1993ToUtcInstantSeconds(double taiSeconds) {
        final double taiInstant = TAI_REFERENCE_SECONDS + taiSeconds;
        return taiInstant - getTaiToUtcOffset(taiInstant);
    }

    public static Date getDate(int year, int dayOfYear, int millisecsInDay) {
        final Calendar calendar = getUTCCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MILLISECOND, millisecsInDay);
        return calendar.getTime();
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

    static long getTaiToUtcOffset(double taiInstant) {
        if (taiInstant > 1483228800) {
            return 37L;
        } else if (taiInstant > 1435708800) {
            return 36L;
        } else if (taiInstant > 1341100800) {
            return 35L;
        } else if (taiInstant > 1230768000) {
            return 34L;
        } else if (taiInstant > 1136073600) {
            return 33L;
        } else if (taiInstant > 915148800) {
            return 32L;
        } else if (taiInstant > 867715200) {
            return 31L;
        } else if (taiInstant > 820454400) {
            return 30L;
        } else if (taiInstant > 773020800) {
            return 29L;
        } else if (taiInstant > 741484800) {
            return 28L;
        } else if (taiInstant > 709948800) {
            return 27L;
        }

        throw new RuntimeException("unsupported time range");
    }
}
