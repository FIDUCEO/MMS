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

public class SphericalDistance implements DistanceMeasure {

    private final double lon;
    private final double lat;
    private final double si;
    private final double co;

    /**
     * Creates a new instance of this class.
     *
     * @param lon The reference longitude of this point_distance calculator.
     * @param lat The reference latitude of this point_distance calculator.
     */
    public SphericalDistance(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
        this.si = Math.sin(Math.toRadians(lat));
        this.co = Math.cos(Math.toRadians(lat));
    }

    /**
     * Returns the spherical point_distance (in Radian) of a given (lon, lat) point to
     * the reference (lon, lat) point.
     *
     * @param lon The longitude.
     * @param lat The latitude.
     * @return the spherical point_distance (in Radian) of the given (lon, lat) point
     * to the reference (lon, lat) point.
     */
    @Override
    public double distance(double lon, double lat) {
        if (lon == this.lon && lat == this.lat) {
            return 0.0; //avoids numerical imprecisions when calculating the acos() tb 2017-05-03
        }
        final double phi = Math.toRadians(lat);
        return Math.acos(si * Math.sin(phi) + co * Math.cos(phi) * Math.cos(Math.toRadians(lon - this.lon)));
    }
}
