
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
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;

class JTSPolygon implements Polygon {

    private final com.vividsolutions.jts.geom.Polygon jtsPolygon;

    JTSPolygon(com.vividsolutions.jts.geom.Polygon jtsPolygon) {
        this.jtsPolygon = jtsPolygon;
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        final com.vividsolutions.jts.geom.Polygon intersection = (com.vividsolutions.jts.geom.Polygon) jtsPolygon.intersection((com.vividsolutions.jts.geom.Geometry) other.getInner()).clone();
        return new JTSPolygon(intersection);
    }

    @Override
    public Polygon getDifference(Polygon polygon) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Polygon getUnion(Polygon polygon) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void shiftLon(double lon) {
        jtsPolygon.apply(new LonShifter(lon));
    }

    @Override
    public Point[] getCoordinates() {
        final Coordinate[] jtsCoordinates = jtsPolygon.getCoordinates();
        final Point[] coordinates = new Point[jtsCoordinates.length];
        for (int i = 0; i < jtsCoordinates.length; i++) {
            coordinates[i] = new JTSPoint(jtsCoordinates[i]);
        }
        return coordinates;
    }

    @Override
    public boolean isEmpty() {
        return jtsPolygon.isEmpty();
    }

    @Override
    public String toString() {
        return jtsPolygon.toString();
    }

    @Override
    public Object getInner() {
        return jtsPolygon;
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("not implemented");
    }
}
