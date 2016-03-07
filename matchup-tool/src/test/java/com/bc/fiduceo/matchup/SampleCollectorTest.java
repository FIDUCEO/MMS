package com.bc.fiduceo.matchup;

import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import org.junit.*;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

public class SampleCollectorTest {

    private SampleCollector collector;
    private Polygon polygon;

    @Before
    public void setUp() throws Exception {
        final PixelLocator pixelLocator = new OffsetPixelLocator(11, 13);

        collector = new SampleCollector(pixelLocator, 30, 50);
        final GeometryFactory factory = new GeometryFactory(GeometryFactory.Type.S2);
        final List<Point> points = Arrays.asList(
                    factory.createPoint(3, 10),
                    factory.createPoint(9, 12),
                    factory.createPoint(10, 2),
                    factory.createPoint(1, 4),
                    factory.createPoint(3, 10)
        );
        polygon = factory.createPolygon(points);
    }

    @Test
    public void testSomething() {
        final Sample[] samples = collector.getSamplesFor(polygon);

        assertNotNull(samples);
        // todo 1 se/se continue 2016-03-03
//        assertEquals(4, samples.length);
    }

    private static class OffsetPixelLocator implements PixelLocator {

        private final int offsetX;
        private final int offsetY;

        public OffsetPixelLocator(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        public Point2D getGeoLocation(double x, double y, Point2D g) {
            return new Point2D.Double(x - offsetX, y - offsetY);
        }

        @Override
        public Point2D[] getPixelLocation(double lon, double lat) {
            return new Point2D[]{new Point2D.Double(lon + offsetX, lat + offsetY)};
        }
    }
}
