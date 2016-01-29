
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
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class S2GeometryFactory implements AbstractGeometryFactory {

    private final S2WKTReader s2WKTReader;

    public S2GeometryFactory() {
        s2WKTReader = new S2WKTReader();
    }

    @Override
    public Geometry parse(String wkt) {
        final Object geometry = s2WKTReader.read(wkt);
        if (geometry instanceof com.google.common.geometry.S2Polygon) {
            return new S2Polygon(geometry);
        } else if (geometry instanceof com.google.common.geometry.S2Polyline) {
            return new S2LineString((S2Polyline) geometry);
        } else if (geometry instanceof com.google.common.geometry.S2Point) {
            return new S2Point(new S2LatLng((com.google.common.geometry.S2Point) geometry));
        }

        throw new RuntimeException("Unsupported geometry type");
    }

    @Override
    public byte[] toStorageFormat(Geometry geometry) {
        // @todo 1 tb/tb do it 2015-12-22
        throw new RuntimeException("not implemented");
    }

    @Override
    public Geometry fromStorageFormat(byte[] rawData) {
        // @todo 1 tb/tb do it 2015-12-22
        throw new RuntimeException("not implemented");
    }

    @Override
    public Point createPoint(double lon, double lat) {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lon);

        return new S2Point(s2LatLng);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        final List<com.google.common.geometry.S2Point> loopPoints = extractS2Points(points);

        final S2Loop s2Loop = new S2Loop(loopPoints);
        boolean s2LoopValid = s2Loop.isValid();
        System.out.println("s2LoopValid = " + s2LoopValid);

        List<S2Loop> loopList = new ArrayList<>();
        loopList.add(s2Loop);
        boolean loopListValid = com.google.common.geometry.S2Polygon.isValid(loopList);
        System.out.println("loopListValid = " + loopListValid);

        final com.google.common.geometry.S2Polygon googlePolygon = new com.google.common.geometry.S2Polygon(s2Loop);
        return  new S2Polygon(googlePolygon);
    }

    @Override
    public LineString createLineString(List<Point> points) {
        final List<com.google.common.geometry.S2Point> loopPoints = extractS2Points(points);

        final S2Polyline s2Polyline = new S2Polyline(loopPoints);
        return new S2LineString(s2Polyline);
    }

    @Override
    public TimeAxis createTimeAxis(LineString lineString, Date startTime, Date endTime) {
        final S2Polyline inner = (S2Polyline) lineString.getInner();
        return new S2TimeAxis(inner, startTime, endTime);
    }

    public static List<com.google.common.geometry.S2Point> extractS2Points(List<Point> points) {
        final ArrayList<com.google.common.geometry.S2Point> loopPoints = new ArrayList<>();

        for (final Point point : points) {
            final S2LatLng s2LatLng = (S2LatLng) point.getInner();
            loopPoints.add(s2LatLng.toPoint());
        }
        return loopPoints;
    }
}
