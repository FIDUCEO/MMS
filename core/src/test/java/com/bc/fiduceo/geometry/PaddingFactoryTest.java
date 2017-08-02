package com.bc.fiduceo.geometry;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.esa.snap.core.datamodel.Rotator;
import org.esa.snap.core.util.math.RsMathUtils;
import org.junit.*;

import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * Created by Sabine on 02.08.2017.
 */
public class PaddingFactoryTest {

    private static final double MEAN_EARTH_RADIUS_KM = RsMathUtils.MEAN_EARTH_RADIUS * 0.001;
    private static final double ENVELOPE_WIDTH_KM = 5;
    private static final double ENVELOPE_WIDTH_RADIAN = ENVELOPE_WIDTH_KM / MEAN_EARTH_RADIUS_KM;
    private static final double PADDING_DIST_DEGREE = Math.toDegrees(ENVELOPE_WIDTH_RADIAN) / 2;
    private static final GeometryFactory GF = new GeometryFactory(GeometryFactory.Type.S2);
    private static final double DELTA = 1e-8;
    private Point[] points;
    private Point[] expectedEnvelope;

    @Before
    public void setUp() throws Exception {
        points = new Point[]{
                GF.createPoint(-10, 0),
                GF.createPoint(0, 0),
                GF.createPoint(10, 0)
        };

        final Point[] p = this.points;
        double d = PADDING_DIST_DEGREE;

        expectedEnvelope = new Point[6];
        expectedEnvelope[0] = GF.createPoint(p[0].getLon() - d, p[0].getLat() + d);
        expectedEnvelope[5] = GF.createPoint(p[0].getLon() - d, p[0].getLat() - d);
        expectedEnvelope[1] = GF.createPoint(p[1].getLon() - 0, p[1].getLat() + d);
        expectedEnvelope[4] = GF.createPoint(p[1].getLon() - 0, p[1].getLat() - d);
        expectedEnvelope[2] = GF.createPoint(p[2].getLon() + d, p[2].getLat() + d);
        expectedEnvelope[3] = GF.createPoint(p[2].getLon() + d, p[2].getLat() - d);
    }

    @Test
    public void createLinePadding_alongTheEquator_0DegreeRotation() throws Exception {
        //execution
        Point[] coordinates = createEnvelope(points, "0/0/0");
        //verification
        assertEnvelope(expectedEnvelope, coordinates);
    }

    @Test
    public void createLinePadding_90Degree() throws Exception {
        //preparation
        final Rotator rotator = new Rotator(0, 0, 90);
        transform(points, rotator);
        transform(expectedEnvelope, rotator);
        //execution
        Point[] envelope = createEnvelope(points, "0, 0, 90");
        //verification
        assertEnvelope(expectedEnvelope, envelope);
    }

    @Test
    public void createLinePadding_65Degree_Location_Hamburg_Germany() throws Exception {
        //preparation
        final Rotator rotator = new Rotator(9.993682, 53.551085, 45);
        transform(points, rotator);
        transform(expectedEnvelope, rotator);
        //execution
        Point[] envelope = createEnvelope(points, "Hamburg 45°");
        //verification
        assertEnvelope(expectedEnvelope, envelope);
    }

    private void transform(Point[] input, Rotator rotator) {
        Point2D.Double p = new Point2D.Double();
        for (int i = 0; i < input.length; i++) {
            Point point = input[i];
            p.setLocation(point.getLon(), point.getLat());
            rotator.transformInversely(p);
            point.setLon(p.x);
            point.setLat(p.y);
        }
    }

    private Point[] createEnvelope(Point[] input, String locationName) {
        final LineString lineString = GF.createLineString(Arrays.asList(input));
        final Polygon envelope = PaddingFactory.createLinePadding(lineString, ENVELOPE_WIDTH_KM, GF);
        System.out.println("envelope at " + locationName + " = " + envelope);
        Point[] coordinates = envelope.getCoordinates();
        coordinates = Arrays.copyOf(coordinates, coordinates.length - 1); // remove last Point because it is equal with first point
        return coordinates;
    }

    private void assertEnvelope(Point[] expectedEnvelope, Point[] envelope) {
        assertThat(envelope.length, is(expectedEnvelope.length));
        for (int i = 0; i < envelope.length; i++) {
            Point enP = envelope[i];
            Point exP = expectedEnvelope[i];
            String message = "fail at index = " + i;
            assertEquals(message, exP.getLon(), enP.getLon(), DELTA);
            assertEquals(message, exP.getLat(), enP.getLat(), DELTA);
        }
    }
}