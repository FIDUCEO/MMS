
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

import com.bc.fiduceo.core.NodeType;
import com.vividsolutions.jts.geom.Coordinate;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

import java.util.ArrayList;
import java.util.List;

class BoundingPolygonCreator {

    private final int intervalX;
    private final int intervalY;

    BoundingPolygonCreator(int intervalX, int intervalY) {
        this.intervalX = intervalX;
        this.intervalY = intervalY;
    }

    public AcquisitionInfo createPixelCodedBoundingPolygon(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude, NodeType nodeType) {
        final int[] shape = arrayLatitude.getShape();
        int width = shape[1] - 1;
        int height = shape[0] - 1;

        List<Coordinate> coordinates = new ArrayList<>();

        int[] timeAxisStart = new int[2];
        int[] timeAxisEnd = new int[2];
        if (nodeType == NodeType.ASCENDING) {
            for (int x = 0; x < width; x += intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
            }

            timeAxisStart[0] = coordinates.size();
            timeAxisEnd[0] = timeAxisStart[0];
            for (int y = 0; y < height; y += intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, width), arrayLatitude.get(y, width)));
                ++timeAxisEnd[0];
            }

            for (int x = width; x > 0; x -= intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(height, x), arrayLatitude.get(height, x)));
            }

            for (int y = height; y > 0; y -= intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
            }
        } else {
            timeAxisStart[0] = 0;
            timeAxisEnd[0] = 0;
            for (int y = 0; y < height; y += intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, width), arrayLatitude.get(y, width)));
                ++timeAxisEnd[0];
            }

            for (int x = width; x > 0; x -= intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(height, x), arrayLatitude.get(height, x)));
            }

            for (int y = height; y > 0; y -= intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
            }

            for (int x = 0; x < width; x += intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
            }
        }

        // close the polygon
        closePolygon(coordinates);

        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setCoordinates(coordinates);
        acquisitionInfo.setTimeAxisStartIndices(timeAxisStart);
        acquisitionInfo.setTimeAxisEndIndices(timeAxisEnd);

        return acquisitionInfo;
    }

    // @todo 1 tb/tb add time axis tracking
    public AcquisitionInfo createIASIBoundingPolygon(ArrayFloat.D2 arrayLatitude, ArrayFloat.D2 arrayLongitude) {
        final int geoXTrack = arrayLatitude.getShape()[1] - 1;
        final int geoTrack = arrayLatitude.getShape()[0] - 1;
        final List<Coordinate> coordinates = new ArrayList<>();

        coordinates.add(new Coordinate(arrayLongitude.get(0, 0), arrayLatitude.get(0, 0)));

        for (int x = 1; x < geoXTrack; x += intervalX) {
            coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
        }

        for (int y = 0; y <= geoTrack; y += intervalY) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, geoXTrack), arrayLatitude.get(y, geoXTrack)));
            if ((y + intervalY) > geoTrack) {
                coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, geoXTrack), arrayLatitude.get(geoTrack, geoXTrack)));
            }
        }

        for (int x = geoXTrack - 1; x > 0; x -= intervalX) {
            coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, x), arrayLatitude.get(geoTrack, x)));
        }

        for (int y = geoTrack; y >= 0; y -= intervalY) {
            coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
        }

        closePolygon(coordinates);

        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setCoordinates(coordinates);
        return acquisitionInfo;
    }

    static void closePolygon(List<Coordinate> coordinates) {
        if (coordinates.size() > 1) {
            coordinates.add(coordinates.get(0));
        }
    }
}
