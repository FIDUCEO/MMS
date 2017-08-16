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

package com.bc.fiduceo.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleTest {

    @Test
    public void testConstructionAndGetter() {
        final Sample sample = new Sample(1, 2, 3.0, 4.0, 5);

        assertEquals(1, sample.getX());
        assertEquals(2, sample.getY());
        assertEquals(3.0, sample.getLon(), 1e-8);
        assertEquals(4.0, sample.getLat(), 1e-8);
        assertEquals(5, sample.getTime());
    }
}
