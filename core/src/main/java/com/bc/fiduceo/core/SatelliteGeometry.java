
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
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


import com.bc.fiduceo.math.TimeAxis;
import com.vividsolutions.jts.geom.Geometry;

public class SatelliteGeometry {

    private final Geometry geometry;
    private final TimeAxis[] timeAxes;
    private int timeAxisStartIndex;
    private int timeAxisEndIndex;

    public SatelliteGeometry(Geometry geometry, TimeAxis[] timeAxes) {
        this.geometry = geometry;
        this.timeAxes = timeAxes;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public TimeAxis[] getTimeAxes() {
        return timeAxes;
    }

    public int getTimeAxisStartIndex() {
        return timeAxisStartIndex;
    }

    public void setTimeAxisStartIndex(int timeAxisStartIndex) {
        this.timeAxisStartIndex = timeAxisStartIndex;
    }

    public int getTimeAxisEndIndex() {
        return timeAxisEndIndex;
    }

    public void setTimeAxisEndIndex(int timeAxisEndIndex) {
        this.timeAxisEndIndex = timeAxisEndIndex;
    }
}
