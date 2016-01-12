
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

package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class GeometryUtils {

    private static final com.bc.fiduceo.geometry.GeometryFactory geoFactory;

    static {
        // @todo 2 tb/tb move this to a common place, we should switch factories at one point onöy 2015-12-03
        geoFactory = new com.bc.fiduceo.geometry.GeometryFactory(com.bc.fiduceo.geometry.GeometryFactory.Type.JTS);
    }

    // @todo 1 tb/tb adapt to multiple time axes 2015-11-18
    static SatelliteGeometry prepareForStorage(AcquisitionInfo acquisitionInfo) {
        final List<Point> coordinates = acquisitionInfo.getCoordinates();
        final com.bc.fiduceo.geometry.Polygon polygon = geoFactory.createPolygon(coordinates);
        final TimeAxis timeAxis = createTimeAxis(polygon,
                acquisitionInfo.getTimeAxisStartIndices()[0],
                acquisitionInfo.getTimeAxisEndIndices()[0],
                acquisitionInfo.getSensingStart(),
                acquisitionInfo.getSensingStop());
        return new SatelliteGeometry(polygon, new TimeAxis[]{timeAxis});
    }

    static TimeAxis createTimeAxis(com.bc.fiduceo.geometry.Polygon polygon, int startIndex, int endIndex, Date startTime, Date endTime) {
        final Point[] polygonCoordinates = polygon.getCoordinates();
        final Point[] coordinates = new Point[endIndex - startIndex + 1];
        System.arraycopy(polygonCoordinates, startIndex, coordinates, 0, endIndex + 1 - startIndex);

        // @todo 3 tb/tb decide here: either you stick to arrays or to lists - but not use both 2015-12-04
        final ArrayList<Point> coordinateList = new ArrayList<>(coordinates.length);
        Collections.addAll(coordinateList, coordinates);
        final com.bc.fiduceo.geometry.LineString lineString = geoFactory.createLineString(coordinateList);
        return geoFactory.createTimeAxis(lineString, startTime, endTime);
    }
}
