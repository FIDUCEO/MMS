
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
