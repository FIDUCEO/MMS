/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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

package com.bc.fiduceo.math;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TimeIntervalTest {

    @Test
    public void testCreate_oneTime() {
        final ArrayList<Date> dates = new ArrayList<>();
        dates.add(new Date(2000));

        final TimeInterval interval = TimeInterval.create(dates);
        assertTimeInterval(2000, 2000, interval);
    }

    @Test
    public void testCreate_twoTimes() {
        final ArrayList<Date> dates = new ArrayList<>();
        dates.add(new Date(2000));
        dates.add(new Date(2500));

        final TimeInterval interval = TimeInterval.create(dates);
        assertTimeInterval(2000, 2500, interval);
    }

    @Test
    public void testCreate_mixedTimes() {
        final ArrayList<Date> dates = new ArrayList<>();
        dates.add(new Date(2000)); // <-- min
        dates.add(new Date(2500));
        dates.add(new Date(2128));
        dates.add(new Date(2278));
        dates.add(new Date(2612));
        dates.add(new Date(3000)); // <-- max
        dates.add(new Date(2987));

        final TimeInterval interval = TimeInterval.create(dates);
        assertTimeInterval(2000, 3000, interval);
    }

    @Test
    public void testIntersect_noIntersection() {
        final TimeInterval interval = createInterval(1000, 1500);
        final TimeInterval other_interval = createInterval(2000, 2500);

        final TimeInterval intersection = interval.intersect(other_interval);
        assertNull(intersection);
    }

    @Test
    public void testIntersect_otherIntersectsAtStart() {
        final TimeInterval interval = createInterval(1000, 1500);
        final TimeInterval other_interval = createInterval(800, 1300);

        final TimeInterval intersection = interval.intersect(other_interval);
        assertTimeInterval(1000, 1300, intersection);
    }

    @Test
    public void testIntersect_otherIntersectsAtEnd() {
        final TimeInterval interval = createInterval(1000, 1500);
        final TimeInterval other_interval = createInterval(1200, 1800);

        final TimeInterval intersection = interval.intersect(other_interval);
        assertTimeInterval(1200, 1500, intersection);
    }

    @Test
    public void testIntersect_otherCompletelyContained() {
        final TimeInterval interval = createInterval(1000, 1500);
        final TimeInterval other_interval = createInterval(1200, 1300);

        final TimeInterval intersection = interval.intersect(other_interval);
        assertTimeInterval(1200, 1300, intersection);
    }

    @Test
    public void testIntersect_otherCompletelyOverlaps() {
        final TimeInterval interval = createInterval(1000, 1500);
        final TimeInterval other_interval = createInterval(800, 2000);

        final TimeInterval intersection = interval.intersect(other_interval);
        assertTimeInterval(1000, 1500, intersection);
    }

    @Test
    public void testSplit_half() {
        final TimeInterval interval = createInterval(1000, 2000);

        final TimeInterval[] splits = interval.split(2);
        assertEquals(2, splits.length);
        assertTimeInterval(1000, 1500, splits[0]);
        assertTimeInterval(1500, 2000, splits[1]);
    }

    @Test
    public void testSplit_third() {
        final TimeInterval interval = createInterval(1000, 2000);

        final TimeInterval[] splits = interval.split(3);
        assertEquals(3, splits.length);
        assertTimeInterval(1000, 1333, splits[0]);
        assertTimeInterval(1333, 1666, splits[1]);
        assertTimeInterval(1666, 2000, splits[2]);
    }

    @Test
    public void testSplit_quarter() {
        final TimeInterval interval = createInterval(1000, 2000);

        final TimeInterval[] splits = interval.split(4);
        assertEquals(4, splits.length);
        assertTimeInterval(1000, 1250, splits[0]);
        assertTimeInterval(1250, 1500, splits[1]);
        assertTimeInterval(1500, 1750, splits[2]);
        assertTimeInterval(1750, 2000, splits[3]);
    }

    private void assertTimeInterval(int expectedStart, int expectedEnd, TimeInterval intersection) {
        assertEquals(expectedStart, intersection.getStartTime().getTime());
        assertEquals(expectedEnd, intersection.getStopTime().getTime());
    }

    private TimeInterval createInterval(int start, int stop) {
        return new TimeInterval(new Date(start), new Date(stop));
    }
}
