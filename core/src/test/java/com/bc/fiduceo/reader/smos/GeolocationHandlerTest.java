package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.core.Interval;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.Index;

import java.awt.geom.Rectangle2D;

import static com.bc.fiduceo.reader.smos.GeolocationHandler.LATITUDE;
import static com.bc.fiduceo.reader.smos.GeolocationHandler.LONGITUDE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class GeolocationHandlerTest {

    private GeolocationHandler geolocationHandler;

    @Before
    public void setUp() {
        final float[] longitudes = {-180.f, -150.f, -120.f, -90.f, -60.f, -30.f, 0.f, 30.f, 60.f, 90.f, 120.f, 150.f, 180.f};
        final float[] latitudes = {-90.f, -60.f, -30.f, 0.f, 30.f, 60.f, 90.f};
        final Rectangle2D.Float geoBoundary = new Rectangle2D.Float(-180.f, -90.f, 360.f, 180.f);
        final RasterPixelLocator pixelLocator = new RasterPixelLocator(longitudes, latitudes, geoBoundary);

        geolocationHandler = new GeolocationHandler(pixelLocator);
    }

    @Test
    public void testGetLon() {
        final Interval interval = new Interval(1, 3);
        final Array lonArray = geolocationHandler.read(3, 2, interval, LONGITUDE);

        assertArrayEquals(new int[]{3, 1}, lonArray.getShape());
        final Index index = lonArray.getIndex();
        index.set(0, 0);
        assertEquals(-90.f, lonArray.getFloat(index), 1e-8);
        index.set(1, 0);
        assertEquals(-90.f, lonArray.getFloat(index), 1e-8);
        index.set(2, 0);
        assertEquals(-90.f, lonArray.getFloat(index), 1e-8);
    }

    @Test
    public void testGetLat() {
        final Interval interval = new Interval(3, 3);
        final Array latArray = geolocationHandler.read(4, 3, interval, LATITUDE);

        assertArrayEquals(new int[]{3, 3}, latArray.getShape());
        final Index index = latArray.getIndex();
        index.set(0, 0);
        assertEquals(-30.f, latArray.getFloat(index), 1e-8);
        index.set(1, 1);
        assertEquals(0.f, latArray.getFloat(index), 1e-8);
        index.set(2, 2);
        assertEquals(30.f, latArray.getFloat(index), 1e-8);
    }

    @Test
    public void testGetLon_outsideUpperRight() {
        final Interval interval = new Interval(3, 3);
        final Array lonArray = geolocationHandler.read(12, 0, interval, LONGITUDE);

        assertArrayEquals(new int[]{3, 3}, lonArray.getShape());
        final Index index = lonArray.getIndex();
        index.set(0, 0);
        assertEquals(Float.NaN, lonArray.getFloat(index), 1e-8);
        index.set(0, 2);
        assertEquals(Float.NaN, lonArray.getFloat(index), 1e-8);
        index.set(1, 0);
        assertEquals(150.f, lonArray.getFloat(index), 1e-8);
        index.set(1, 1);
        assertEquals(180.f, lonArray.getFloat(index), 1e-8);
        index.set(1, 2);
        assertEquals(Float.NaN, lonArray.getFloat(index), 1e-8);
    }

    @Test
    public void testGetLat_outsideLowerLeft() {
        final Interval interval = new Interval(3, 3);
        final Array latArray = geolocationHandler.read(0, 6, interval, LATITUDE);

        assertArrayEquals(new int[]{3, 3}, latArray.getShape());
        final Index index = latArray.getIndex();
        index.set(0, 0);
        assertEquals(Float.NaN, latArray.getFloat(index), 1e-8);
        index.set(0, 1);
        assertEquals(60.f, latArray.getFloat(index), 1e-8);
        index.set(0, 2);
        assertEquals(60.f, latArray.getFloat(index), 1e-8);
        index.set(1, 0);
        assertEquals(Float.NaN, latArray.getFloat(index), 1e-8);
        index.set(1, 1);
        assertEquals(90.f, latArray.getFloat(index), 1e-8);
        index.set(1, 2);
        assertEquals(90.f, latArray.getFloat(index), 1e-8);

        index.set(2, 2);
        assertEquals(Float.NaN, latArray.getFloat(index), 1e-8);
    }
}
