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

package com.bc.fiduceo.reader.insitu.ocean_rain;

class Line {

    private final float lon;
    private final float lat;
    private final int time;
    private final float sst;

    Line(float lon, float lat, int time, float sst) {
        this.lon = lon;
        this.lat = lat;
        this.time = time;
        this.sst = sst;
    }

    /**
     * Retrieves the measurement location longitude in decimal degrees (-180 -> 180)
     *
     * @return the location longitude
     */
    float getLon() {
        return lon;
    }

    /**
     * Retrieves the measurement location latitude in decimal degrees (-90 -> 90)
     *
     * @return the location latitude
     */
    public float getLat() {
        return lat;
    }

    /**
     * Retrieves the measurement acquisition time in seconds since 1970 UTC
     *
     * @return the acquisition time
     */
    public int getTime() {
        return time;
    }

    /**
     * Retrieves the measurement sea surface temperature in degrees
     *
     * @return the sea surface temperature
     */
    public float getSst() {
        return sst;
    }
}
