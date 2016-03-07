package com.bc.fiduceo.geometry;

import com.bc.fiduceo.geometry.s2.BcS2GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BcGeometryCollectionTest {
    @Test
    public void testGetCoordinates() throws Exception {
        final GeometryCollection geometryCollection = getGeometryCollection();

        Point[] coordinates = geometryCollection.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(10, coordinates.length);
        assertEquals("POINT(0.0 0.0)", coordinates[0].toString());
        assertEquals("POINT(4.0 0.0)", coordinates[1].toString());
        assertEquals("POINT(4.000000000000001 1.0)", coordinates[2].toString());
        assertEquals("POINT(0.0 1.0)", coordinates[3].toString());
        assertEquals("POINT(0.0 0.0)", coordinates[4].toString());
        assertEquals("POINT(3.0000000000000004 0.0)", coordinates[5].toString());
        assertEquals("POINT(4.999999999999999 0.0)", coordinates[6].toString());
        assertEquals("POINT(5.0 0.9999999999999998)", coordinates[7].toString());
        assertEquals("POINT(3.0000000000000004 1.0)", coordinates[8].toString());
        assertEquals("POINT(3.0000000000000004 0.0)", coordinates[9].toString());

        Point point = coordinates[2];
        assertEquals(4.000000000000001, point.getLon(), 1e-8);
        assertEquals(1.0, point.getLat(), 1e-8);


        point = coordinates[7];
        assertEquals(5.0, point.getLon(), 1e-8);
        assertEquals(0.9999999999999998, point.getLat(), 1e-8);
    }

    @Test
    public void testGetInner() throws Exception {
        GeometryCollection geometryCollection = getGeometryCollection();
        Object inner = geometryCollection.getInner();
        assertTrue(inner instanceof Geometry[]);
    }

    private GeometryCollection getGeometryCollection() {
        BcS2GeometryFactory geometryFactory = new BcS2GeometryFactory();

        Geometry[] geometries = new Geometry[2];
        geometries[0] = geometryFactory.parse("POLYGON ((0 0, 4 0, 4 1, 0 1, 0 0))");
        geometries[1] = geometryFactory.parse("POLYGON ((3 0, 5 0, 5 1, 3 1, 3 0))");

        final GeometryCollection geometryCollection = new BcGeometryCollection();
        geometryCollection.setGeometries(geometries);
        return geometryCollection;
    }
}