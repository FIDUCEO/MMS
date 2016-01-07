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

package com.bc.fiduceo.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SamplingPointTest {

    private SamplingPoint samplingPoint;

    @Before
    public void setUp() {
        samplingPoint = new SamplingPoint();
    }

    @Test
    public void testSetGetLon() {
        final double lon_1 = -126.45;
        final double lon_2 = 19.88;

        samplingPoint.setLon(lon_1);
        assertEquals(lon_1, samplingPoint.getLon(), 1e-8);

        samplingPoint.setLon(lon_2);
        assertEquals(lon_2, samplingPoint.getLon(), 1e-8);
    }

    @Test
    public void testSetGetLat() {
        final double lat_1 = -37.56;
        final double lat_2 = 21.92;

        samplingPoint.setLat(lat_1);
        assertEquals(lat_1, samplingPoint.getLat(), 1e-8);

        samplingPoint.setLat(lat_2);
        assertEquals(lat_2, samplingPoint.getLat(), 1e-8);
    }

    @Test
    public void testSetGetTime() {
        final long time_1 = 3346657L;
        final long time_2 = 77256253883L;

        samplingPoint.setTime(time_1);
        assertEquals(time_1, samplingPoint.getTime());

        samplingPoint.setTime(time_2);
        assertEquals(time_2, samplingPoint.getTime());
    }

    @Test
    public void testParametrizedConstruction() {
        final double lon = -108.33;
        final double lat = 56.2;
        final long time = 116677288276L;
        final SamplingPoint samplingPoint = new SamplingPoint(lon, lat, time);

        assertEquals(lon, samplingPoint.getLon(), 1e-8);
        assertEquals(lat, samplingPoint.getLat(), 1e-8);
        assertEquals(time, samplingPoint.getTime());
    }

    @Test
    public void testDefaultConstruction() {
        assertTrue(Double.isNaN(samplingPoint.getLon()));
        assertTrue(Double.isNaN(samplingPoint.getLat()));
        assertEquals(Long.MIN_VALUE, samplingPoint.getTime());
    }
}
