package com.bc.fiduceo.location;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RasterPixelLocatorTest {

    private PixelLocator rasterPixelLocator;

    @Before
    public void setUp() {
        final float[] lons = {-170, -130, -90, -50, -10, 10, 50, 90, 130, 170};
        final float[] lats = {-80, -50, -20, 0, 20, 50, 80};

        final Rectangle2D.Float boundary = new Rectangle2D.Float(-180, -88, 360, 176);

        rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);
    }

    @Test
    public void testGetGeoLocation() {
        Point2D geoLocation = rasterPixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(-170, geoLocation.getX(), 1e-8);
        assertEquals(-80, geoLocation.getY(), 1e-8);

        geoLocation = rasterPixelLocator.getGeoLocation(4.5, 2.5, null);
        assertEquals(-10, geoLocation.getX(), 1e-8);
        assertEquals(-20, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_outside() {
        Point2D geoLocation = rasterPixelLocator.getGeoLocation(-1, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(11, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, 8.5, null);
        assertNull(geoLocation);
    }

    @Test
    public void testGetPixelLocation() {
        // on the location
        Point2D[] locations = rasterPixelLocator.getPixelLocation(100, 20);
        assertEquals(1, locations.length);
        assertEquals(7.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from west
        locations = rasterPixelLocator.getPixelLocation(-144, 0);
        assertEquals(1, locations.length);
        assertEquals(1.5, locations[0].getX(), 1e-8);
        assertEquals(3.5, locations[0].getY(), 1e-8);

        // select closest from east
        locations = rasterPixelLocator.getPixelLocation(-46, -20);
        assertEquals(1, locations.length);
        assertEquals(3.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);

        // select closest from south
        locations = rasterPixelLocator.getPixelLocation(10, 18);
        assertEquals(1, locations.length);
        assertEquals(5.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from north
        locations = rasterPixelLocator.getPixelLocation(50, 22);
        assertEquals(1, locations.length);
        assertEquals(6.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outsideVectorButInsideRectangle() {
        // west of lon axis
        Point2D[] locations = rasterPixelLocator.getPixelLocation(-174, 50);
        assertEquals(1, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(5.5, locations[0].getY(), 1e-8);

        // east of lon axis
        locations = rasterPixelLocator.getPixelLocation(177, 80);
        assertEquals(1, locations.length);
        assertEquals(9.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);

        // south of lat axis
        locations = rasterPixelLocator.getPixelLocation(-170, -84);
        assertEquals(1, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(0.5, locations[0].getY(), 1e-8);

        // north of lat axis
        locations = rasterPixelLocator.getPixelLocation(-130, 84);
        assertEquals(1, locations.length);
        assertEquals(1.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outside() {
        // south
        Point2D[] locations = rasterPixelLocator.getPixelLocation(90, -89);
        assertEquals(0, locations.length);

        // north
        locations = rasterPixelLocator.getPixelLocation(130, 88.5);
        assertEquals(0, locations.length);
    }
}
