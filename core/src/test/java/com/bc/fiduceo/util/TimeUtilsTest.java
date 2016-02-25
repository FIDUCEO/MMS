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

import com.bc.fiduceo.TestUtil;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TimeUtilsTest {

    @Test
    public void testCreate_fromMillisSinceEpoch() {
        Date date = TimeUtils.create(1435000000000L);
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2015, 6, 22, 19, 6, 40, 0, date);

        date = TimeUtils.create(1435100000000L);
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2015, 6, 23, 22, 53, 20, 0, date);
    }

    @Test
    public void testFormat() {
        final Date date = TimeUtils.create(1435000000000L);

        final String formatted = TimeUtils.format(date);
        assertNotNull(formatted);
        assertEquals("22-Jun-2015 19:06:40", formatted);
    }

    @Test
    public void testFormat_withPattern() {
        final Date date = TimeUtils.create(1435000000123L);

        final String formatted = TimeUtils.format(date, "yyyy-MM-dd HH:mm:ss.S");
        assertNotNull(formatted);
        assertEquals("2015-06-22 19:06:40.123", formatted);
    }

    @Test
    public void testParse_withFormat() {
        final Date date = TimeUtils.parse("2014-08-23 18:16:04.334", "yyyy-MM-dd HH:mm:ss.S");

        assertNotNull(date);
        assertEquals("2014-08-23 18:16:04.334", TimeUtils.format(date, "yyyy-MM-dd HH:mm:ss.S"));
    }

    @Test
    public void testParseYearDOYBeginOfDay() {
        Date date = TimeUtils.parseDOYBeginOfDay("2007-124");
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2007, 5, 4, 0, 0, 0, 0, date);

        date = TimeUtils.parseDOYBeginOfDay("2007-125");
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2007, 5, 5, 0, 0, 0, 0, date);
    }

    @Test
    public void testParseYearDOYEndOfDay() {
        Date date = TimeUtils.parseDOYEndOfDay("2007-126");
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2007, 5, 6, 23, 59, 59, 999, date);

        date = TimeUtils.parseDOYEndOfDay("2007-127");
        assertNotNull(date);
        TestUtil.assertCorrectUTCDate(2007, 5, 7, 23, 59, 59, 999, date);
    }

    @Test
    public void testTimeStampToDate() {
        final Timestamp timestamp = new Timestamp(1435000000234L);

        final Date date = TimeUtils.toDate(timestamp);
        assertNotNull(date);
        assertEquals("2015-06-22 19:06:40.234", TimeUtils.format(date, "yyyy-MM-dd HH:mm:ss.S"));
    }

    @Test
    public void testToTimeStamp() {
        final Date date = TimeUtils.create(1435100000345L);

        final Timestamp timestamp = TimeUtils.toTimestamp(date);
        assertEquals(1435100000345L, timestamp.getTime());
    }

    @Test
    public void testAddSeconds() {
        Date date = TimeUtils.parseDOYBeginOfDay("2007-130");
        TestUtil.assertCorrectUTCDate(2007, 5, 10, 0, 0, 0, 0, date);

        Date adjustedDate = TimeUtils.addSeconds(300, date);
        TestUtil.assertCorrectUTCDate(2007, 5, 10, 0, 5, 0, 0, adjustedDate);

        adjustedDate = TimeUtils.addSeconds(-300, date);
        TestUtil.assertCorrectUTCDate(2007, 5, 9, 23, 55, 0, 0, adjustedDate);
    }

    @Test
    public void testGetDayBetween() {
        HashMap<Integer, Integer> daysBetween = TimeUtils.getDaysInterval(TimeUtils.parseDOYBeginOfDay("2015-23"), TimeUtils.parseDOYBeginOfDay("2015-304"), 2);
        assertFalse(daysBetween.isEmpty());
        Object[] startDaysKey = daysBetween.keySet().toArray();
        Object[] endDaysValue = daysBetween.values().toArray();

        assertEquals(startDaysKey[1], 23);
        assertEquals(endDaysValue[1], 163);

        assertEquals(startDaysKey[0], 164);
        assertEquals(endDaysValue[0], 304);
    }

    @Test
    public void testGetDaysBetweenYrs() {
        List<Calendar[]> daysIntervalYear = TimeUtils.getDaysIntervalYear(TimeUtils.parseDOYBeginOfDay("2015-360"), TimeUtils.parseDOYBeginOfDay("2016-4"), 3);
        assertFalse(daysIntervalYear.isEmpty());

        Calendar calendars[] = daysIntervalYear.get(0);

        int dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        int yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 360);
        assertEquals(yearStart, 2015);

        int dayEnd = calendars[1].get(Calendar.DAY_OF_YEAR);
        int yearEnd = calendars[1].get(Calendar.YEAR);
        assertEquals(dayEnd, 363);
        assertEquals(yearEnd, 2015);


        calendars = daysIntervalYear.get(1);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 364);
        assertEquals(yearStart, 2015);

        dayEnd = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearEnd = calendars[1].get(Calendar.YEAR);
        assertEquals(dayEnd, 1);
        assertEquals(yearEnd, 2016);


        calendars = daysIntervalYear.get(2);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 2);
        assertEquals(yearStart, 2016);

        dayEnd = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearEnd = calendars[1].get(Calendar.YEAR);
        assertEquals(dayEnd, 5);
        assertEquals(yearEnd, 2016);
    }

    @Test
    public void testGetDaysBetweenYr() {
        List<Calendar[]> daysIntervalYear = TimeUtils.getDaysIntervalYear(TimeUtils.parseDOYBeginOfDay("2010-360"), TimeUtils.parseDOYBeginOfDay("2016-4"), 10);
        assertFalse(daysIntervalYear.isEmpty());
        assertEquals(daysIntervalYear.size(), 10);

        //----
        Calendar calendars[] = daysIntervalYear.get(0);
        int dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        int yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 360);
        assertEquals(yearStart, 2010);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 178);
        assertEquals(yearStart, 2011);

        //----
        calendars = daysIntervalYear.get(1);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 179);
        assertEquals(yearStart, 2011);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 362);
        assertEquals(yearStart, 2011);

        //----
        calendars = daysIntervalYear.get(2);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 363);
        assertEquals(yearStart, 2011);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 180);
        assertEquals(yearStart, 2012);

        //----
        calendars = daysIntervalYear.get(3);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 181);
        assertEquals(yearStart, 2012);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 364);
        assertEquals(yearStart, 2012);

        //----
        calendars = daysIntervalYear.get(4);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 365);
        assertEquals(yearStart, 2012);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 183);
        assertEquals(yearStart, 2013);

        //----
        calendars = daysIntervalYear.get(5);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 184);
        assertEquals(yearStart, 2013);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 2);
        assertEquals(yearStart, 2014);


        //----
        calendars = daysIntervalYear.get(6);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 3);
        assertEquals(yearStart, 2014);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 186);
        assertEquals(yearStart, 2014);

        //----
        calendars = daysIntervalYear.get(7);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 187);
        assertEquals(yearStart, 2014);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 5);
        assertEquals(yearStart, 2015);

        //----
        calendars = daysIntervalYear.get(8);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 6);
        assertEquals(yearStart, 2015);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 189);
        assertEquals(yearStart, 2015);

        //----
        calendars = daysIntervalYear.get(9);
        dayStart = calendars[0].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[0].get(Calendar.YEAR);
        assertEquals(dayStart, 190);
        assertEquals(yearStart, 2015);

        dayStart = calendars[1].get(Calendar.DAY_OF_YEAR);
        yearStart = calendars[1].get(Calendar.YEAR);
        assertEquals(dayStart, 7);
        assertEquals(yearStart, 2016);

    }

    @Test
    public void getGetName() {
        String fileName = "NSS.AMBX.NK.D15348.S0057.E0250.B9144748.GC.h5";

        Pattern compile = Pattern.compile("'*\\d{5}");
        Matcher matcher = compile.matcher(fileName);
        if (matcher.find()) {
            String group = matcher.group();
            System.out.println("group = " + group);

            String yr = group.substring(0, 2);
            String month = group.substring(2, group.length());

            System.out.println("yr = " + yr);
            System.out.println("month = " + month);


        }
    }
}
