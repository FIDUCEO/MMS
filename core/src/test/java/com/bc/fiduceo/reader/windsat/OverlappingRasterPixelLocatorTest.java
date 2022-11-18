package com.bc.fiduceo.reader.windsat;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;

public class OverlappingRasterPixelLocatorTest {

    private float[] lats;
    private float[] lons_1;
    private float[] lons_2;
    private Rectangle2D.Float boundary_1;
    private Rectangle2D.Float boundary_2;

    @Before
    public void setUp() {
        lats = new float[]{-75, -45, -15, 15, 45, 75};
        lons_1 = new float[]{75, 45, 15, 0};
        lons_2 = new float[]{15, 0, -15, -45, -75, -105, -135, -165};

        boundary_1 = new Rectangle2D.Float(0, -88, 90, 176);
        boundary_2 = new Rectangle2D.Float(-180, -88, 210, 176);
    }

    @Test
    public void testGetGeoLocation_oneRaster() {
        final float[][] latVectors = {lats};
        final float[][] lonVectors = {lons_2};
        final Rectangle2D.Float[] boundaries = {boundary_2};

        final OverlappingRasterPixelLocator pixelLocator = new OverlappingRasterPixelLocator(lonVectors, latVectors, boundaries);

        /*
        @todo 1 tb/tb continue here 2022-11-18
        Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(-170, geoLocation.getX(), 1e-8);
        assertEquals(-80, geoLocation.getY(), 1e-8);
         */
    }
}
