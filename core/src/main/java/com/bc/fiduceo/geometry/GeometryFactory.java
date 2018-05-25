/*
 * Copyright (C) 2018 Brockmann Consult GmbH
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

import com.bc.fiduceo.geometry.jts.JtsGeometryFactory;
import com.bc.fiduceo.geometry.s2.BcS2GeometryFactory;

import java.util.Date;
import java.util.List;

public class GeometryFactory extends AbstractGeometryFactory {

    private final GeometryFactoryInterface factoryImpl;

    public GeometryFactory(Type type) {
        if (type == Type.JTS) {
            factoryImpl = new JtsGeometryFactory();
        } else if (type == Type.S2) {
            factoryImpl = new BcS2GeometryFactory();
        } else {
            throw new IllegalArgumentException("unknown geometry factory type");
        }
    }

    public GeometryFactory(String type) {
        if ("S2".equalsIgnoreCase(type)) {
            factoryImpl = new BcS2GeometryFactory();
        } else if ("JTS".equalsIgnoreCase(type)) {
            factoryImpl = new JtsGeometryFactory();
        } else {
            throw new IllegalArgumentException("unknown geometry factory type");
        }
    }

    @Override
    public Geometry parse(String wkt) {
        return factoryImpl.parse(wkt);
    }

    @Override
    public String format(Geometry geometry) {
        return factoryImpl.format(geometry);
    }

    @Override
    public byte[] toStorageFormat(Geometry geometry) {
        return factoryImpl.toStorageFormat(geometry);
    }

    @Override
    public Geometry fromStorageFormat(byte[] rawData) {
        return factoryImpl.fromStorageFormat(rawData);
    }

    @Override
    public Point createPoint(double lon, double lat) {
        return factoryImpl.createPoint(lon, lat);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        return factoryImpl.createPolygon(points);
    }

    @Override
    public LineString createLineString(List<Point> points) {
        return factoryImpl.createLineString(points);
    }

    @Override
    public MultiPolygon createMultiPolygon(List<Polygon> polygonList) {
        return factoryImpl.createMultiPolygon(polygonList);
    }

    @Override
    public TimeAxis createTimeAxis(LineString lineString, Date startTime, Date endTime) {
        return factoryImpl.createTimeAxis(lineString, startTime, endTime);
    }

    public enum Type {
        JTS,
        S2
    }
}
