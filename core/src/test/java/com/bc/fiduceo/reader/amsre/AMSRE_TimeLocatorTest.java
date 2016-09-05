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

package com.bc.fiduceo.reader.amsre;

import org.junit.Test;
import ucar.ma2.Array;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSRE_TimeLocatorTest {

    @Test
    public void testGetTimeFor() {
        final Array timeArray = mock(Array.class);
        when(timeArray.getDouble(21)).thenReturn(3.827722026045922E8);
        when(timeArray.getDouble(284)).thenReturn(3.827725970760162E8);
        when(timeArray.getDouble(1570)).thenReturn(3.8277452593631935E8);

        final AMSRE_TimeLocator timeLocator = new AMSRE_TimeLocator(timeArray);

        assertEquals(1108618570604L, timeLocator.getTimeFor(23, 21));
        assertEquals(1108618965076L, timeLocator.getTimeFor(24, 284));
        assertEquals(1108620893936L, timeLocator.getTimeFor(25, 1570));

    }
}
