package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Era5PostProcessingTest {

    // @todo 1 tb/tb check anti-meridian data 2020-11-18

    @Test
    public void testGetInterpolationContext() {
        final float[] longitudes = new float[]{
                -151.1874f, -151.2369f, -151.2863f,
                -151.1929f, -151.2424f, -151.2918f
        };
        final Array lonArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, longitudes);

        final float[] latitudes = new float[]{
                28.0077f, 27.9995f, 27.9912f,
                28.0379f, 28.0297f, 28.0214f
        };
        final Array latArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, latitudes);

        final InterpolationContext context = Era5PostProcessing.getInterpolationContext(lonArray, latArray);
        assertNotNull(context);

        BilinearInterpolator interpolator = context.get(0, 0);
        assertEquals(115, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        interpolator = context.get(2, 1);
        assertEquals(114, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        final Rectangle era5Region = context.getEra5Region();
        assertEquals(114, era5Region.x);
        assertEquals(247, era5Region.y);
        assertEquals(3, era5Region.width);
        assertEquals(3, era5Region.height);
    }

    @Test
    public void testGetInterpolationContext_singlePixel() {
        final float[] longitudes = new float[]{-151.1874f};
        final Array lonArray = Array.factory(DataType.FLOAT, new int[]{1}, longitudes).reduce();

        final float[] latitudes = new float[]{28.0077f};
        final Array latArray = Array.factory(DataType.FLOAT, new int[]{1}, latitudes).reduce();

        final InterpolationContext context = Era5PostProcessing.getInterpolationContext(lonArray, latArray);
        assertNotNull(context);

        BilinearInterpolator interpolator = context.get(0, 0);
        assertEquals(115, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        final Rectangle era5Region = context.getEra5Region();
        assertEquals(115, era5Region.x);
        assertEquals(247, era5Region.y);
        assertEquals(2, era5Region.width);
        assertEquals(2, era5Region.height);
    }
}
