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

package com.bc.fiduceo.reader.iasi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IASI_TimeLocatorTest {

    @Test
    public void testGetTime() {
        final long[][] times = {{1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}};

        final IASI_TimeLocator timeLocator = new IASI_TimeLocator(times);

        assertEquals(1, timeLocator.getTimeFor(0, 0));
        assertEquals(1, timeLocator.getTimeFor(1, 0));

        assertEquals(6, timeLocator.getTimeFor(3, 2));
        assertEquals(7, timeLocator.getTimeFor(4, 2));

        assertEquals(11, timeLocator.getTimeFor(4, 5));
        assertEquals(11, timeLocator.getTimeFor(5, 5));
    }
}
