package com.bc.fiduceo.reader.windsat;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OverlappingRasterPixelLocatorTest {

    private OverlappingRasterPixelLocator pixelLocator;

    @Before
    public void setUp() {
        final float[] lats = new float[]{-75, -45, -15, 15, 45, 75};
        final float[] lons = new float[]{15, 0, 345, 315, 285, 255, 225, 195, 165, 135, 105, 75, 45, 15, 0};

        pixelLocator = new OverlappingRasterPixelLocator(lons, lats);
    }

    @Test
    public void testGetGeoLocation() {
        Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(15.0, geoLocation.getX(), 1e-8);
        assertEquals(-75.0, geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(13.5, 1.5, null);
        assertEquals(15.0, geoLocation.getX(), 1e-8);
        assertEquals(-45.0, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_outside() {
        Point2D geoLocation = pixelLocator.getGeoLocation(-0.5, 0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(15.5, 0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(2.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(2.5, 6.5, null);
        assertNull(geoLocation);
    }

    @Test
    public void testGetPixelLocation_singleRasterArea() {
        // on the location
        Point2D[] locations = pixelLocator.getPixelLocation(105.f, -45.f);
        assertEquals(1, locations.length);
        assertEquals(10.5, locations[0].getX(), 1e-8);
        assertEquals(1.5, locations[0].getY(), 1e-8);

        // select closest from west
        locations = pixelLocator.getPixelLocation(134, -15);
        assertEquals(1, locations.length);
        assertEquals(9.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);

        // select closest from east
        locations = pixelLocator.getPixelLocation(167, 15);
        assertEquals(1, locations.length);
        assertEquals(8.5, locations[0].getX(), 1e-8);
        assertEquals(3.5, locations[0].getY(), 1e-8);

        // select closest from south
        locations = pixelLocator.getPixelLocation(-15, 43);
        assertEquals(1, locations.length);
        assertEquals(2.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from north
        locations = pixelLocator.getPixelLocation(-15, 47);
        assertEquals(1, locations.length);
        assertEquals(2.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_overlappingRasterArea() {
        Point2D[] locations = pixelLocator.getPixelLocation(15.f, -45.f);
        assertEquals(2, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(1.5, locations[0].getY(), 1e-8);

        assertEquals(13.5, locations[1].getX(), 1e-8);
        assertEquals(1.5, locations[1].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outsideVectorButInsideRectangle_singleArea() {
        // west
        Point2D[] locations = pixelLocator.getPixelLocation(-171.f, -45.f);
        assertEquals(1, locations.length);
        assertEquals(7.5, locations[0].getX(), 1e-8);
        assertEquals(1.5, locations[0].getY(), 1e-8);

        // east
        locations = pixelLocator.getPixelLocation(171.f, -45.f);
        assertEquals(1, locations.length);
        assertEquals(8.5, locations[0].getX(), 1e-8);
        assertEquals(1.5, locations[0].getY(), 1e-8);

        // north
        locations = pixelLocator.getPixelLocation(-15.f, 80.f);
        assertEquals(1, locations.length);
        assertEquals(2.5, locations[0].getX(), 1e-8);
        assertEquals(5.5, locations[0].getY(), 1e-8);

        // south
        locations = pixelLocator.getPixelLocation(-15.f, -80.f);
        assertEquals(1, locations.length);
        assertEquals(2.5, locations[0].getX(), 1e-8);
        assertEquals(0.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outsideVectorButInsideRectangle_overlappingArea() {
        // west
        Point2D[] locations = pixelLocator.getPixelLocation(-6.f, -15.f);
        assertEquals(2, locations.length);
        assertEquals(1.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);
        assertEquals(14.5, locations[1].getX(), 1e-8);
        assertEquals(2.5, locations[1].getY(), 1e-8);

        // east
        locations = pixelLocator.getPixelLocation(22.f, -15.f);
        assertEquals(2, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);
        assertEquals(13.5, locations[1].getX(), 1e-8);
        assertEquals(2.5, locations[1].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outside() {
        // south
        Point2D[] locations = pixelLocator.getPixelLocation(90, -91);
        assertEquals(0, locations.length);

        // north
        locations = pixelLocator.getPixelLocation(130, 92);
        assertEquals(0, locations.length);
    }
}
