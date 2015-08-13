package com.bc.fiduceo.math;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import org.junit.Test;

public class IntersectionTest {

    @Test
    public void testIntersectLineAndPolygon() throws ParseException {
        final WKTReader wktReader = new WKTReader();
        final Polygon polygon = (Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final LineString lineString = (LineString) wktReader.read("LINESTRING(-2 0,4 6)");

        System.out.println("original length: " + lineString.getLength());

        final LineString intersection = (LineString) polygon.intersection(lineString);
        System.out.println("intersected length: " + intersection.getLength());

        final Point startPoint = intersection.getStartPoint();
        final LengthIndexedLine lengthIndexedLine = new LengthIndexedLine(lineString);
        final double pointLength = lengthIndexedLine.indexOf(startPoint.getCoordinate());
        System.out.println("offset length: " + pointLength);

        final LineString lineString_2 = (LineString) wktReader.read("LINESTRING(-1 1,5 7)");
        System.out.println("original length 2: " + lineString.getLength());

        final LineString intersection_2 = (LineString) polygon.intersection(lineString_2);
        System.out.println("intersected length 2: " + intersection_2.getLength());

        final Point startPoint_2 = intersection_2.getStartPoint();
        final LengthIndexedLine lengthIndexedLine_2 = new LengthIndexedLine(lineString_2);
        final double pointLength_2 = lengthIndexedLine_2.indexOf(startPoint_2.getCoordinate());
        System.out.println("offset length: " + pointLength_2);
    }
}
