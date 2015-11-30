package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeometryFactoryTest_S2 {

    @Test
    public void testParsePolygon() {
        final GeometryFactory factory = new GeometryFactory(GeometryFactory.Type.S2);

        final Geometry geometry = factory.parse("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))");
        assertNotNull(geometry);
        assertTrue(geometry instanceof S2Polygon);

        assertEquals("Polygon: (1) loops:\n" +
                "loop <\n" +
                "(6.0, 1.9999999999999996)\n" +
                "(4.999999999999999, 1.9999999999999996)\n" +
                "(4.0, 2.0)\n" +
                "(3.0000000000000004, 1.9999999999999996)\n" +
                "(3.000000000000001, 3.0000000000000004)\n" +
                "(4.0, 3.0000000000000004)\n" +
                "(4.999999999999999, 3.0000000000000004)\n" +
                "(6.000000000000001, 3.0000000000000004)\n" +
                ">\n", geometry.toString());
    }
}
