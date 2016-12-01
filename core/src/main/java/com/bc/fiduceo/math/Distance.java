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
 */
package com.bc.fiduceo.math;

import org.esa.snap.core.util.math.RsMathUtils;
import org.esa.snap.core.util.math.SphericalDistance;

public class Distance {

    private static final double MEAN_EARTH_RADIUS_IN_KM = RsMathUtils.MEAN_EARTH_RADIUS * 0.001;

    public static double computeSpericalDistanceKm(double p_lon, double p_lat, double s_lon, double s_lat) {
        final SphericalDistance sphericalDistance = new SphericalDistance(p_lon, p_lat);
        final double radDistance = sphericalDistance.distance(s_lon, s_lat);
        return radDistance * MEAN_EARTH_RADIUS_IN_KM;
    }
}
