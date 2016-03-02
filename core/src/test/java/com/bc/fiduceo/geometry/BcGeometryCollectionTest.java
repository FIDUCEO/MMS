package com.bc.fiduceo.geometry;

import com.bc.fiduceo.geometry.s2.BcS2GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by muhammad.bc on 3/2/2016.
 */
public class BcGeometryCollectionTest {
    @Test
    public void testGetCoordinates() throws Exception {
        final GeometryCollection geometryCollection = getGeometryCollection();

        Point[] coordinates = geometryCollection.getCoordinates();
        assertNotNull(coordinates);
        assertEquals(8,coordinates.length);
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

    @Test
    public void testGetInner() throws Exception {
        GeometryCollection geometryCollection = getGeometryCollection();
        Object inner = geometryCollection.getInner();
        assertTrue(inner instanceof Geometry[]);
    }
}