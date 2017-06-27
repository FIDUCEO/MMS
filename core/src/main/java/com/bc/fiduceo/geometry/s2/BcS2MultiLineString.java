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

import com.bc.fiduceo.geometry.*;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

class BcS2MultiLineString implements MultiLineString {

    private List<S2Polyline> s2PolylineList;


    BcS2MultiLineString(List<S2Polyline> s2Polylines) {
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
        List<Geometry> results;
        if (other instanceof Point) {
            results = intersectPoint(other);
            // do not change order here please, a MultiLineString is a LineString tb 2017-06-26
        } else if (other instanceof MultiLineString) {
            results = intersectMultiLineString(other);
        } else if (other instanceof LineString) {
            results = intersectLineString(other);
        } else {
            throw new RuntimeException("not implemented");
        }

        return assembleResultGeometry(results);
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
                    if (!isInitialPointsZero) {
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
                pointList.add(BcS2Point.createFrom(s2Polyline.vertex(j)));
            }
        }
        return pointList.toArray(new Point[pointList.size()]);
    }

    @Override
    public Object getInner() {
        return s2PolylineList;
    }

    private List<Geometry> intersectPoint(Geometry other) {
        final List<Geometry> results = new ArrayList<>();
        final S2LatLng otherInner = (S2LatLng) other.getInner();
        for (S2Polyline s2Polyline : s2PolylineList) {
            final S2Point intersects = s2Polyline.intersects(otherInner.toPoint());
            if (intersects != null) {
                results.add(new BcS2Point(new S2LatLng(intersects)));
            }
        }
        return results;
    }

    private List<Geometry> intersectLineString(Geometry other) {
        final List<Geometry> results = new ArrayList<>();
        final S2Polyline otherInner = (S2Polyline) other.getInner();
        for (S2Polyline s2Polyline : s2PolylineList) {
            final S2Point[] intersects = s2Polyline.intersects(otherInner);
            if (intersects.length > 0) {
                for (final S2Point intersectingPoint : intersects) {
                    results.add(new BcS2Point(new S2LatLng(intersectingPoint)));
                }
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Geometry> intersectMultiLineString(Geometry other) {
        final List<Geometry> results = new ArrayList<>();
        final List<S2Polyline> otherInner = (List<S2Polyline>) other.getInner();
        for (final S2Polyline s2Polyline : otherInner) {
            final List<Geometry> geometries = intersectLineString(new BcS2LineString(s2Polyline));
            results.addAll(geometries);
        }
        return results;
    }

    // package access for testing only tb 2017-06-27
    static Geometry assembleResultGeometry(List<Geometry> results) {
        if (results.isEmpty()) {
            return BcS2Point.createEmpty();
        } else if (results.size() > 1) {
            final GeometryCollection geometryCollection = new BcGeometryCollection();
            geometryCollection.setGeometries(results.toArray(new Geometry[results.size()]));
            return geometryCollection;
        } else {
            return results.get(0);
        }
    }
}
