package com.bc.fiduceo.math;

import com.google.common.geometry.S2LatLng;
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

    // @todo 2 tb/tb move to S2WKTWriter class and write tests 2015-11-23
    public static String toWKT(S2Polyline polyline) {
        final StringBuilder builder = new StringBuilder();
        builder.append("LINESTRING(");

        final int numVertices = polyline.numVertices();
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
