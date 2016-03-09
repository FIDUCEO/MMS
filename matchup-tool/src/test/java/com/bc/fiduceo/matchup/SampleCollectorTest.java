package com.bc.fiduceo.matchup;

import static org.junit.Assert.*;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.tool.ToolContext;
import org.junit.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SampleCollectorTest {

    private SampleCollector collector;
    private GeometryFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new GeometryFactory(GeometryFactory.Type.S2);
        final ToolContext context = new ToolContext();
        context.setGeometryFactory(factory);

        final PixelLocator pixelLocator = new OffsetPixelLocator(11, 13);
        collector = new SampleCollector(context, pixelLocator);
    }

    @Test
    public void testSomething() {
        // preparation
        final Sample[] expecteds = {
                    new Sample(12, 14, 1.5, 1.5, null),
                    new Sample(13, 14, 2.5, 1.5, null),
                    new Sample(12, 15, 1.5, 2.5, null),
                    new Sample(13, 15, 2.5, 2.5, null)

        };
        final List<Point> points = createPoints(new double[]{
                    1, 1,
                    3, 1,
                    3, 3,
                    1, 3,
                    1, 1
        });
        final Polygon polygon = factory.createPolygon(points);

        // execution
        final LinkedList<Sample> samples = collector.getSamplesFor(polygon);

        // verification
        assertNotNull(samples);
        assertEquals(expecteds.length, samples.size());
        for (int i = 0; i < expecteds.length; i++) {
            final Sample expected = expecteds[i];
            final Sample actual = samples.get(i);
            assertEquals("Index = " + i, expected.x, actual.x);
            assertEquals("Index = " + i, expected.y, actual.y);
            assertEquals("Index = " + i, expected.lon, actual.lon, 0.000001);
            assertEquals("Index = " + i, expected.lat, actual.lat, 0.000001);
        }
    }

    @Test
    public void testPointInPolygonTest_GeometriesCreatedByFactory() throws Exception {
        final Polygon polygon = factory.createPolygon(createPoints(new double[]{
                    2, 2,
                    6, 2,
                    6, 6,
                    2, 6,
                    2, 2
        }));
        assertTrue(polygon.contains(factory.createPoint(4, 4)));
    }

    private List<Point> createPoints(final double[] lonsLats) {
        final ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < lonsLats.length; i++) {
            double lon = lonsLats[i];
            double lat = lonsLats[++i];
            points.add(factory.createPoint(lon, lat));
        }
        return points;
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
