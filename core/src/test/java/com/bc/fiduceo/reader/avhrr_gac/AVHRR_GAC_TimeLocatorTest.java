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

package com.bc.fiduceo.reader.avhrr_gac;

import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AVHRR_GAC_TimeLocatorTest {

    private static final float[][][] DTIME_DATA = new float[][][]{{{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            {0.5012512f, 0.5012512f, 0.5012512f, 0.5012512f, 0.5012512f, 0.5012512f},
            {0.9990692f, 0.9990692f, 0.9990692f, 0.9990692f, 0.9990692f, 0.9990692f},
            {1.5003204f, 1.5003204f, 1.5003204f, 1.5003204f, 1.5003204f, 1.5003204f},
            {2.0015717f, 2.0015717f, 2.0015717f, 2.0015717f, 2.0015717f, 2.0015717f},
            {2.4993896f, 2.4993896f, 2.4993896f, 2.4993896f, 2.4993896f, 2.4993896f}}};
    private Array dTime;

    @Before
    public void setUp() throws Exception {
        dTime = Array.factory(DTIME_DATA);
    }

    @Test
    public void testGetTime_insideArray() {
        final Date sensingStart = TimeUtils.parseDOYBeginOfDay("2008-214");
        final AVHRR_GAC_TimeLocator timeLocator = new AVHRR_GAC_TimeLocator(dTime, sensingStart);

        final long referenceTime = sensingStart.getTime();

        long pixelTime = timeLocator.getTimeFor(0, 0);
        assertEquals(referenceTime, pixelTime);

        pixelTime = timeLocator.getTimeFor(3, 0);
        assertEquals(referenceTime, pixelTime);

        pixelTime = timeLocator.getTimeFor(4, 1);
        assertEquals(referenceTime + 501, pixelTime);

        pixelTime = timeLocator.getTimeFor(5, 2);
        assertEquals(referenceTime + 999, pixelTime);

        pixelTime = timeLocator.getTimeFor(0, 3);
        assertEquals(referenceTime + 1500, pixelTime);
    }

    @Test
    public void testGetTime_outsideArray() {
        final AVHRR_GAC_TimeLocator timeLocator = new AVHRR_GAC_TimeLocator(dTime, new Date());

        try {
            timeLocator.getTimeFor(12, 2);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }
}
