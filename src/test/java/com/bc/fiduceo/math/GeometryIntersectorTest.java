package com.bc.fiduceo.math;


import com.bc.fiduceo.core.SatelliteGeometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("ConstantConditions")
public class GeometryIntersectorTest {

    private WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
    }

    @Test
    public void testGetIntersectionTime_noGeometricIntersection() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                "LINESTRING(2 1,2 2, 2 3, 2 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((0 5, 0 4, 0 3, 0 2, 1 2, 1 3, 1 4, 1 5, 0 5))",
                "LINESTRING(0 5, 0 4, 0 3, 0 2)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(Integer.MAX_VALUE, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                "LINESTRING(2 1,2 2, 2 3, 2 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))",
                "LINESTRING(2 6, 2 5, 2 4, 2 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1666L, timeInterval.getStartTime().getTime());
        assertEquals(2000L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending_shiftedLon() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((-4 2, -4 1, -4 0, -4 -1, -4 -2, -3 -2, -3 -1, -3 0, -3 1, -3 2, -4 2))",
                "LINESTRING(-4 2, -4 1, -4 0, -4 -1, -4 -2)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((-2.5 0, -2.5 1, -2.5 2, -2.5 3, -3.5 3, -3.5 2, -3.5 1, -3.5 0, -2.5 0))",
                "LINESTRING(-2.5 0, -2.5 1, -2.5 2, -2.5 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1500L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_noOverlappingTime() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))",
                "LINESTRING(2 6, 2 5, 2 4, 2 3)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(333, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_bothDescending_closerInTime() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 5, 2 4, 2 3, 2 2, 3 2, 3 3, 3 4, 3 5, 2 5))",
                "LINESTRING(2 5, 2 4, 2 3, 2 2)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1333L, timeInterval.getStartTime().getTime());
        assertEquals(1666L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_onSamePlatform() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 4, 2 3, 2 2, 2 1, 3 1, 3 2, 3 3, 3 4, 2 4))",
                "LINESTRING(2 4,2 3, 2 2, 2 1)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(2000L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_intersectingTime() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((3 1, 3 2, 3 3, 4 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                "LINESTRING(3 1, 3 2, 3 3, 3 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 1, 3 2, 4 3, 5 4, 4 5, 3 4, 2 3, 1 2, 2 1))",
                "LINESTRING(2 1, 3 2, 4 3, 5 4)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        final TimeInterval timeInterval = timeInfo.getOverlapInterval();
        assertEquals(1000L, timeInterval.getStartTime().getTime());
        assertEquals(1833L, timeInterval.getStopTime().getTime());
        assertEquals(0, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testGetIntersectionTime_angularIntersection_noIntersectingTime() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((3 1, 3 2, 3 3, 4 4, 2 4, 2 3, 2 2, 2 1, 3 1))",
                "LINESTRING(3 1, 3 2, 3 3, 3 4)", 1900, 2900);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 1, 3 2, 4 3, 5 4, 4 5, 3 4, 2 3, 1 2, 2 1))",
                "LINESTRING(2 1, 3 2, 4 3, 5 4)", 1000, 2000);

        final TimeInfo timeInfo = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(timeInfo.getOverlapInterval());
        assertEquals(67, timeInfo.getMinimalTimeDelta());
    }

    @Test
    public void testCalculateTimeDelta() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, GeometryIntersector.calculateTimeDelta(interval_1, interval_2));
    }

    @Test
    public void testCalculateTimeDelta_intervalsAreReordered() {
        final TimeInterval interval_1 = new TimeInterval(new Date(2500), new Date(2800));
        final TimeInterval interval_2 = new TimeInterval(new Date(3000), new Date(3500));

        assertEquals(200, GeometryIntersector.calculateTimeDelta(interval_2, interval_1));
    }

    private SatelliteGeometry createSatelliteGeometry(String polygon, String line, int startTime, int stopTime) throws ParseException {
        final Polygon polygon_1 = (Polygon) wktReader.read(polygon);
        final LineString lineString_1 = (LineString) wktReader.read(line);
        final TimeAxis timeAxis = new TimeAxis(lineString_1, new Date(startTime), new Date(stopTime));
        return new SatelliteGeometry(polygon_1, timeAxis);
    }
}
