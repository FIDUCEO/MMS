
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
import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class GeometryUtils {

    private static final com.bc.fiduceo.geometry.Polygon westShiftedGlobe;
    private static final com.bc.fiduceo.geometry.Polygon eastShiftedGlobe;
    private static final com.bc.fiduceo.geometry.Polygon centralGlobe;
    private static final com.bc.fiduceo.geometry.GeometryFactory geoFactory;

    static {
        // @todo 2 tb/tb move this to a common place, we should switch factories at one point onöy 2015-12-03
        geoFactory = new com.bc.fiduceo.geometry.GeometryFactory(com.bc.fiduceo.geometry.GeometryFactory.Type.JTS);

        westShiftedGlobe = createWestShiftedGlobe();
        eastShiftedGlobe = createEastShiftedGlobe();
        centralGlobe = createCentralGlobe();
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

    static void normalizePolygon(Coordinate[] coordinates) {
        if (coordinates.length < 2) {
            return;
        }

        final double[] originalLon = new double[coordinates.length];
        for (int i = 0; i < originalLon.length; i++) {
            originalLon[i] = coordinates[i].x;
        }

        double lonDiff;
        double increment = 0.f;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (int i = 1; i < coordinates.length; i++) {
            final Coordinate coordinate = coordinates[i];

            lonDiff = originalLon[i] - originalLon[i - 1];
            if (lonDiff > 180.0F) {
                increment -= 360.0;
            } else if (lonDiff < -180.0) {
                increment += 360.0;
            }

            coordinate.x += increment;
            if (coordinate.x < minLon) {
                minLon = coordinate.x;
            }
            if (coordinate.x > maxLon) {
                maxLon = coordinate.x;
            }
        }

        boolean negNormalized = false;
        boolean posNormalized = false;

        if (minLon < -180.0) {
            posNormalized = true;
        }
        if (maxLon > 180.0) {
            negNormalized = true;
        }

        if (!negNormalized && posNormalized) {
            for (final Coordinate coordinate : coordinates) {
                coordinate.x += 360.0;
            }
        }
    }

    // @todo 2 tb/tb this functionality is completely JTS stuff - move to jts package. Introduce a "prepareForStorage" method
    // that does all this and keep empty implementation for S2 2015-12-03
    static com.bc.fiduceo.geometry.Polygon[] mapToGlobe(com.bc.fiduceo.geometry.Geometry geometry) {
        final ArrayList<com.bc.fiduceo.geometry.Polygon> geometries = new ArrayList<>();
        final com.bc.fiduceo.geometry.Polygon westShifted = (com.bc.fiduceo.geometry.Polygon) westShiftedGlobe.intersection(geometry);
        if (!westShifted.isEmpty()) {
            westShifted.shiftLon(360.0);
            geometries.add(westShifted);
        }

        final com.bc.fiduceo.geometry.Polygon central = (com.bc.fiduceo.geometry.Polygon) centralGlobe.intersection(geometry);
        if (!central.isEmpty()) {
            geometries.add(central);
        }

        final com.bc.fiduceo.geometry.Polygon eastShifted = (com.bc.fiduceo.geometry.Polygon) eastShiftedGlobe.intersection(geometry);
        if (!eastShifted.isEmpty()) {
            eastShifted.shiftLon(-360.0);
            geometries.add(eastShifted);
        }

        return geometries.toArray(new com.bc.fiduceo.geometry.Polygon[geometries.size()]);
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

    private static com.bc.fiduceo.geometry.Polygon createCentralGlobe() {
        final List<Point> pointList = new ArrayList<>(5);
        pointList.add(geoFactory.createPoint(-180, 90));
        pointList.add(geoFactory.createPoint(-180, -90));
        pointList.add(geoFactory.createPoint(180, -90));
        pointList.add(geoFactory.createPoint(180, 90));
        pointList.add(geoFactory.createPoint(-180, 90));
        return geoFactory.createPolygon(pointList);
    }

    private static com.bc.fiduceo.geometry.Polygon createEastShiftedGlobe() {
        final List<Point> pointList = new ArrayList<>(5);
        pointList.add(geoFactory.createPoint(180, 90));
        pointList.add(geoFactory.createPoint(180, -90));
        pointList.add(geoFactory.createPoint(540, -90));
        pointList.add(geoFactory.createPoint(540, 90));
        pointList.add(geoFactory.createPoint(180, 90));
        return geoFactory.createPolygon(pointList);
    }

    private static com.bc.fiduceo.geometry.Polygon createWestShiftedGlobe() {
        final List<Point> pointList = new ArrayList<>(5);
        pointList.add(geoFactory.createPoint(-540, 90));
        pointList.add(geoFactory.createPoint(-540, -90));
        pointList.add(geoFactory.createPoint(-180, -90));
        pointList.add(geoFactory.createPoint(-180, 90));
        pointList.add(geoFactory.createPoint(-540, 90));
        return geoFactory.createPolygon(pointList);
    }
}
