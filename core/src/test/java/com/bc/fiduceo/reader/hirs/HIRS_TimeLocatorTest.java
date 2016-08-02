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

package com.bc.fiduceo.reader.hirs;


import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HIRS_TimeLocatorTest {

    private static final int[] TIME_ARRAY = new int[] {606118157, 606118163, 606118170, 606118176, 606118183, 606118189, 606118195, 606118202};
    private HIRS_TimeLocator timeLocator;

    @Before
    public void setUp() {
        final Array timeArray = Array.factory(TIME_ARRAY);
        timeLocator = new HIRS_TimeLocator(timeArray);
    }

    @Test
    public void testGetTime() {
        assertEquals(606118163000L, timeLocator.getTimeFor(10, 1));
        assertEquals(606118195000L, timeLocator.getTimeFor(100, 6));
        assertEquals(606118202000L, timeLocator.getTimeFor(110, 7));
    }

    @Test
    public void testGetTime_outsideArray() {
        try {
            timeLocator.getTimeFor(0, 9);
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
