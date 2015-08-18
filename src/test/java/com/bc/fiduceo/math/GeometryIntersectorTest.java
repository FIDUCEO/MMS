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

        final TimeInterval interval = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertNull(interval);
    }

    @Test
    public void testGetIntersectionTime_onSameOrbit_ascendingAndDescending() throws ParseException {
        final SatelliteGeometry satelliteGeometry_1 = createSatelliteGeometry("POLYGON((2 1, 3 1, 3 2, 3 3, 3 4, 2 4, 2 3, 2 2, 2 1))",
                "LINESTRING(2 1,2 2, 2 3, 2 4)", 1000, 2000);
        final SatelliteGeometry satelliteGeometry_2 = createSatelliteGeometry("POLYGON((2 6, 2 5, 2 4, 2 3, 3 3, 3 4, 3 5, 3 6, 2 6))",
                "LINESTRING(2 6, 2 5, 2 4, 2 3)", 1000, 2000);

        final TimeInterval interval = GeometryIntersector.getIntersectingInterval(satelliteGeometry_1, satelliteGeometry_2);
        assertEquals(1666L, interval.getStartTime().getTime());
        assertEquals(2000L, interval.getStopTime().getTime());
    }

    private SatelliteGeometry createSatelliteGeometry(String polygon, String line, int startTime, int stopTime) throws ParseException {
        final Polygon polygon_1 = (Polygon) wktReader.read(polygon);
        final LineString lineString_1 = (LineString) wktReader.read(line);
        final TimeAxis timeAxis = new TimeAxis(lineString_1, new Date(startTime), new Date(stopTime));
        return new SatelliteGeometry(polygon_1, timeAxis);
    }
}
