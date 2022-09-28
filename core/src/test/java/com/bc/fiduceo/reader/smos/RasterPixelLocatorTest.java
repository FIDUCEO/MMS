package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.location.PixelLocator;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RasterPixelLocatorTest {

    @Test
    public void testGetGeoLocation() {
        final float[] lons = {-180, -140, -100, -60, -20, 20, 60, 100, 140, 180};
        final float[] lats = {-90, -60, -30, 0, 30, 60, 90};

        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats);
        Point2D geoLocation = rasterPixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(-180, geoLocation.getX(), 1e-8);
        assertEquals(-90, geoLocation.getY(), 1e-8);

        geoLocation = rasterPixelLocator.getGeoLocation(4.5, 2.5, null);
        assertEquals(-20, geoLocation.getX(), 1e-8);
        assertEquals(-30, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_outside() {
        final float[] lons = {-180, -140, -100, -60, -20, 20, 60, 100, 140, 180};
        final float[] lats = {-90, -60, -30, 0, 30, 60, 90};

        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats);

        Point2D geoLocation = rasterPixelLocator.getGeoLocation(-1, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(11, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, 8.5, null);
        assertNull(geoLocation);
    }
}
