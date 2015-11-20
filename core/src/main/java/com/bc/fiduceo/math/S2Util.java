package com.bc.fiduceo.math;

import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;

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
            final double[]vector = new double[3];
            final S2Point s2Point = vertices.get(i);
            vector[0] = s2Point.getX();
            vector[1] = s2Point.getY();
            vector[2] = s2Point.getZ();

            xyzArray[i] = vector;
        }

        return xyzArray;
    }

    public static void plot(double[][] vertices) {
        for (final double[] vector : vertices) {
            System.out.println("[ " + vector[0] + " " + vector[1] + " " + vector[2] + "]");
        }
    }
}
