package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Interval;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PixelLocatorSegmentedTest {

    private PixelLocatorSegmented pixelLocator;

    @Before
    public void setUp() {
        pixelLocator = new PixelLocatorSegmented(300);
    }

    @Test
    public void testEmpty() {
        final Point2D geoLocation = pixelLocator.getGeoLocation(23, 55.8, null);
        assertNull(geoLocation);

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-12.77, 81.66);
        assertEquals(0, pixelLocation.length);
    }

    @Test
    public void testOneGeoCoding() {
        final GeoCoding geoCoding = mock(GeoCoding.class);
        when(geoCoding.getGeoPos(any(), any())).thenReturn(new GeoPos(22.8, -14.55));
        when(geoCoding.getPixelPos(any(), any())).thenReturn(new PixelPos(108, 2167));
        final Interval interval = new Interval(198, 2455);

        pixelLocator.addGeoCoding(geoCoding, interval);

        // inside interval
        Point2D geoLocation = pixelLocator.getGeoLocation(223, 244, null);
        assertEquals(-14.55, geoLocation.getX(), 1e-8);
        assertEquals(22.8, geoLocation.getY(), 1e-8);

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-109.34, -65.26);
        assertEquals(1, pixelLocation.length);
        assertEquals(108, pixelLocation[0].getX(), 1e-8);
        assertEquals(2365, pixelLocation[0].getY(), 1e-8);
    }

    @Test
    public void testOneGeoCoding_positionsOutside() {
        final GeoCoding geoCoding = mock(GeoCoding.class);
        when(geoCoding.getGeoPos(any(), any())).thenReturn(new GeoPos(23.8, -15.55));
        when(geoCoding.getPixelPos(any(), any())).thenReturn(null);
        final Interval interval = new Interval(199, 2356);

        pixelLocator.addGeoCoding(geoCoding, interval);

        // outside interval
        Point2D geoLocation = pixelLocator.getGeoLocation(224, 2, null);
        assertNull(geoLocation);

        // outside swath width
        geoLocation = pixelLocator.getGeoLocation(500, 3, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(-1, 4, null);
        assertNull(geoLocation);

        geoLocation = pixelLocator.getGeoLocation(225, 2357, null);
        assertNull(geoLocation);

        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-109.34, -65.26);
        assertEquals(0, pixelLocation.length);
    }

    @Test
    public void testThreeGeoCodings() {
        final GeoCoding geoCoding_0 = mock(GeoCoding.class);
        when(geoCoding_0.getGeoPos(any(), any())).thenReturn(new GeoPos(23.8, -15.55));
        when(geoCoding_0.getPixelPos(any(), any())).thenReturn(new PixelPos(109, 2156));
        final Interval interval_0 = new Interval(198, 2355);
        pixelLocator.addGeoCoding(geoCoding_0, interval_0);

        final GeoCoding geoCoding_1 = mock(GeoCoding.class);
        when(geoCoding_1.getGeoPos(any(), any())).thenReturn(new GeoPos(24.8, -16.55));
        when(geoCoding_1.getPixelPos(any(), any())).thenReturn(new PixelPos(110, 125));
        final Interval interval_1 = new Interval(2677, 5688);
        pixelLocator.addGeoCoding(geoCoding_1, interval_1);

        final GeoCoding geoCoding_2 = mock(GeoCoding.class);
        when(geoCoding_2.getGeoPos(any(), any())).thenReturn(new GeoPos(25.8, -17.55));
        when(geoCoding_2.getPixelPos(any(), any())).thenReturn(new PixelPos(111, 450));
        final Interval interval_2 = new Interval(6200, 8900);
        pixelLocator.addGeoCoding(geoCoding_2, interval_2);

        // before first interval
        Point2D geoLocation = pixelLocator.getGeoLocation(224, 100, null);
        assertNull(geoLocation);

        // inside first interval
        geoLocation = pixelLocator.getGeoLocation(225, 500, null);
        assertEquals(-15.55, geoLocation.getX(), 1e-8);
        assertEquals(23.8, geoLocation.getY(), 1e-8);

        // before second interval
        geoLocation = pixelLocator.getGeoLocation(226, 2500, null);
        assertNull(geoLocation);

        // inside second interval
        geoLocation = pixelLocator.getGeoLocation(227, 4500, null);
        assertEquals(-16.55, geoLocation.getX(), 1e-8);
        assertEquals(24.8, geoLocation.getY(), 1e-8);

        // before third interval
        geoLocation = pixelLocator.getGeoLocation(227, 6000, null);
        assertNull(geoLocation);

        // inside third interval
        geoLocation = pixelLocator.getGeoLocation(228, 7500, null);
        assertEquals(-17.55, geoLocation.getX(), 1e-8);
        assertEquals(25.8, geoLocation.getY(), 1e-8);

        // after third interval
        geoLocation = pixelLocator.getGeoLocation(229, 10000, null);
        assertNull(geoLocation);

        // all three return a value ....
        final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-109.34, -65.26);
        assertEquals(3, pixelLocation.length);
        assertEquals(109, pixelLocation[0].getX(), 1e-8);
        assertEquals(2354, pixelLocation[0].getY(), 1e-8);

        assertEquals(110, pixelLocation[1].getX(), 1e-8);
        assertEquals(2802, pixelLocation[1].getY(), 1e-8);

        assertEquals(111, pixelLocation[2].getX(), 1e-8);
        assertEquals(6650, pixelLocation[2].getY(), 1e-8);
    }

    @Test
    public void testIsInSegment() {
        final Interval interval = new Interval(100, 500);

        assertTrue(pixelLocator.isInSegment(new PixelPos(231, 0), interval));
        assertTrue(pixelLocator.isInSegment(new PixelPos(23, 150), interval));
        assertTrue(pixelLocator.isInSegment(new PixelPos(257, 398), interval));

        assertFalse(pixelLocator.isInSegment(new PixelPos(-1, 498), interval));
        assertFalse(pixelLocator.isInSegment(new PixelPos(302, 498), interval));
        assertFalse(pixelLocator.isInSegment(new PixelPos(156, -1), interval));
        assertFalse(pixelLocator.isInSegment(new PixelPos(156, 401), interval));
    }
}
