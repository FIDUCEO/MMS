package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BilinearInterpolatorTest {

    @Test
    public void testInterpolate_flat() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.34, 0.67);

        final double interpolate = interpolator.interpolate(1.f, 1.f, 1.f, 1.f);
        assertEquals(1.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_rise_left_right() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.5, 0.5);

        final double interpolate = interpolator.interpolate(-1.f, 1.f, -1.f, 1.f);
        assertEquals(0.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_rise_top_down() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.5, 0.5);

        final double interpolate = interpolator.interpolate(-1.f, -1.f, 1.f, 1.f);
        assertEquals(0.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_standard() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.18, 0.74);

        final double interpolate = interpolator.interpolate(18f, 19f, 18.4f, 19.13f);
        assertEquals(18.44003565673828, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_boundaries() {
        final BilinearInterpolator left = new BilinearInterpolator(0.0, 0.67);
        double interpolate = left.interpolate(10f, 11f, 12f, 13f);
        assertEquals(11.34, interpolate, 1e-8);

        final BilinearInterpolator right = new BilinearInterpolator(1.0, 0.67);
        interpolate = right.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.34, interpolate, 1e-8);

        final BilinearInterpolator top = new BilinearInterpolator(0.66, 0.0);
        interpolate = top.interpolate(10f, 11f, 12f, 13f);
        assertEquals(10.66, interpolate, 1e-8);

        final BilinearInterpolator bottom = new BilinearInterpolator(0.66, 1.0);
        interpolate = bottom.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.66, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_corners() {
        final BilinearInterpolator upperLeft = new BilinearInterpolator(0.0, 0.0);
        double interpolate = upperLeft.interpolate(10f, 11f, 12f, 13f);
        assertEquals(10.0, interpolate, 1e-8);

        final BilinearInterpolator upperRight = new BilinearInterpolator(1.0, 0.0);
        interpolate = upperRight.interpolate(10f, 11f, 12f, 13f);
        assertEquals(11.0, interpolate, 1e-8);

        final BilinearInterpolator lowerLeft= new BilinearInterpolator(0.0, 1.0);
        interpolate = lowerLeft.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.0, interpolate, 1e-8);

        final BilinearInterpolator lowerRight = new BilinearInterpolator(1.0, 1.0);
        interpolate = lowerRight.interpolate(10f, 11f, 12f, 13f);
        assertEquals(13.0, interpolate, 1e-8);
    }
}
