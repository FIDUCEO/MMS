
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

package com.bc.geometry.s2;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.List;

public class S2WKTWriter {

    public static String write(Object geometry) {

        if (geometry instanceof S2Polyline) {
            return writeLinestringWKT((S2Polyline) geometry);
        } else if (geometry instanceof List) {
            final List geometryList = (List) geometry;
            if (!geometryList.isEmpty() && geometryList.get(0) instanceof S2Polyline) {
                return writeMultiLinestringWKT((List<S2Polyline>) geometry);
            }
            if (!geometryList.isEmpty() && geometryList.get(0) instanceof S2Polygon) {
                return writeMultiPolygonWKT((List<S2Polygon>) geometry);
            }
        } else if (geometry instanceof S2Point) {
            return writePointWkt((S2Point) geometry);
        } else if (geometry instanceof S2LatLng) {
            return writePointWkt((S2LatLng) geometry);
        } else if (geometry instanceof S2Polygon) {
            return writePolygonWkt((S2Polygon) geometry);
        }

        throw new IllegalArgumentException("unsupported geometry type: " + geometry.toString());
    }

    private static String writePolygonWkt(S2Polygon polygon) {
        final StringBuilder builder = new StringBuilder();
        builder.append("POLYGON((");

        writeLoopPoints(polygon, builder);

        builder.append("))");
        return builder.toString();
    }

    private static void writeLoopPoints(S2Polygon polygon, StringBuilder builder) {
        final int numLoops = polygon.numLoops();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = polygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                appendWktPoint(loop.vertex(k), builder);
                builder.append(",");
            }
            appendWktPoint(loop.vertex(0), builder);
        }
    }

    private static String writePointWkt(S2Point geometry) {
        final S2LatLng s2LatLng = new S2LatLng(geometry);
        return writePointWkt(s2LatLng);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static String writePointWkt(S2LatLng geometry) {
        final StringBuilder builder = new StringBuilder();
        builder.append("POINT(");
        builder.append(geometry.lngDegrees());
        builder.append(",");
        builder.append(geometry.latDegrees());
        builder.append(")");
        return builder.toString();
    }

    private static String writeLinestringWKT(S2Polyline geometry) {
        final int numVertices = geometry.numVertices();
        if (numVertices < 2) {
            throw new IllegalArgumentException("Linestring contains less that 2 vertices.");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("LINESTRING(");
        for (int i = 0; i < numVertices; i++) {
            appendWktPoint(geometry.vertex(i), builder);
            if (i != numVertices - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private static String writeMultiLinestringWKT(List<S2Polyline> geometry) {
        final StringBuilder builder = new StringBuilder();
        builder.append("MULTILINESTRING(");

        for (final S2Polyline polyline : geometry) {
            final int numVertices = polyline.numVertices();
            if (numVertices < 2) {
                throw new IllegalArgumentException("Linestring contains less that 2 vertices.");
            }

            builder.append("(");
            for (int i = 0; i < numVertices; i++) {
                appendWktPoint(polyline.vertex(i), builder);
                if (i != numVertices - 1) {
                    builder.append(",");
                }
            }
            builder.append("),");
        }

        builder.setLength(builder.length() - 1);    // remove last comma tb 2017-06-26
        
        builder.append(")");
        return builder.toString();
    }

    private static String writeMultiPolygonWKT(List<S2Polygon> geometry) {
        final StringBuilder builder = new StringBuilder();
        builder.append("MULTIPOLYGON(");

        for (final S2Polygon polygon : geometry) {
            builder.append("((");
            writeLoopPoints(polygon, builder);
            builder.append(")),");
        }

       builder.setLength(builder.length() - 1);    // remove last comma tb 2018-01-30

        builder.append(")");
        return builder.toString();
    }

    private static void appendWktPoint(S2Point vertex, StringBuilder builder) {
        final S2LatLng latLng = new S2LatLng(vertex);
        builder.append(latLng.lngDegrees());
        builder.append(" ");
        builder.append(latLng.latDegrees());
    }
}
