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

package com.bc.fiduceo.math;

import org.esa.snap.core.util.math.DistanceMeasure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SphericalDistanceTest {

    @Test
    public void testDistance() {
        final DistanceMeasure distanceMeasure = new SphericalDistance(0.0, 0.0);

        assertEquals(0.0, distanceMeasure.distance(0.0, 0.0), 0.0);
        assertEquals(1.0, Math.toDegrees(distanceMeasure.distance(1.0, 0.0)), 1.0e-10);
        assertEquals(1.0, Math.toDegrees(distanceMeasure.distance(0.0, 1.0)), 1.0e-10);
    }

    @Test
    public void testDistanceHalfDegree() {
        final double lat = 60.0;
        final DistanceMeasure distanceMeasure = new SphericalDistance(0.0, lat);

        assertEquals(0.0, distanceMeasure.distance(0.0, lat), 0.0);
        assertEquals(0.5, Math.toDegrees(distanceMeasure.distance(1.0, lat)), 1e-5);
    }
}

