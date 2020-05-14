/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader;

import com.bc.fiduceo.reader.TimeLocator_TAI1993Vector;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;

public class TimeLocator_TAI1993VectorTest {

    @Test
    public void testGetTimeFor() {
        final double[] timeData = {1.8, 2.9, 3.0, 4.2, 5.2, 6.3};
        final Array timeDataArray = NetCDFUtils.create(timeData);

        final TimeLocator_TAI1993Vector timeLocator = new TimeLocator_TAI1993Vector(timeDataArray);

        assertEquals(725846374800L, timeLocator.getTimeFor(0, 0));
        assertEquals(725846374800L, timeLocator.getTimeFor(5, 0));
        assertEquals(725846379300L, timeLocator.getTimeFor(8, 5));
    }
}
