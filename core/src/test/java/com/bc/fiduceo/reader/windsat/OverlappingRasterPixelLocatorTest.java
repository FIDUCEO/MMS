package com.bc.fiduceo.reader.windsat;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OverlappingRasterPixelLocatorTest {

    private float[] lats;
    private float[] lons_1;
    private float[] lons;
    private Rectangle2D.Float boundary_1;
    private Rectangle2D.Float boundary_2;

    @Before
    public void setUp() {
        lats = new float[]{-75, -45, -15, 15, 45, 75};
        lons = new float[]{15, 0, 345, 315, 285, 255, 225, 195, 165, 135, 105, 75, 45, 15, 0};
    }

    @Test
    public void testGetGeoLocation() {
        final OverlappingRasterPixelLocator pixelLocator = new OverlappingRasterPixelLocator(lons, lats);

        Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(15.0, geoLocation.getX(), 1e-8);
        assertEquals(-75.0, geoLocation.getY(), 1e-8);

        geoLocation = pixelLocator.getGeoLocation(13.5, 1.5, null);
        assertEquals(15.0, geoLocation.getX(), 1e-8);
        assertEquals(-45.0, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_outside() {
        final OverlappingRasterPixelLocator pixelLocator = new OverlappingRasterPixelLocator(lons, lats);

        Point2D geoLocation = pixelLocator.getGeoLocation(-0.5, 0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(15.5, 0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(2.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(2.5, 6.5, null);
        assertNull(geoLocation);

    }
}
