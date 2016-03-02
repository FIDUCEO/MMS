
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

package com.bc.fiduceo.geometry.jts;


import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.vividsolutions.jts.geom.Coordinate;

class JTSLineString implements LineString {

    private final com.vividsolutions.jts.geom.LineString jtsLineString;

    JTSLineString(com.vividsolutions.jts.geom.LineString jtsLineString) {
        this.jtsLineString = jtsLineString;
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isEmpty() {
        return jtsLineString.isEmpty();
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Point[] getCoordinates() {
        final Coordinate[] coordinates = jtsLineString.getCoordinates();
        final Point[] points = new Point[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            final Coordinate coordinate = coordinates[i];
            points[i] = new JTSPoint(coordinate);
        }
        return points;
    }

    @Override
    public String toString() {
        return jtsLineString.toString();
    }

    @Override
    public Object getInner() {
        return jtsLineString;
    }
}
