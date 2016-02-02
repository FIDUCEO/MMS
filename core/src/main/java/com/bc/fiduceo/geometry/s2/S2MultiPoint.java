package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.google.common.geometry.*;
import com.google.common.geometry.S2Point;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author muhammad.bc
 */
public class S2MultiPoint implements Polygon {
    private List<com.google.common.geometry.S2Point> s2PointList;

    public S2MultiPoint(List<com.google.common.geometry.S2Point> s2PointList) {
        this.s2PointList = s2PointList;
    }

    @Override
    public void shiftLon(double lon) {

    }

    @Override
    public Geometry intersection(Geometry other) {
        List<com.google.common.geometry.S2Polygon> s2PolygonIntersect = new ArrayList<>();
        for (S2Point s2Point : s2PointList) {
            S2Loop s2Loop = new S2Loop(Arrays.asList(s2Point));
            com.google.common.geometry.S2Polygon s2Polygon = new com.google.common.geometry.S2Polygon(s2Loop);

            final com.google.common.geometry.S2Polygon intersection = new com.google.common.geometry.S2Polygon();
            intersection.initToIntersection(s2Polygon, (com.google.common.geometry.S2Polygon) other.getInner());
            s2PolygonIntersect.add(intersection);
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Point[] getCoordinates() {
        return s2PointList.toArray(new Point[s2PointList.size()]);
    }


    @Override
    public Object getInner() {
        return s2PointList;
    }
}
