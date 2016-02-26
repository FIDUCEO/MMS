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
import com.bc.fiduceo.geometry.MultiPolygon;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author muhammad.bc
 */
class S2MultiPolygon implements MultiPolygon {
    private List<Polygon> polygonList;


    @SuppressWarnings("unchecked")
    public S2MultiPolygon(List<Polygon> polygonList) {
        this.polygonList = polygonList;
    }


    @Override
    public Geometry getIntersection(Geometry other) {
        if (other instanceof S2MultiLineString) {
            return intersectS2MultiLineString((S2MultiLineString) other);
        } else if (other instanceof S2Polygon) {
            return intersectS2MultiPolygon((S2Polygon) other);
        }
        throw new RuntimeException("Intersection for geometry type not implemented: " + other.toString());
    }


    @Override
    public boolean isEmpty() {
        final boolean listEmpty = polygonList.isEmpty();
        if (listEmpty) {
            return true;
        }

        for (Polygon polygon : polygonList) {
            if (!polygon.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Point[] getCoordinates() {
        final List<Point> pointList = new ArrayList<>();
        for (Polygon polygon : polygonList) {
            final List<Point> polygonPointList = Arrays.asList(polygon.getCoordinates());
            pointList.addAll(polygonPointList);

        }
        return pointList.toArray(new Point[pointList.size()]);
    }


    @Override
    public Object getInner() {
        return polygonList;
    }

    @SuppressWarnings("unchecked")
    private Geometry intersectS2MultiLineString(S2MultiLineString other) {
        List<S2LineString> lineStrings = new ArrayList<>();
        for (Polygon polygon : polygonList) {
            final S2MultiLineString intersection = (S2MultiLineString) polygon.getIntersection(other);
            if (!intersection.isEmpty()) {
                final List<S2Polyline> inner = (List<S2Polyline>) intersection.getInner();
                for (final S2Polyline polyline : inner) {
                    lineStrings.add(new S2LineString(polyline));
                }
            }
        }
        return S2MultiLineString.createFrom(lineStrings);
    }

    private Geometry intersectS2MultiPolygon(S2Polygon other) {
        List<Polygon> resultList = new ArrayList<>();
        for (Polygon s2Polygon : polygonList) {

            final Polygon intersection = (Polygon) s2Polygon.getIntersection(other);
            if (!intersection.isEmpty()) {
                resultList.add(intersection);
            }
        }

        if (resultList.size() == 1) {
            return new S2Polygon(resultList.get(0).getInner());
        }
        return new S2MultiPolygon(resultList);
    }
}
