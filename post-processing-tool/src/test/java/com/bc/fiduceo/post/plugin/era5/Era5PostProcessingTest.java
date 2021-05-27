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
        assertEquals(0, Era5PostProcessing.getEra5LonMin(-179.99f));
        assertEquals(0, Era5PostProcessing.getEra5LonMin(-179.84f));
        assertEquals(1, Era5PostProcessing.getEra5LonMin(-179.67f));
        assertEquals(405, Era5PostProcessing.getEra5LonMin(-78.54f));
        assertEquals(624, Era5PostProcessing.getEra5LonMin(-23.8f));
        assertEquals(718, Era5PostProcessing.getEra5LonMin(-0.26f));
        assertEquals(719, Era5PostProcessing.getEra5LonMin(0.f));
        assertEquals(893, Era5PostProcessing.getEra5LonMin(43.32f));
        assertEquals(1438, Era5PostProcessing.getEra5LonMin(179.58f));
        assertEquals(1438, Era5PostProcessing.getEra5LonMin(179.72f));
        assertEquals(1439, Era5PostProcessing.getEra5LonMin(179.98f));
        assertEquals(1439, Era5PostProcessing.getEra5LonMin(179.99f));
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
}
