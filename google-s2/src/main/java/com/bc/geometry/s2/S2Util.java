
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

import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

public class S2Util {

    public static double[][] toXYZArray(S2Polygon polygon) {
        final List<S2Point> vertices = new ArrayList<>();
        final int numLoops = polygon.numLoops();
        for (int i = 0; i < numLoops; i++) {
            final S2Loop loop = polygon.loop(i);
            final int numVertices = loop.numVertices();
            for (int k = 0; k < numVertices; k++) {
                vertices.add(loop.vertex(k));
            }
        }

        final int numVertices = vertices.size();
        final double[][] xyzArray = new double[numVertices][3];
        for (int i = 0; i < numVertices; i++) {
            final double[] vector = new double[3];
            final S2Point s2Point = vertices.get(i);
            vector[0] = s2Point.getX();
            vector[1] = s2Point.getY();
            vector[2] = s2Point.getZ();

            xyzArray[i] = vector;
        }

        return xyzArray;
    }

    public static double[][] toXYZArray(S2Polyline polyline) {
        final int numVertices = polyline.numVertices();
        final double[][] xyzArray = new double[numVertices][3];
        for (int i = 0; i < numVertices; i++) {
            final double[] vector = new double[3];
            final S2Point s2Point = polyline.vertex(i);
            vector[0] = s2Point.getX();
            vector[1] = s2Point.getY();
            vector[2] = s2Point.getZ();

            xyzArray[i] = vector;
        }

        return xyzArray;
    }

    public static void plot(double[][] vertices) {
        System.out.print("[");
        for (int i = 0; i < vertices.length; i++) {
            final double[] vector = vertices[i];
            System.out.print(vector[0] + " " + vector[1] + " " + vector[2]);
            if (i == vertices.length - 1) {
                System.out.println("]");
            } else {
                System.out.println(";");
            }
        }
    }
}
