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

        final int timeDelta = (int) Math.abs(interval_1.getStartTime().getTime() - interval_2.getStartTime().getTime());

        timeInfo.setTimeInterval(interval_1.intersect(interval_2));
        timeInfo.setMinimalTimeDelta(timeDelta);
        return timeInfo;
    }
}
