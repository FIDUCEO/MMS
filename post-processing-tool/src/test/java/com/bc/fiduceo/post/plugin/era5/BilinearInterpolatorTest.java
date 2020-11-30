package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BilinearInterpolatorTest {

    @Test
    public void testInterpolate_flat() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.34, 0.67, 56, 123);

        final double interpolate = interpolator.interpolate(1.f, 1.f, 1.f, 1.f);
        assertEquals(1.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_rise_left_right() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.5, 0.5, 57, 124);

        final double interpolate = interpolator.interpolate(-1.f, 1.f, -1.f, 1.f);
        assertEquals(0.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_rise_top_down() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.5, 0.5, 58, 125);

        final double interpolate = interpolator.interpolate(-1.f, -1.f, 1.f, 1.f);
        assertEquals(0.0, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_standard() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.18, 0.74, 55, 126);

        final double interpolate = interpolator.interpolate(18f, 19f, 18.4f, 19.13f);
        assertEquals(18.44003565673828, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_boundaries() {
        final BilinearInterpolator left = new BilinearInterpolator(0.0, 0.67, 60, 127);
        double interpolate = left.interpolate(10f, 11f, 12f, 13f);
        assertEquals(11.34, interpolate, 1e-8);

        final BilinearInterpolator right = new BilinearInterpolator(1.0, 0.67, 60, 127);
        interpolate = right.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.34, interpolate, 1e-8);

        final BilinearInterpolator top = new BilinearInterpolator(0.66, 0.0, 60, 127);
        interpolate = top.interpolate(10f, 11f, 12f, 13f);
        assertEquals(10.66, interpolate, 1e-8);

        final BilinearInterpolator bottom = new BilinearInterpolator(0.66, 1.0, 60, 127);
        interpolate = bottom.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.66, interpolate, 1e-8);
    }

    @Test
    public void testInterpolate_corners() {
        final BilinearInterpolator upperLeft = new BilinearInterpolator(0.0, 0.0, 61, 128);
        double interpolate = upperLeft.interpolate(10f, 11f, 12f, 13f);
        assertEquals(10.0, interpolate, 1e-8);

        final BilinearInterpolator upperRight = new BilinearInterpolator(1.0, 0.0, 61, 128);
        interpolate = upperRight.interpolate(10f, 11f, 12f, 13f);
        assertEquals(11.0, interpolate, 1e-8);

        final BilinearInterpolator lowerLeft= new BilinearInterpolator(0.0, 1.0, 61, 128);
        interpolate = lowerLeft.interpolate(10f, 11f, 12f, 13f);
        assertEquals(12.0, interpolate, 1e-8);

        final BilinearInterpolator lowerRight = new BilinearInterpolator(1.0, 1.0, 61, 128);
        interpolate = lowerRight.interpolate(10f, 11f, 12f, 13f);
        assertEquals(13.0, interpolate, 1e-8);
    }

    @Test
    public void testConstructAndGetCoordinates() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.0, 0.0, 62, 129);

        assertEquals(62, interpolator.getXMin());
        assertEquals(129, interpolator.getYMin());
    }
}
