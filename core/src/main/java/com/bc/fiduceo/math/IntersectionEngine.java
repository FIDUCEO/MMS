
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.math;


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IntersectionEngine {

    public static Intersection[] getIntersectingIntervals(SatelliteObservation primaryObservation, SatelliteObservation secondaryObservation) {
        final Geometry[] primaryGeometries = getGeometryArray(primaryObservation);
        final Geometry[] secondaryGeometries = getGeometryArray(secondaryObservation);

        final TimeAxis[] primaryTimeAxes = primaryObservation.getTimeAxes();
        final TimeAxis[] secondaryTimeAxes = secondaryObservation.getTimeAxes();

        final List<Intersection> intersectionList = new ArrayList<>();
        for (int primaryIndex = 0; primaryIndex < primaryGeometries.length; primaryIndex++) {
            for (int secondaryIndex = 0; secondaryIndex < secondaryGeometries.length; secondaryIndex++) {
                final Geometry primaryGeometry = primaryGeometries[primaryIndex];
                final Geometry secondaryGeometry = secondaryGeometries[secondaryIndex];
                final Intersection intersection = getIntersection(primaryGeometry, secondaryGeometry, primaryTimeAxes[primaryIndex], secondaryTimeAxes[secondaryIndex]);
                if (intersection != null) {
                    intersection.setPrimaryGeometry(primaryGeometry);
                    intersection.setSecundaryGeometry(secondaryGeometry);
                    intersectionList.add(intersection);
                }
            }
        }
        return intersectionList.toArray(new Intersection[intersectionList.size()]);
    }

    private static Geometry[] getGeometryArray(SatelliteObservation observation) {
        Geometry[] geometries;
        final Geometry primaryGeometry = observation.getGeoBounds();
        if (primaryGeometry instanceof GeometryCollection) {
            final GeometryCollection primaryCollection = (GeometryCollection) primaryGeometry;
            geometries = primaryCollection.getGeometries();
        } else {
            geometries = new Geometry[]{primaryGeometry};
        }
        return geometries;
    }

    private static Intersection getIntersection(Geometry primaryGeometry, Geometry secondaryGeometry, TimeAxis primaryTimeAxis, TimeAxis secondaryTimeAxis) {
        final TimeInfo timeInfo = new TimeInfo();
        final Geometry intersectionGeometry = primaryGeometry.getIntersection(secondaryGeometry);
        if (intersectionGeometry.isEmpty()) {
            return null;
        }

        final Point[] coordinates = intersectionGeometry.getCoordinates();
        final ArrayList<Date> primarySensorTimes = new ArrayList<>(coordinates.length);
        final ArrayList<Date> secondarySensorTimes = new ArrayList<>(coordinates.length);
        for (int i = 0; i < coordinates.length - 1; i++) {
            final Point coordinate = coordinates[i];
            Date time = primaryTimeAxis.getTime(coordinate);
            if (time != null) {
                primarySensorTimes.add(time);
            }

            time = secondaryTimeAxis.getTime(coordinate);
            if (time != null) {
                secondarySensorTimes.add(time);
            }
        }

        final TimeInterval primaryCommonInterval = TimeInterval.create(primarySensorTimes);
        final TimeInterval secondaryCommonInterval = TimeInterval.create(secondarySensorTimes);

        final TimeInterval overlapInterval = primaryCommonInterval.intersect(secondaryCommonInterval);
        if (overlapInterval == null) {
            final int timeDelta = calculateTimeDelta(primaryCommonInterval, secondaryCommonInterval);
            timeInfo.setMinimalTimeDelta(timeDelta);
        } else {
            timeInfo.setMinimalTimeDelta(0);
            timeInfo.setOverlapInterval(overlapInterval);
        }

        final Intersection intersection = new Intersection();
        intersection.setGeometry(intersectionGeometry);
        intersection.setTimeInfo(timeInfo);
        return intersection;
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
