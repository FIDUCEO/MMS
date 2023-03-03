package com.bc.fiduceo.location;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static com.bc.fiduceo.location.RasterPixelLocator.LON_EAST_WEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RasterPixelLocatorTest {

    private float[] lons;
    private float[] lons_swapped;
    private float[] lats;
    private Rectangle2D.Float boundary;

    @Before
    public void setUp() {
        lons = new float[]{-170, -130, -90, -50, -10, 10, 50, 90, 130, 170};
        lons_swapped = new float[]{170, 130, 90, 50, 10, -10, -50, -90, -130, -170};
        lats = new float[]{-80, -50, -20, 0, 20, 50, 80};

        boundary = new Rectangle2D.Float(-180, -88, 360, 176);
    }

    @Test
    public void testGetGeoLocation() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);

        Point2D geoLocation = rasterPixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(-170, geoLocation.getX(), 1e-8);
        assertEquals(-80, geoLocation.getY(), 1e-8);

        geoLocation = rasterPixelLocator.getGeoLocation(4.5, 2.5, null);
        assertEquals(-10, geoLocation.getX(), 1e-8);
        assertEquals(-20, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_outside() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);

        Point2D geoLocation = rasterPixelLocator.getGeoLocation(-1, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(11, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, 8.5, null);
        assertNull(geoLocation);
    }

    @Test
    public void testGetPixelLocation() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);

        // on the location
        Point2D[] locations = rasterPixelLocator.getPixelLocation(100, 20);
        assertEquals(1, locations.length);
        assertEquals(7.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from west
        locations = rasterPixelLocator.getPixelLocation(-144, 0);
        assertEquals(1, locations.length);
        assertEquals(1.5, locations[0].getX(), 1e-8);
        assertEquals(3.5, locations[0].getY(), 1e-8);

        // select closest from east
        locations = rasterPixelLocator.getPixelLocation(-46, -20);
        assertEquals(1, locations.length);
        assertEquals(3.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);

        // select closest from south
        locations = rasterPixelLocator.getPixelLocation(10, 18);
        assertEquals(1, locations.length);
        assertEquals(5.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from north
        locations = rasterPixelLocator.getPixelLocation(50, 22);
        assertEquals(1, locations.length);
        assertEquals(6.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outsideVectorButInsideRectangle() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);

        // west of lon axis
        Point2D[] locations = rasterPixelLocator.getPixelLocation(-174, 50);
        assertEquals(1, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(5.5, locations[0].getY(), 1e-8);

        // east of lon axis
        locations = rasterPixelLocator.getPixelLocation(177, 80);
        assertEquals(1, locations.length);
        assertEquals(9.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);

        // south of lat axis
        locations = rasterPixelLocator.getPixelLocation(-170, -84);
        assertEquals(1, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(0.5, locations[0].getY(), 1e-8);

        // north of lat axis
        locations = rasterPixelLocator.getPixelLocation(-130, 84);
        assertEquals(1, locations.length);
        assertEquals(1.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_outside() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons, lats, boundary);

        // south
        Point2D[] locations = rasterPixelLocator.getPixelLocation(90, -89);
        assertEquals(0, locations.length);

        // north
        locations = rasterPixelLocator.getPixelLocation(130, 88.5);
        assertEquals(0, locations.length);
    }

    @Test
    public void testGetIndexLargerThan() {
        int index = RasterPixelLocator.getIndexLargerThan(-197.f, lons);
        assertEquals(0, index);

        index = RasterPixelLocator.getIndexLargerThan(-176.2f, lons);
        assertEquals(0, index);

        index = RasterPixelLocator.getIndexLargerThan(-168.3f, lons);
        assertEquals(1, index);

        index = RasterPixelLocator.getIndexLargerThan(92.4f, lons);
        assertEquals(8, index);

        index = RasterPixelLocator.getIndexLargerThan(169.1f, lons);
        assertEquals(9, index);

        index = RasterPixelLocator.getIndexLargerThan(173.5f, lons);
        assertEquals(-1, index);
    }

    @Test
    public void testGetIndexSmallerThan() {
        int index = RasterPixelLocator.getIndexSmallerThan(172.f, lons_swapped);
        assertEquals(0, index);

        index = RasterPixelLocator.getIndexSmallerThan(163.f, lons_swapped);
        assertEquals(1, index);

        index = RasterPixelLocator.getIndexSmallerThan(52.f, lons_swapped);
        assertEquals(3, index);

        index = RasterPixelLocator.getIndexSmallerThan(-112.f, lons_swapped);
        assertEquals(8, index);

        index = RasterPixelLocator.getIndexSmallerThan(-169.4f, lons_swapped);
        assertEquals(9, index);

        index = RasterPixelLocator.getIndexSmallerThan(-176.3f, lons_swapped);
        assertEquals(-1, index);
    }

    @Test
    public void testGetGeoLocation_lonSwapped() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons_swapped, lats, boundary, LON_EAST_WEST);

        Point2D geoLocation = rasterPixelLocator.getGeoLocation(0.5, 0.5, null);
        assertEquals(170, geoLocation.getX(), 1e-8);
        assertEquals(-80, geoLocation.getY(), 1e-8);

        geoLocation = rasterPixelLocator.getGeoLocation(4.5, 2.5, null);
        assertEquals(10, geoLocation.getX(), 1e-8);
        assertEquals(-20, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetGeoLocation_lonSwapped_outside() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons_swapped, lats, boundary, LON_EAST_WEST);

        Point2D geoLocation = rasterPixelLocator.getGeoLocation(-1, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(11, 0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, -0.5, null);
        assertNull(geoLocation);

        geoLocation = rasterPixelLocator.getGeoLocation(1.5, 8.5, null);
        assertNull(geoLocation);
    }

    @Test
    public void testGetPixelLocation_lonSwapped() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons_swapped, lats, boundary, LON_EAST_WEST);

        // on the location
        Point2D[] locations = rasterPixelLocator.getPixelLocation(100, 20);
        assertEquals(1, locations.length);
        assertEquals(2.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from west
        locations = rasterPixelLocator.getPixelLocation(-144, 0);
        assertEquals(1, locations.length);
        assertEquals(8.5, locations[0].getX(), 1e-8);
        assertEquals(3.5, locations[0].getY(), 1e-8);


        // select closest from east
        locations = rasterPixelLocator.getPixelLocation(-86, -20);
        assertEquals(1, locations.length);
        assertEquals(7.5, locations[0].getX(), 1e-8);
        assertEquals(2.5, locations[0].getY(), 1e-8);

        // select closest from south
        locations = rasterPixelLocator.getPixelLocation(10, 18);
        assertEquals(1, locations.length);
        assertEquals(4.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);

        // select closest from north
        locations = rasterPixelLocator.getPixelLocation(50, 22);
        assertEquals(1, locations.length);
        assertEquals(3.5, locations[0].getX(), 1e-8);
        assertEquals(4.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_lonSwapped_outsideVectorButInsideRectangle() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons_swapped, lats, boundary, LON_EAST_WEST);

        // west of lon axis
        Point2D[] locations = rasterPixelLocator.getPixelLocation(-174, 50);
        assertEquals(1, locations.length);
        assertEquals(9.5, locations[0].getX(), 1e-8);
        assertEquals(5.5, locations[0].getY(), 1e-8);

        // east of lon axis
        locations = rasterPixelLocator.getPixelLocation(177, 80);
        assertEquals(1, locations.length);
        assertEquals(0.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);

        // south of lat axis
        locations = rasterPixelLocator.getPixelLocation(-170, -84);
        assertEquals(1, locations.length);
        assertEquals(9.5, locations[0].getX(), 1e-8);
        assertEquals(0.5, locations[0].getY(), 1e-8);

        // north of lat axis
        locations = rasterPixelLocator.getPixelLocation(-130, 84);
        assertEquals(1, locations.length);
        assertEquals(8.5, locations[0].getX(), 1e-8);
        assertEquals(6.5, locations[0].getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation_lonSwapped_outside() {
        final PixelLocator rasterPixelLocator = new RasterPixelLocator(lons_swapped, lats, boundary, LON_EAST_WEST);

        // south
        Point2D[] locations = rasterPixelLocator.getPixelLocation(90, -89);
        assertEquals(0, locations.length);

        // north
        locations = rasterPixelLocator.getPixelLocation(130, 88.5);
        assertEquals(0, locations.length);
    }
}
