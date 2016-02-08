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
import java.util.Calendar;
import java.util.Date;

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

    private static class CalendarThreadLocal extends ThreadLocal<Calendar> {
        @Override
        protected Calendar initialValue() {
            return ProductData.UTC.createCalendar();
        }
    }
}
