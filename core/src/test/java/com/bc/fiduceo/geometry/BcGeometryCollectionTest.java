package com.bc.fiduceo.geometry;

import com.bc.fiduceo.geometry.s2.BcS2GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BcGeometryCollectionTest {
    @Test
    public void testGetCoordinates() {
        final GeometryCollection geometryCollection = getGeometryCollection();

        final Point[] coordinates = geometryCollection.getCoordinates();
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
    }

    @Test
    public void testGetInner() {
        final GeometryCollection geometryCollection = getGeometryCollection();
        final Object inner = geometryCollection.getInner();
        assertTrue(inner instanceof Geometry[]);
    }

    @Test
    public void testIsValid_valid() {
        final GeometryCollection geometryCollection = new BcGeometryCollection();
        final Geometry[] geometries = new Geometry[2];
        geometries[0] = mock(Geometry.class);
        when(geometries[0].isValid()).thenReturn(true);
        geometries[1] = mock(Geometry.class);
        when(geometries[1].isValid()).thenReturn(true);
        geometryCollection.setGeometries(geometries);

        assertTrue(geometryCollection.isValid());
    }

    @Test
    public void testIsValid_invalid() {
        final GeometryCollection geometryCollection = new BcGeometryCollection();
        final Geometry[] geometries = new Geometry[2];
        geometries[0] = mock(Geometry.class);
        when(geometries[0].isValid()).thenReturn(false);
        geometries[1] = mock(Geometry.class);
        when(geometries[1].isValid()).thenReturn(true);
        geometryCollection.setGeometries(geometries);

        assertFalse(geometryCollection.isValid());
    }

    @Test
    public void testIsValid_valid_empty() {
        final GeometryCollection geometryCollection = new BcGeometryCollection();

        assertFalse(geometryCollection.isValid());
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