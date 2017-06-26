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


import com.bc.fiduceo.geometry.BcGeometryCollection;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.geometry.s2.S2WKTWriter;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;

class BcS2LineString implements LineString {

    private final S2Polyline googleLineString;

    BcS2LineString(S2Polyline googleLineString) {
        this.googleLineString = googleLineString;
    }

    @Override
    public String toString() {
        return S2WKTWriter.write(googleLineString);
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        if (other instanceof BcS2Point) {
            return intersectWithPoint((BcS2Point) other);
        } else if (other instanceof BcS2LineString) {
            return intersectWithLineString((BcS2LineString) other);
        }if (other instanceof BcS2Polygon) {
            return other.getIntersection(this);
        }

        throw new RuntimeException("Unsupported intersection type");
    }

    @Override
    public boolean isEmpty() {
        return googleLineString.numVertices() <= 0;
    }

    @Override
    public boolean isValid() {
        int numVertices = googleLineString.numVertices();
        ArrayList<S2Point> s2PointArrayList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            s2PointArrayList.add(googleLineString.vertex(i));
        }
        return googleLineString.isValid(s2PointArrayList);
    }

    @Override
    public Point[] getCoordinates() {
        final int numVertices = googleLineString.numVertices();
        final Point[] coordinates = new Point[numVertices];
        for (int i = 0; i < numVertices; i++) {
            final S2Point googlePoint = googleLineString.vertex(i);
            coordinates[i] = BcS2Point.createFrom(googlePoint);
        }
        return coordinates;
    }

    @Override
    public Object getInner() {
        return googleLineString;
    }

    private Geometry intersectWithPoint(BcS2Point other) {
        final S2LatLng inner = (S2LatLng) other.getInner();
        final S2Point intersects = googleLineString.intersects(inner.toPoint());
        if (intersects != null) {
            return new BcS2Point(new S2LatLng(intersects));
        }
        return BcS2Point.createEmpty();
    }

    private Geometry intersectWithLineString(BcS2LineString other) {
        final S2Polyline otherInner = (S2Polyline) other.getInner();
        final S2Point[] intersects = googleLineString.intersects(otherInner);

        if (intersects.length == 1) {
            return BcS2Point.createFrom(intersects[0]);
        } else if (intersects.length > 1) {
            final BcGeometryCollection collection = new BcGeometryCollection();
            final BcS2Point[] bcS2Points = new BcS2Point[intersects.length];
            for (int i = 0; i < intersects.length; i++) {
                bcS2Points[i] = BcS2Point.createFrom(intersects[i]);
            }
            collection.setGeometries(bcS2Points);
            return collection;
        } else {
            return BcS2Point.createEmpty();
        }
    }
}
