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

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;
import com.bc.fiduceo.core.TimeRange;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TimeLatMapStrategyTest {

    private TimeLatMapStrategy strategy;

    @Before
    public void setUp() {
        strategy = new TimeLatMapStrategy(800, 400);
    }

    @Test
    public void testInitializeAndMap_onePoint() throws ParseException {
        final SamplingPoint samplingPoint = createPoint("2010-06-02T00:00:00Z", 23.0);
        final List<SamplingPoint> points = new ArrayList<>();
        points.add(samplingPoint);

        strategy.initialize(points);
        final PlotPoint mapPoint = strategy.map(samplingPoint);
        assertNotNull(mapPoint);
        assertEquals(26, mapPoint.getX());
        assertEquals(148, mapPoint.getY());
    }

    @Test
    public void testInitializeAndMap_threePoints() throws ParseException {
        final List<SamplingPoint> points = new ArrayList<>();
        points.add(createPoint("2009-11-08T00:00:00Z", 23.0));
        points.add(createPoint("2009-11-22T00:00:00Z", 34.0));
        points.add(createPoint("2009-11-11T00:00:00Z", -45.0));

        strategy.initialize(points);
        PlotPoint mapPoint = strategy.map(createPoint("2009-11-01T00:00:00Z", -56));
        assertNotNull(mapPoint);
        assertEquals(0, mapPoint.getX());
        assertEquals(324, mapPoint.getY());

        mapPoint = strategy.map(createPoint("2009-12-01T00:00:00Z", -45));
        assertNotNull(mapPoint);
        assertEquals(800, mapPoint.getX());
        assertEquals(300, mapPoint.getY());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testInterfaceImplemented() {
        assertTrue(strategy instanceof MapStrategy);
    }

    @Test
    public void testExtractTimeRangeInFullMoths_onePoint() throws ParseException {
        final List<SamplingPoint> points = new ArrayList<>();
        points.add(createPoint("2011-06-02T00:00:00Z", 19.0)) ;

        final TimeRange timeRange = TimeLatMapStrategy.extractTimeRangeInFullMonths(points);
        assertNotNull(timeRange);
        assertExpectedTime("2011-06-01T00:00:00Z", timeRange.getStartDate());
        assertExpectedTime("2011-07-01T00:00:00Z", timeRange.getStopDate());
    }

    @Test
    public void testExtractTimeRangeInFullMoths_threePoints() throws ParseException {
        final List<SamplingPoint> points = new ArrayList<>();
        points.add(createPoint("2010-05-11T00:00:00Z", 19.0)) ;
        points.add(createPoint("2010-05-08T11:00:00Z", 19.0)) ;
        points.add(createPoint("2010-05-17T14:00:00Z", 19.0)) ;

        final TimeRange timeRange = TimeLatMapStrategy.extractTimeRangeInFullMonths(points);
        assertNotNull(timeRange);
        assertExpectedTime("2010-05-01T00:00:00Z", timeRange.getStartDate());
        assertExpectedTime("2010-06-01T00:00:00Z", timeRange.getStopDate());
    }

    private static void assertExpectedTime(String expected, Date date) throws ParseException {
        assertEquals(TimeUtils.parse(expected, "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime(), date.getTime());
    }

    private static SamplingPoint createPoint(String time, double lat) throws ParseException {
        final long timeMillis = TimeUtils.parse(time, "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime();
        return new SamplingPoint(78, lat, timeMillis);
    }
}
