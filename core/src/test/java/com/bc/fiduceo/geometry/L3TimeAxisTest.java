package com.bc.fiduceo.geometry;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class L3TimeAxisTest {

    private L3TimeAxis timeAxis;

    @Before
    public void setUp() {
        final Date startDate = TimeUtils.parse("20190107T000000", "yyyyMMdd'T'HHmmss");
        final Date stopDate = TimeUtils.parse("20190107T235959", "yyyyMMdd'T'HHmmss");

        final GeometryFactory geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
        final Geometry multiLineString = geometryFactory.parse("MULTILINESTRING((-180 0, 180 0), (0 -90, 0 90))");
        timeAxis = new L3TimeAxis(startDate, stopDate, multiLineString);
    }

    @Test
    public void testGetIntersectionTime() {
        final TimeInterval intersectionTime = timeAxis.getIntersectionTime(null);// we don't care about the polygon tb 2022-09-15
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 0, 0, 0, intersectionTime.getStartTime());
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 23, 59, 59, intersectionTime.getStopTime());
    }

    @Test
    public void testGetProjectionTime() {
        final TimeInterval projectionTime = timeAxis.getProjectionTime(null);// we don't care about the geometry tb 2022-09-15
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 0, 0, 0, projectionTime.getStartTime());
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 23, 59, 59, projectionTime.getStopTime());
    }

    @Test
    public void testGetStartTime() {
        final Date startTime = timeAxis.getStartTime();
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 0, 0, 0, startTime);
    }

    @Test
    public void testGetEndTime() {
        final Date endTime = timeAxis.getEndTime();
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 23, 59, 59, endTime);
    }

    @Test
    public void testGetDurationInMillis() {
        assertEquals(86399000, timeAxis.getDurationInMillis());
    }

    @Test
    public void testGetTime() {
        final Date time = timeAxis.getTime(null);// the location is not important tb 2022-09-23
        TestUtil.assertCorrectUTCDate(2019, 1, 7, 11, 59, 59, 500, time);
    }

    @Test
    public void testGetGeometry()  {
        final Geometry geometry = timeAxis.getGeometry();
        assertTrue(geometry instanceof MultiLineString);
    }
}
