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
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author muhammad.bc
 */
class BcS2MultiLineString implements LineString {

    private List<S2Polyline> s2PolylineList;


    public BcS2MultiLineString(List<S2Polyline> s2Polylines) {
        this.s2PolylineList = s2Polylines;
    }

    static BcS2MultiLineString createFrom(List<BcS2LineString> lineStringList) {
        final List<S2Polyline> googlePolyLineList = new ArrayList<>();
        for (final BcS2LineString lineString : lineStringList) {
            googlePolyLineList.add((S2Polyline) lineString.getInner());
        }

        return new BcS2MultiLineString(googlePolyLineList);
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isEmpty() {
        boolean isInitialPointsZero = false;

        if (s2PolylineList.isEmpty()) {
            return true;
        }

        for (S2Polyline s2Polyline : s2PolylineList) {
            if (s2Polyline == null) {
                return true;
            }
            int numVertices = s2Polyline.numVertices();
            if (numVertices > 0) {
                for (int i = 0; i < numVertices; i++) {
                    S2Point vertex = s2Polyline.vertex(i);
                    isInitialPointsZero = vertex.getX() == 0 && vertex.getY() == 0 && vertex.getZ() == 0;
                    if (!isInitialPointsZero){
                        return false;
                    }
                }
            }
        }
        return isInitialPointsZero;
    }

    @Override
    public boolean isValid() {
        boolean valid = false;
        for (S2Polyline s2Polyline : s2PolylineList) {
            int numVertices = s2Polyline.numVertices();
            ArrayList<S2Point> s2PointArrayList = new ArrayList<>();
            for (int i = 0; i < numVertices; i++) {
                s2PointArrayList.add(s2Polyline.vertex(i));
            }
            valid = s2Polyline.isValid(s2PointArrayList);
        }
        return valid;
    }

    @Override
    public Point[] getCoordinates() {
        List<Point> pointList = new ArrayList<>();
        for (S2Polyline s2Polyline : s2PolylineList) {
            int i = s2Polyline.numVertices();
            for (int j = 0; j < i; j++) {
                pointList.add(new BcS2Point(new S2LatLng(s2Polyline.vertex(j))));
            }
        }
        return pointList.toArray(new Point[pointList.size()]);
    }

    @Override
    public Object getInner() {
        return s2PolylineList;
    }
}
