
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

package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;

import java.util.ArrayList;

class S2Polygon implements Polygon {

    private final com.google.common.geometry.S2Polygon googlePolygon;

    S2Polygon(Object geometry) {
        this.googlePolygon = (com.google.common.geometry.S2Polygon) geometry;
    }

    @Override
    public Geometry intersection(Geometry other) {
        final com.google.common.geometry.S2Polygon intersection = new com.google.common.geometry.S2Polygon();
        intersection.initToIntersection(googlePolygon, (com.google.common.geometry.S2Polygon) other.getInner());
        return new S2Polygon(intersection);
    }

    @Override
    public boolean isEmpty() {
        return googlePolygon.numLoops() == 0;
    }

    @Override
    public void shiftLon(double lon) {
        throw new RuntimeException("not implemented");
    }


    @Override
    public Point[] getCoordinates() {
        final ArrayList<Point> coordinates = new ArrayList<>();
        final int numLoops = googlePolygon.numLoops();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                final com.google.common.geometry.S2Point googlePoint = loop.vertex(k);
                coordinates.add(new S2Point(new S2LatLng(googlePoint)));
            }
        }
        // @todo 2 tb/** the S2 loops do not contain the closing point. Check if we need to add this point here.
        // check what happens when the polygon contains more than one loop tb 2016-01-27
        return coordinates.toArray(new Point[coordinates.size()]);
    }

    @Override
    public String toString() {
        return googlePolygon.toString();
    }

    @Override
    public Object getInner() {
        return googlePolygon;
    }
}
