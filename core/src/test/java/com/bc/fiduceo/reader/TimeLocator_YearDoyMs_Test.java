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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TimeLocator_YearDoyMs_Test {

    private final int[] YEARS = new int[]{2007, 2007, 2007, 2007, 2007, 2007};
    private final int[] DAYS = new int[]{234, 234, 234, 234, 234, 234};
    private final int[] MILLIS = new int[]{23429119, 23431786, 23434452, 23437119, 23439786, 23442452};

    private TimeLocator_YearDoyMs timeLocator;

    @Before
    public void setUp(){
        final Array scnlinyr = NetCDFUtils.create(YEARS);
        final Array scnlindy = NetCDFUtils.create(DAYS);
        final Array scnlintime = NetCDFUtils.create(MILLIS);

        timeLocator = new TimeLocator_YearDoyMs(scnlinyr, scnlindy, scnlintime);
    }

    @Test
    public void testGetTime() {
        assertEquals(1187764229119L, timeLocator.getTimeFor(1, 0));
        assertEquals(1187764231786L, timeLocator.getTimeFor(2, 1));
        assertEquals(1187764239786L, timeLocator.getTimeFor(3, 4));
    }

    @Test
    public void testGetTime_outsideArray() {
        try {
            timeLocator.getTimeFor(0, 12);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }

        try {
            timeLocator.getTimeFor(0, -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }
}
