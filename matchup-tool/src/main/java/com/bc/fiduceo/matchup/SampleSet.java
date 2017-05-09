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

package com.bc.fiduceo.matchup;

// todo se multisensor
public class SampleSet {

    private static final int SECONDARY_INDEX = 0;
    private Sample primary;
    private Sample[] secondary;
    private float sphericalDistance;

    public SampleSet() {
        secondary = new Sample[1];
        sphericalDistance = Float.MIN_VALUE;
    }

    public Sample getPrimary() {
        return primary;
    }

    public void setPrimary(Sample primary) {
        this.primary = primary;
    }

    public Sample getSecondary() {
        return secondary[SECONDARY_INDEX];
    }

    public void setSecondary(Sample secondary) {
        this.secondary[SECONDARY_INDEX] = secondary;
    }

    public float getSphericalDistance() {
        return sphericalDistance;
    }

    public void setSphericalDistance(float sphericalDistance) {
        this.sphericalDistance = sphericalDistance;
    }
}
