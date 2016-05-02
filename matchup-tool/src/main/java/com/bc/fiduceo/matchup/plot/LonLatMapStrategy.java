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

package com.bc.fiduceo.matchup.plot;

import com.bc.fiduceo.core.SamplingPoint;

import java.util.List;

class LonLatMapStrategy implements MapStrategy {

    private final int width;
    private final int height;

    LonLatMapStrategy(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void initialize(List<SamplingPoint> samplingPoints) {
        // nothing to do here
    }

    public PlotPoint map(SamplingPoint samplingPoint) {
        final double x = (samplingPoint.getLon() + 180.0) / 360.0;
        final double y = (90.0 - samplingPoint.getLat()) / 180.0;
        final int i = (int) (y * height);
        final int k = (int) (x * width);
        return new PlotPoint(k, i);
    }
}
