package com.bc.geometry.s2;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polyline;

public class S2WKTWriter {

    public static String write(S2Polyline polyline) {
        final int numVertices = polyline.numVertices();
        if (numVertices < 2) {
            throw new IllegalArgumentException("Linestring contains less that 2 vertices.");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("LINESTRING(");

        for (int i = 0; i < numVertices; i++) {
            final S2Point vertex = polyline.vertex(i);
            final S2LatLng latLng = new S2LatLng(vertex);
            builder.append(latLng.lngDegrees());
            builder.append(" ");
            builder.append(latLng.latDegrees());
            if (i != numVertices - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
