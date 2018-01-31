
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
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

class BcS2Polygon implements Polygon {

    private final S2Polygon googlePolygon;

    BcS2Polygon(Object geometry) {
        this.googlePolygon = (S2Polygon) geometry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Geometry getIntersection(Geometry other) {
        if (other instanceof BcS2Polygon) {
            return intersectPolygon(other);
        } else if (other instanceof BcS2MultiPolygon) {
            return intersectMultiPolygon(other);
        } else if (other instanceof BcS2LineString) {
            return intersectLineString(other);
        } else if (other instanceof BcS2MultiLineString) {
            return intersectMultiLineString(other);
        } else if (other instanceof BcS2Point) {
            return intersectPoint(other);
        }

        throw new RuntimeException("intersection type not implemented");
    }

    @Override
    public Polygon getDifference(Polygon polygon) {
        final S2Polygon difference = new S2Polygon();
        difference.initToDifference(googlePolygon, (S2Polygon) polygon.getInner());
        return new BcS2Polygon(difference);
    }

    @Override
    public Polygon getUnion(Polygon polygon) {
        final S2Polygon union = new S2Polygon();
        union.initToUnion(googlePolygon, (S2Polygon) polygon.getInner());
        return new BcS2Polygon(union);
    }

    @Override
    public Point getCentroid() {
        return BcS2Point.createFrom(googlePolygon.getCentroid());
    }

    @Override
    public boolean contains(Geometry geometry) {
        final Object inner = geometry.getInner();
        if (inner instanceof S2LatLng) {
            final S2Point point = ((S2LatLng) inner).toPoint();
            boolean contains = googlePolygon.contains(point);

            if (!contains) {
                contains = isVertexPoint(point);
            }

            return contains;
        }
        throw new RuntimeException("contains for geometry type not implemented");
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
    public boolean isValid() {
        final int numLoops = googlePolygon.numLoops();
        final ArrayList<S2Loop> loops = new ArrayList<>();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            if (!loop.isValid()) {
                return false;
            }
            loops.add(loop);
        }

        return S2Polygon.isValid(loops);
    }

    @Override
    public Point[] getCoordinates() {
        final List<Point> pointList = extractPoints(googlePolygon);
        // @todo 2 tb/** the S2 loops do not contain the closing point. Check if we need to add this point here.
        // check what happens when the polygon contains more than one loop tb 2016-01-27
        return pointList.toArray(new Point[pointList.size()]);
    }

    @Override
    public String toString() {
        return googlePolygon.toString();
    }

    @Override
    public Object getInner() {
        return googlePolygon;
    }

    static ArrayList<Point> extractPoints(S2Polygon googlePolygon) {
        final ArrayList<Point> coordinates = new ArrayList<>();
        final int numLoops = googlePolygon.numLoops();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                final S2Point googlePoint = loop.vertex(k);
                coordinates.add(BcS2Point.createFrom(googlePoint));
            }

            // close loop - outside world expects this tb 2016-03-03
            final S2Point googlePoint = loop.vertex(0);
            coordinates.add(BcS2Point.createFrom(googlePoint));
        }
        return coordinates;
    }

    private boolean isVertexPoint(S2Point point) {
        final int numLoops = googlePolygon.numLoops();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = googlePolygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                final S2Point vertex = loop.vertex(k);
                if (vertex.equals(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Geometry intersectPoint(Geometry other) {
        final S2LatLng inner = (S2LatLng) other.getInner();
        if (googlePolygon.contains(inner.toPoint())) {
            return other;
        } else {
            return BcS2Point.createEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private Geometry intersectMultiLineString(Geometry other) {
        List<S2Polyline> s2PolylineList = (List<S2Polyline>) other.getInner();
        List<S2Polyline> intersectionResult = new ArrayList<>();
        for (final S2Polyline s2Polyline : s2PolylineList) {
            intersectionResult.addAll(googlePolygon.intersectWithPolyLine(s2Polyline));
        }
        return new BcS2MultiLineString(intersectionResult);
    }

    private Geometry intersectLineString(Geometry other) {
        final S2Polyline s2Polyline = (S2Polyline) other.getInner();
        final List<S2Polyline> intersection = googlePolygon.intersectWithPolyLine(s2Polyline);
        return new BcS2MultiLineString(intersection);
    }

    @SuppressWarnings("unchecked")
    private Geometry intersectMultiPolygon(Geometry other) {
        final List<S2Polygon> polygonList = (List<S2Polygon>) other.getInner();
        List<S2Polygon> intersectionResult = new ArrayList<>();
        for (final S2Polygon polygon : polygonList) {
            final S2Polygon intersection = new S2Polygon();
            intersection.initToIntersection(googlePolygon, polygon);
            if (intersection.numLoops() != 0) {
                intersectionResult.add(intersection);
            }
        }
        if (intersectionResult.size() == 1) {
            return new BcS2Polygon(intersectionResult.get(0));
        } else {
            final List<Polygon> bcPolygonList = new ArrayList<>();
            for (final S2Polygon polygon : intersectionResult) {
                bcPolygonList.add(new BcS2Polygon(polygon));
            }
            return new BcS2MultiPolygon(bcPolygonList);
        }
    }

    private Geometry intersectPolygon(Geometry other) {
        final S2Polygon intersection = new S2Polygon();
        intersection.initToIntersection(googlePolygon, (S2Polygon) other.getInner());
        return new BcS2Polygon(intersection);
    }
}
