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

import static org.junit.Assert.assertEquals;
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
    public void testCreateNow(){
        final Date now = TimeUtils.createNow();
        assertNotNull(now);
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
    public void testFormatToDOY(){
        final Date date = TimeUtils.parseDOYBeginOfDay("2007-127");

        final String doyFormatted = TimeUtils.formatToDOY(date);
        assertEquals("2007-127", doyFormatted);
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
    public void testCalculateTheTimeDifferentFrom_1970_01_01() throws Exception {
        Date date;

        date = TimeUtils.parse("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        assertEquals(0, date.getTime());

        date = TimeUtils.parse("1970-01-01 00:30:00", "yyyy-MM-dd HH:mm:ss");
        assertEquals(1800000, date.getTime());

        date = TimeUtils.parse("1969-12-31 23:59:00", "yyyy-MM-dd HH:mm:ss");
        assertEquals(-60000, date.getTime());
    }

    @Test
    public void testGetUTCCalendar() {
        Calendar utcCalendar = TimeUtils.getUTCCalendar();

        assertNotNull(utcCalendar);
        assertEquals("UTC", utcCalendar.getTimeZone().getID());
    }

    @Test
    public void testGetBeginOfMoth() {
        final Date date = TimeUtils.parse("2011-08-19 18:24:53", "yyyy-MM-dd HH:mm:ss");

        final Date begin = TimeUtils.getBeginOfMonth(date);
        TestUtil.assertCorrectUTCDate(2011, 8, 1, 0, 0, 0, begin);
    }

    @Test
    public void testMjd2000ToDate() {
        assertEquals(946684800000L, TimeUtils.mjd2000ToDate(0.0).getTime());
        assertEquals(946771200000L, TimeUtils.mjd2000ToDate(1.0).getTime());
        assertEquals(955324800000L, TimeUtils.mjd2000ToDate(100.0).getTime());
    }
}
