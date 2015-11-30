package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeometryFactoryTest_JTS {

    @Test
    public void testParsePolygon() {
        final GeometryFactory factory = new GeometryFactory(GeometryFactory.Type.JTS);

        final Geometry geometry = factory.parse("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof JTSPolygon);

        assertEquals("POLYGON ((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))", geometry.toString());
    }
}
