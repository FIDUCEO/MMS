package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.*;

import static org.junit.Assert.*;

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
        assertEquals(835, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        interpolator = context.get(2, 1);
        assertEquals(834, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        final Rectangle era5Region = context.getEra5Region();
        assertEquals(834, era5Region.x);
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
        assertEquals(835, interpolator.getXMin());
        assertEquals(247, interpolator.getYMin());

        final Rectangle era5Region = context.getEra5Region();
        assertEquals(835, era5Region.x);
        assertEquals(247, era5Region.y);
        assertEquals(2, era5Region.width);
        assertEquals(2, era5Region.height);
    }

    @Test
    public void testGetEra5Collection_fromConfig() {
        final Configuration configuration = new Configuration();
        configuration.setNWPAuxDir("/yamas/strange/path");
        configuration.setEra5Collection("era_5t");

        final Era5Collection era5Collection = Era5PostProcessing.getEra5Collection(configuration);
        assertEquals(Era5Collection.ERA_5T, era5Collection);
    }

    @Test
    public void testGetEra5Collection_fromPath_unknown() {
        final Configuration configuration = new Configuration();
        configuration.setNWPAuxDir("/yamas/strange/path");

        try {
            Era5PostProcessing.getEra5Collection(configuration);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetEra5Collection_fromPath() {
        final Configuration configuration = new Configuration();

        configuration.setNWPAuxDir("/data/era5");
        assertEquals(Era5Collection.ERA_5, Era5PostProcessing.getEra5Collection(configuration));

        configuration.setNWPAuxDir("/data/era-5");
        assertEquals(Era5Collection.ERA_5, Era5PostProcessing.getEra5Collection(configuration));

        configuration.setNWPAuxDir("/data/ERA51");
        assertEquals(Era5Collection.ERA_51, Era5PostProcessing.getEra5Collection(configuration));

        configuration.setNWPAuxDir("/data/era-51");
        assertEquals(Era5Collection.ERA_51, Era5PostProcessing.getEra5Collection(configuration));

        configuration.setNWPAuxDir("/data/era5t");
        assertEquals(Era5Collection.ERA_5T, Era5PostProcessing.getEra5Collection(configuration));

        configuration.setNWPAuxDir("/data/ERA-5T");
        assertEquals(Era5Collection.ERA_5T, Era5PostProcessing.getEra5Collection(configuration));
    }

    @Test
    public void testGetLonMin() {
        assertEquals(720, Era5PostProcessing.getEra5LonMin(-179.99f));
        assertEquals(720, Era5PostProcessing.getEra5LonMin(-179.84f));
        assertEquals(721, Era5PostProcessing.getEra5LonMin(-179.67f));
        assertEquals(1125, Era5PostProcessing.getEra5LonMin(-78.54f));
        assertEquals(1344, Era5PostProcessing.getEra5LonMin(-23.8f));
        assertEquals(1438, Era5PostProcessing.getEra5LonMin(-0.26f));
        assertEquals(1439, Era5PostProcessing.getEra5LonMin(-0.18f));
        assertEquals(0, Era5PostProcessing.getEra5LonMin(0.f));
        assertEquals(173, Era5PostProcessing.getEra5LonMin(43.32f));
        assertEquals(718, Era5PostProcessing.getEra5LonMin(179.58f));
        assertEquals(718, Era5PostProcessing.getEra5LonMin(179.72f));
        assertEquals(719, Era5PostProcessing.getEra5LonMin(179.98f));
        assertEquals(719, Era5PostProcessing.getEra5LonMin(179.99f));
    }

    @Test
    public void testGetLatMin() {
        assertEquals(0, Era5PostProcessing.getEra5LatMin(89.95f));
        assertEquals(88, Era5PostProcessing.getEra5LatMin(67.87f));
        assertEquals(359, Era5PostProcessing.getEra5LatMin(0.f));
        assertEquals(448, Era5PostProcessing.getEra5LatMin(-22.19f));
        assertEquals(719, Era5PostProcessing.getEra5LatMin(-89.95f));
    }

    @Test
    public void testIsValidLon() {
        assertTrue(Era5PostProcessing.isValidLon(12.8f));
        assertTrue(Era5PostProcessing.isValidLon(-176.3f));

        assertFalse(Era5PostProcessing.isValidLon(1087f));
        assertFalse(Era5PostProcessing.isValidLon(9.96921E36f));
        assertFalse(Era5PostProcessing.isValidLon(Float.NaN));
    }

    @Test
    public void testIsValidLat() {
        assertTrue(Era5PostProcessing.isValidLat(33.9f));
        assertTrue(Era5PostProcessing.isValidLat(-76.3f));

        assertFalse(Era5PostProcessing.isValidLat(-92.6f));
        assertFalse(Era5PostProcessing.isValidLat(9.96921E36f));
        assertFalse(Era5PostProcessing.isValidLat(Float.NaN));
    }

    @Test
    public void testCreateInterpolator() {
        BilinearInterpolator interpolator = Era5PostProcessing.createInterpolator(-179.99f, 89.95f, 720, 0);
        assertEquals(0.03997802734375, interpolator.getA(), 1e-8);
        assertEquals(0.20001220703125, interpolator.getB(), 1e-8);

        interpolator = Era5PostProcessing.createInterpolator(-78.54f, 67.87f, 1125, 88);
        assertEquals(0.839996337890625, interpolator.getA(), 1e-8);
        assertEquals(0.519989013671875, interpolator.getB(), 1e-8);

        interpolator = Era5PostProcessing.createInterpolator(-0.18f, 0.f, 1439, 359);
        assertEquals(0.2799999713897705, interpolator.getA(), 1e-8);
        assertEquals(1.0, interpolator.getB(), 1e-8);

        interpolator = Era5PostProcessing.createInterpolator(43.32f, -22.19f, 173, 448);
        assertEquals(0.279998779296875, interpolator.getA(), 1e-8);
        assertEquals(0.7600021362304688, interpolator.getB(), 1e-8);

        interpolator = Era5PostProcessing.createInterpolator(179.98f, -89.95f, 719, 719);
        assertEquals(0.91998291015625, interpolator.getA(), 1e-8);
        assertEquals(0.79998779296875, interpolator.getB(), 1e-8);
    }
}
