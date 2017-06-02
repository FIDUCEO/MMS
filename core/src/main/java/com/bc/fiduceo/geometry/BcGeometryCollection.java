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

package com.bc.fiduceo.geometry;


import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BcGeometryCollection implements GeometryCollection {

    private Geometry[] geometries;

    BcGeometryCollection() {
        geometries = new Geometry[0];
    }

    @Override
    public Geometry[] getGeometries() {
        return geometries;
    }

    @Override
    public void setGeometries(Geometry[] geometries) {
        this.geometries = geometries;
    }

    @Override
    public Geometry getIntersection(Geometry other) {
        final List<Geometry> intersections = new ArrayList<>();
        for (Geometry geometry : geometries) {
            final Geometry intersection = geometry.getIntersection(other);
            if (intersection!= null) {
                intersections.add(intersection);
            }
        }
        if (intersections.size()==1) {
            return intersections.get(0);
        } else {
            final BcGeometryCollection bcGeometryCollection = new BcGeometryCollection();
            bcGeometryCollection.setGeometries(intersections.toArray(new Geometry[intersections.size()]));
            return bcGeometryCollection;
        }
    }

    @Override
    public boolean isEmpty() {
        for (Geometry geometry : geometries) {
            if (!geometry.isEmpty()){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValid() {
        if (geometries.length == 0) {
            return false;
        }

        for (Geometry geometry : geometries) {
            if (!geometry.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Point[] getCoordinates() {
        List<Point> pointsList = new ArrayList<>();
        for (Geometry geometry : geometries) {
            pointsList.addAll(Arrays.asList(geometry.getCoordinates()));
        }
        return pointsList.toArray(new Point[pointsList.size()]);
    }

    @Override
    public Object getInner() {
        return geometries;
    }
}
