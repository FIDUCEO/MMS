package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class GeometryFactoryTest_JTS {

    private GeometryFactory factory;

    @Before
    public void setUp() {
        factory = new GeometryFactory(GeometryFactory.Type.JTS);
    }

    @Test
    public void testParsePolygon() {
        final Geometry geometry = factory.parse("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSPolygon);

        assertEquals("POLYGON ((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))", geometry.toString());
    }

    @Test
    public void testParseLineString() {
        final Geometry geometry = factory.parse("LINESTRING(3 1, 3 2, 3 3, 3 4)");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSLineString);

        assertEquals("LINESTRING (3 1, 3 2, 3 3, 3 4)", geometry.toString());
    }

    @Test
    public void testCreatePoint() {
        Point point = factory.createPoint(11.78, -23.56);
        assertNotNull(point);
        assertEquals(11.78, point.getLon(), 1e-8);
        assertEquals(-23.56, point.getLat(), 1e-8);

        point = factory.createPoint(-106.224, 19.86);
        assertNotNull(point);
        assertEquals(-106.224, point.getLon(), 1e-8);
        assertEquals(19.86, point.getLat(), 1e-8);
    }

    @Test
    public void testCreatePolygon() {
        final ArrayList<Point> points = new ArrayList<>();

        points.add(factory.createPoint(0, 0));
        points.add(factory.createPoint(1, 0));
        points.add(factory.createPoint(1, 1));
        points.add(factory.createPoint(0, 1));
        points.add(factory.createPoint(0, 0));

        final Polygon polygon = factory.createPolygon(points);
        assertNotNull(polygon);
        assertEquals("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))", polygon.toString());
    }
}
