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
import com.bc.geometry.s2.S2WKTWriter;
import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BcS2GeometryFactory extends AbstractGeometryFactory {

    private final S2WKTReader s2WKTReader;

    public BcS2GeometryFactory() {
        s2WKTReader = new S2WKTReader();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Geometry parse(String wkt) {
        final Object geometry = s2WKTReader.read(wkt);
        if (geometry instanceof S2Polygon) {
            return new BcS2Polygon(geometry);
        } else if (geometry instanceof S2Polyline) {
            return new BcS2LineString((S2Polyline) geometry);
        } else if (geometry instanceof S2Point) {
            return BcS2Point.createFrom((S2Point) geometry);
        } else if (geometry instanceof List) {
            final  Object geoObject = ((List<?>) geometry).get(0);
            if (geoObject instanceof S2Polygon) {
                final ArrayList<Polygon> polygonList = new ArrayList<>();
                final List<S2Polygon> googlePolygonList = (List<S2Polygon>) geometry;
                for (S2Polygon googlePolygon : googlePolygonList) {
                    polygonList.add(new BcS2Polygon(googlePolygon));
                }
                return new BcS2MultiPolygon(polygonList);
            } else if (geoObject instanceof S2Polyline) {
                final List<S2Polyline> googlePolylineList = (List<S2Polyline>) geometry;
                return new BcS2MultiLineString(googlePolylineList);
            }
        }
        throw new RuntimeException("Unsupported geometry type");
    }

    @Override
    public String format(Geometry geometry) {
        return S2WKTWriter.write(geometry.getInner());
    }

    @Override
    public byte[] toStorageFormat(Geometry geometry) {
        final String wkt = S2WKTWriter.write(geometry.getInner());
        return wkt.getBytes();
    }

    @Override
    public Geometry fromStorageFormat(byte[] rawData) {
        final String geometryWkt = new String(rawData);
        return parse(geometryWkt);
    }

    @Override
    public Point createPoint(double lon, double lat) {
        final S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lon);

        return new BcS2Point(s2LatLng);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        final List<S2Point> loopPoints = extractS2Points(points);
        final S2Point first = loopPoints.get(0);
        final int lastIndex = loopPoints.size() - 1;
        final S2Point last = loopPoints.get(lastIndex);
        if (first.equals(last)) {
            loopPoints.remove(lastIndex);
        }

        final S2Loop s2Loop = new S2Loop(loopPoints);

        final S2Polygon googlePolygon = new S2Polygon(s2Loop);
        return new BcS2Polygon(googlePolygon);
    }

    @Override
    public LineString createLineString(List<Point> points) {
        final List<S2Point> loopPoints = extractS2Points(points);

        final S2Polyline s2Polyline = new S2Polyline(loopPoints);
        return new BcS2LineString(s2Polyline);
    }

    @Override
    public MultiPolygon createMultiPolygon(List<Polygon> polygonList) {
        return new BcS2MultiPolygon(polygonList);
    }

    @Override
    public TimeAxis createTimeAxis(LineString lineString, Date startTime, Date endTime) {
        return new BcS2TimeAxis(lineString, startTime, endTime);
    }

    static List<S2Point> extractS2Points(List<Point> points) {
        final ArrayList<S2Point> loopPoints = new ArrayList<>();

        for (final Point point : points) {
            final S2LatLng s2LatLng = (S2LatLng) point.getInner();
            loopPoints.add(s2LatLng.toPoint());
        }
        return loopPoints;
    }
}
