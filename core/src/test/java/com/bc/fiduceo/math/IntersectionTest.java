package com.bc.fiduceo.math;

import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class IntersectionTest {

    private Intersection intersection;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        intersection = new Intersection();
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @Test
    public void testSetGetGeometry() {
        final Geometry geometry = geometryFactory.createPoint(-11.9, 22.5);

        intersection.setGeometry(geometry);
        assertSame(geometry, intersection.getGeometry());
    }

    @Test
    public void testSetGetTimeInfor() {
        final TimeInfo timeInfo = new TimeInfo();

        intersection.setTimeInfo(timeInfo);
        assertSame(timeInfo, intersection.getTimeInfo());
    }

    @Test
    public void testSetGetPrimaryGeometry() {
        final Geometry geometry = geometryFactory.createPoint(-12.9, 23.5);

        intersection.setPrimaryGeometry(geometry);
        assertSame(geometry, intersection.getPrimaryGeometry());
    }

    @Test
    public void testSetGetSecondaryGeometry() {
        final Geometry geometry = geometryFactory.createPoint(-13.9, 24.5);

        intersection.setSecondaryGeometry(geometry);
        assertSame(geometry, intersection.getSecondaryGeometry());
    }
}
