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
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author muhammad.bc
 */
class S2MultiPolygon implements MultiPolygon {
    private List<com.google.common.geometry.S2Polygon> polygonList;
    private List<Point> pointList;


    @SuppressWarnings("unchecked")
    public S2MultiPolygon(Object object) {
        pointList = new ArrayList<>();
        final ArrayList s2PolygonList = (ArrayList) object;
        if (s2PolygonList != null) {
            if (s2PolygonList.size() != 0) {
                if (s2PolygonList.get(0) instanceof S2Polygon) {
                    for (S2Polygon s2Polygon : (List<S2Polygon>) object) {
                        pointList.addAll(Arrays.asList(s2Polygon.getCoordinates()));
                    }
                }
            }
        }
        this.polygonList = (List<com.google.common.geometry.S2Polygon>) object;
    }


    @Override
    public Geometry getIntersection(Geometry other) {
        if (other instanceof S2MultiLineString) {
            return intersectS2MultiLineString(other);
        } else if (other.getInner() instanceof com.google.common.geometry.S2Polygon) {
            return intersectS2MultiPolygon(other);
        }
        throw new RuntimeException("Intersection for geometry type not implemented: " + other.toString());
    }


    @Override
    public boolean isEmpty() {
        return polygonList.isEmpty();
    }

    @Override
    public Point[] getCoordinates() {
        if (pointList.size() > 0) {
            return pointList.toArray(new Point[pointList.size()]);
        }
        for (com.google.common.geometry.S2Polygon s2Polygon : polygonList) {
            int i = s2Polygon.numLoops();
            ArrayList<Point> s2Points = S2Polygon.createS2Points(i, s2Polygon);
            pointList.addAll(s2Points);
        }
        return pointList.toArray(new Point[pointList.size()]);
    }


    @Override
    public Object getInner() {
        return polygonList;
    }

    @SuppressWarnings("unchecked")
    private Geometry intersectS2MultiLineString(Geometry other) {
        List<S2Polyline> s2PolylineList = new ArrayList<>();
        for (com.google.common.geometry.S2Polygon s2Polygon : polygonList) {
            List<S2Polyline> s2Polylines_S2MultiLines = (List<S2Polyline>) other.getInner();
            for (S2Polyline s2Polyline : s2Polylines_S2MultiLines) {
                s2PolylineList.addAll(s2Polygon.intersectWithPolyLine(s2Polyline));
            }
        }
        return new S2MultiLineString(s2PolylineList);
    }

    private Geometry intersectS2MultiPolygon(Geometry other) {
        List<com.google.common.geometry.S2Polygon> resultList = new ArrayList<>();
        for (com.google.common.geometry.S2Polygon s2Polygon : polygonList) {

            com.google.common.geometry.S2Polygon intersection = new com.google.common.geometry.S2Polygon();
            intersection.initToIntersection(s2Polygon, (com.google.common.geometry.S2Polygon) other.getInner());
            if (intersection.numLoops() != 0) {
                resultList.add(intersection);
            }
        }

        if (resultList.size() == 1) {
            return new S2Polygon(resultList.get(0));
        }
        return new S2MultiPolygon(resultList);
    }
}
