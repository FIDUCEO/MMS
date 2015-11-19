package com.bc.fiduceo.math;

import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Polyline;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TimeAxisS2Test {

    private S2WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new S2WKTReader();
    }

    @Test
    public void testGetIntersectionTime_noIntersection() {
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 2, 2 2, 2 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(0 -2,4 -2)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
        assertNull(timeInterval);
    }

    @Test
    public void testGetIntersectionTime_StraightLineWithSquare(){
        final S2Polygon polygon = (S2Polygon) wktReader.read("POLYGON((0 0, 0 4, 4 4, 4 0, 0 0))");
        final S2Polyline polyline = (S2Polyline) wktReader.read("LINESTRING(-2 0,4 6)");

        final TimeAxisS2 timeAxis = new TimeAxisS2(polyline, new Date(100000000000L), new Date(100001000000L));
        final TimeInterval timeInterval = timeAxis.getIntersectionTime(polygon);
//        assertNotNull(timeInterval);
//        assertTimeIntervalEquals(100000333333L, 100000666666L, timeInterval);
    }

    private void assertTimeIntervalEquals(long expectedStart, long expectedStop, TimeInterval timeInterval) {
        assertEquals(expectedStart, timeInterval.getStartTime().getTime());
        assertEquals(expectedStop, timeInterval.getStopTime().getTime());
    }

}
