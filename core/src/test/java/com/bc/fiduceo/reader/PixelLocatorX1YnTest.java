package com.bc.fiduceo.reader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.esa.snap.core.datamodel.Rotator;
import org.esa.snap.core.util.math.RsMathUtils;
import org.junit.*;
import ucar.ma2.Array;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Created by Sabine on 26.06.2017.
 */
public class PixelLocatorX1YnTest {

    private PixelLocatorX1Yn pixelLocatorX1Yn;
    private Array lats;
    private Array lons;
    private int maxDistanceKm;

    @Before
    public void setUp() throws Exception {
        lons = Array.factory(new float[]{12, 13, 14, 15, 16, 17, 18});
        lats = Array.factory(new float[]{20, 30, 40, 50, 60, 70, 80});
        maxDistanceKm = 5;
        pixelLocatorX1Yn = new PixelLocatorX1Yn(maxDistanceKm, lons, lats);
    }

    @Test
    public void invalidConstruction() throws Exception {
        try {
            new PixelLocatorX1Yn(1, null, lats);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(NullPointerException.class)));
        }
        try {
            new PixelLocatorX1Yn(1, lons, null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(NullPointerException.class)));
        }
        try {
            final Array mock = mock(Array.class);
            final long maxValue = Integer.MAX_VALUE;
            when(mock.getSize()).thenReturn(maxValue + 1);
            new PixelLocatorX1Yn(1, mock, lats);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            final String expectedMsg = "The number of elements in an array must be less or equal the integer maximum value 2147483647 = 0x7fffffff = (2^31)-1.";
            assertThat(expected.getMessage(), is(equalTo(expectedMsg)));
        }
        try {
            final Array mock = mock(Array.class);
            when(mock.getSize()).thenReturn(lats.getSize() + 1);
            new PixelLocatorX1Yn(1, lons, mock);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is(equalTo("The arrays lons and lats must have the same number of elements.")));
        }
    }

    @Test
    public void getGeoLocation_validCalls() throws Exception {
        // valid min x min y = first pixel
        assertThat(pixelLocatorX1Yn.getGeoLocation(0d, 0d, null), is(equalTo(new Point2D.Double(12d, 20d))));
        // valid max x min y = first pixel
        assertThat(pixelLocatorX1Yn.getGeoLocation(1d, 0d, null), is(equalTo(new Point2D.Double(12d, 20d))));
        // valid min x max y yalue
        assertThat(pixelLocatorX1Yn.getGeoLocation(0d, lats.getSize(), null), is(equalTo(new Point2D.Double(18d, 80d))));
        // valid max x max y yalue
        assertThat(pixelLocatorX1Yn.getGeoLocation(1d, lats.getSize(), null), is(equalTo(new Point2D.Double(18d, 80d))));

        // valid middle x middle y yalue
        final double y = lats.getSize() / 2d;
        assertThat(pixelLocatorX1Yn.getGeoLocation(0.5, y, null), is(equalTo(new Point2D.Double(15d, 50d))));
    }

    @Test
    public void getGeoLocation_invalidCalls() throws Exception {
        try {
            // x value less than 0
            pixelLocatorX1Yn.getGeoLocation(-0.000000000000001, 0.5, null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is(equalTo("Invalid x value. Must be in the range >=0 and <=1.")));
        }
        try {
            // x value bigger than 1
            pixelLocatorX1Yn.getGeoLocation(1.000000000000001, 0.5, null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is(equalTo("Invalid x value. Must be in the range >=0 and <=1.")));
        }
        try {
            // y value less than 0
            pixelLocatorX1Yn.getGeoLocation(0.5, -0.000000000000001, null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is(equalTo("Invalid y value. Must be in the range >=0 and <=7.")));
        }
        try {
            // y value bigger size of data
            pixelLocatorX1Yn.getGeoLocation(0.5, lats.getSize() + 0.000000000000001, null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertThat(expected, is(instanceOf(RuntimeException.class)));
            assertThat(expected.getMessage(), is(equalTo("Invalid y value. Must be in the range >=0 and <=7.")));
        }
    }

    @Test
    public void getPixelLocation_validCalls() throws Exception {
        for (int i = 0; i < lons.getSize(); i++) {
            final double lon = lons.getDouble(i);
            final double lat = lats.getDouble(i);
            final Point2D.Double[] expected = new Point2D.Double[]{new Point2D.Double(0.5, i + 0.5)};
            assertThat("Loop number " + i, pixelLocatorX1Yn.getPixelLocation(lon, lat), is(equalTo(expected)));
        }
    }

    @Test
    public void getPixelLocation_distanceBiggerThan5km() throws Exception {
        final double distanceInMeters = maxDistanceKm * 1000 + 0.00001;
        final double distBigger5kmInDegree = Math.toDegrees(distanceInMeters / RsMathUtils.MEAN_EARTH_RADIUS);
        for (int i = 0; i < lons.getSize(); i++) {
            final double lon = lons.getDouble(i);
            final double lat = lats.getDouble(i);
            final Rotator rotator = new Rotator(lon, lat);
            final Point2D.Double toFar = new Point2D.Double(distBigger5kmInDegree, 0);
            rotator.transformInversely(toFar);
            final double toFarLon = toFar.getX();
            final double toFarLat = toFar.getY();
            final Point2D[] pixelLocations = pixelLocatorX1Yn.getPixelLocation(toFarLon, toFarLat);
            assertThat("Loop number " + i, pixelLocations, is(notNullValue()));
            assertThat("Loop number " + i, pixelLocations.length, is(0));
        }
    }
}