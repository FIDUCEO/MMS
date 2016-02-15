package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author muhammad.bc
 */
class S2MultiPolygon implements Polygon {
    private List<com.google.common.geometry.S2Polygon> polygonList;

    public S2MultiPolygon(List<com.google.common.geometry.S2Polygon> polygonList) {
        this.polygonList = polygonList;

    }

    @Override
    public void shiftLon(double lon) {

    }

    @Override
    public Geometry intersection(Geometry other) {
        if (other instanceof S2MultiLineString) {
            return intersectS2MultiLineString(other);
        } else if (other.getInner() instanceof com.google.common.geometry.S2Polygon) {
            return intersectS2MultiPolygon(other);
        }
        throw new NullPointerException("Must not reach here !!!");
    }

    private Geometry intersectS2MultiLineString(Geometry other) {
        List<S2Polyline> s2PolylineList = new ArrayList<>();
        for (com.google.common.geometry.S2Polygon s2Polygon : polygonList) {
            List<S2Polyline> s2Polylines_S2MultiLines = (List<S2Polyline>) other.getInner();
            for (S2Polyline s2Polyline : s2Polylines_S2MultiLines) {
                s2PolylineList.addAll(s2Polygon.intersectWithPolyLine(s2Polyline));
            }
        }
        return new S2MultiLineString(s2PolylineList);
    }

    private Geometry intersectS2MultiPolygon(Geometry other) {
        List<com.google.common.geometry.S2Polygon> resultList = new ArrayList<>();
        for (com.google.common.geometry.S2Polygon s2Polygon : polygonList) {
            com.google.common.geometry.S2Polygon intersection = new com.google.common.geometry.S2Polygon();

            intersection.initToIntersection(s2Polygon, (com.google.common.geometry.S2Polygon) other.getInner());

            if (intersection.numLoops() != 0) {
                resultList.add(intersection);
            }
        }

        if (resultList.size() == 1) {
            return new S2Polygon(resultList.get(0));
        }
        return new S2MultiPolygon(resultList);
    }


    @Override
    public boolean isEmpty() {
        return polygonList.isEmpty();
    }

    @Override
    public Point[] getCoordinates() {
        List<Point> pointList = new ArrayList<>();
        for (com.google.common.geometry.S2Polygon polygon : polygonList) {
            int i = polygon.numLoops();
            for (int j = 0; j < i; j++) {
                S2Loop loop = polygon.loop(j);
                int i1 = loop.numVertices();
                for (int k = 0; k < i1; k++) {
                    com.google.common.geometry.S2Point vertex = loop.vertex(k);
                    pointList.add(new S2Point(new S2LatLng(vertex)));
                }
            }
        }
        return pointList.toArray(new Point[pointList.size()]);
    }


    @Override
    public Object getInner() {
        return polygonList;
    }
}
