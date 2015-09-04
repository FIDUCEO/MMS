package com.bc.fiduceo.math;


import com.bc.fiduceo.core.SatelliteGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Date;

public class GeometryIntersector {

    public static TimeInfo getIntersectingInterval(SatelliteGeometry satGeometry_1, SatelliteGeometry satGeometry_2) {
        final Geometry geometry_1 = satGeometry_1.getGeometry();
        final Geometry geometry_2 = satGeometry_2.getGeometry();
        final TimeInfo timeInfo = new TimeInfo();

        final Geometry intersection = geometry_1.intersection(geometry_2);
        if (intersection.isEmpty()) {
            return timeInfo;
        }

        final TimeAxis timeAxis_1 = satGeometry_1.getTimeAxis();
        final TimeAxis timeAxis_2 = satGeometry_2.getTimeAxis();

        final Coordinate[] coordinates = intersection.getCoordinates();
        final ArrayList<Date> sensor_1_dates = new ArrayList<>(coordinates.length);
        final ArrayList<Date> sensor_2_dates = new ArrayList<>(coordinates.length);
        for (int i = 0; i < coordinates.length - 1; i++) {
            final Coordinate coordinate = coordinates[i];
            Date time = timeAxis_1.getTime(coordinate);
            if (time != null) {
                sensor_1_dates.add(time);
            }

            time = timeAxis_2.getTime(coordinate);
            if (time != null) {
                sensor_2_dates.add(time);
            }
        }

        final TimeInterval interval_1 = TimeInterval.create(sensor_1_dates);
        final TimeInterval interval_2 = TimeInterval.create(sensor_2_dates);


        final TimeInterval overlapInterval = interval_1.intersect(interval_2);
        timeInfo.setOverlapInterval(overlapInterval);

        if (overlapInterval == null) {
            final int timeDelta = calculateTimeDelta(interval_1, interval_2);
            timeInfo.setMinimalTimeDelta(timeDelta);
        } else {
            timeInfo.setMinimalTimeDelta(0);
        }
        return timeInfo;
    }

    // package access for testing only tb 2015-09-04
    static int calculateTimeDelta(TimeInterval interval_1, TimeInterval interval_2) {
        TimeInterval earlier;
        TimeInterval later;
        if (interval_1.getStartTime().before(interval_2.getStartTime())) {
            earlier = interval_1;
            later = interval_2;
        } else {
            earlier = interval_2;
            later = interval_1;
        }

        return (int) (later.getStartTime().getTime() - earlier.getStopTime().getTime());
    }
}
