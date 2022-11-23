package com.bc.fiduceo.reader.windsat;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
        Point2D[] pixelLocation = pixelLocator.getPixelLocation(105.f, -45.f);
        assertEquals(1, pixelLocation.length);
        // @todo 1 tb/tb continue here 2022-11-23
        //assertEquals(5, pixelLocation[0].getX(), 1e-8);
        //assertEquals(1, pixelLocation[0].getY(), 1e-8);
    }
}
