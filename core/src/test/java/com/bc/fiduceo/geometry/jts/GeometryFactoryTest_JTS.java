package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
}
