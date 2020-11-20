package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.core.GeoRect;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Era5PostProcessingTest {

    @Test
    public void testGetGeoRegion() {
        final Array lonArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, new float[]{127.4f, 127.5f, 127.32f, 127.41f, 127.88f, 127.58f});
        final Array latArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, new float[]{-11.3f, -11.65f, -11.13f, -11.17f, -11.23f, -11.308f});

        final GeoRect geoRect = Era5PostProcessing.getGeoRegion(lonArray, latArray);
        assertEquals(127.32f, geoRect.getLonMin(), 1e-8);
        assertEquals(127.88f, geoRect.getLonMax(), 1e-8);
        assertEquals(-11.65f, geoRect.getLatMin(), 1e-8);
        assertEquals(-11.13f, geoRect.getLatMax(), 1e-8);
    }

    @Test
    public void testGetEra5RasterPosition() {
        GeoRect geoRect = new GeoRect(8.34f, 8.56f, -56.85f, -56.34f);

        Rectangle rasterPosition = Era5PostProcessing.getEra5RasterPosition(geoRect);
        assertEquals(753, rasterPosition.x);
        assertEquals(3, rasterPosition.width);
        assertEquals(585, rasterPosition.y);
        assertEquals(4, rasterPosition.height);

        geoRect = new GeoRect(-16.602997f, -16.339996f, 63.629f, 63.729f);

        rasterPosition = Era5PostProcessing.getEra5RasterPosition(geoRect);
        assertEquals(653, rasterPosition.x);
        assertEquals(3, rasterPosition.width);
        assertEquals(105, rasterPosition.y);
        assertEquals(2, rasterPosition.height);
    }

    @Test
    public void testGetEra5RasterPosition_point() {
        GeoRect geoRect = new GeoRect(-17.233136f, -17.233136f, 63.677588f, 63.677588f);

        Rectangle rasterPosition = Era5PostProcessing.getEra5RasterPosition(geoRect);
        assertEquals(651, rasterPosition.x);
        assertEquals(2, rasterPosition.width);
        assertEquals(105, rasterPosition.y);
        assertEquals(2, rasterPosition.height);
    }

    // @todo 1 tb/tb check anti-meridian data 2020-11-18

    @Test
    public void testGetInterpolationContext() {
        final float[] longitudes = new float[] {
                -151.1874f, -151.2369f, -151.2863f,
                -151.1929f, -151.2424f, -151.2918f
        };
        final Array lonArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, longitudes);

        final float[] latitudes = new float[] {
                28.0077f, 27.9995f, 27.9912f,
                28.0379f, 28.0297f, 28.0214f
        };
        final Array latArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, latitudes);

        final InterpolationContext context = Era5PostProcessing.getInterpolationContext(lonArray, latArray);
        assertNotNull(context);
    }
}
