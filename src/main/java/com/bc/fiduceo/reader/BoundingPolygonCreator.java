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
        int geoXTrack = shape[1] - 1;
        int geoTrack = shape[0] - 1;

        List<Coordinate> coordinates = new ArrayList<>();

        int timeAxisStart;
        int timeAxisEnd;
        if (nodeType == NodeType.ASCENDING) {
            for (int x = 0; x < geoXTrack; x += intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
            }

            timeAxisStart = coordinates.size();
            timeAxisEnd = timeAxisStart;
            for (int y = 0; y < geoTrack; y += intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, geoXTrack), arrayLatitude.get(y, geoXTrack)));
                ++timeAxisEnd;
            }

            for (int x = geoXTrack; x > 0; x -= intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, x), arrayLatitude.get(geoTrack, x)));
            }

            for (int y = geoTrack; y > 0; y -= intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
            }
        } else {
            timeAxisStart = 0;
            timeAxisEnd = timeAxisStart;
            for (int y = 0; y < geoTrack; y += intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, geoXTrack), arrayLatitude.get(y, geoXTrack)));
                ++timeAxisEnd;
            }

            for (int x = geoXTrack; x > 0; x -= intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(geoTrack, x), arrayLatitude.get(geoTrack, x)));
            }

            for (int y = geoTrack; y > 0; y -= intervalY) {
                coordinates.add(new Coordinate(arrayLongitude.get(y, 0), arrayLatitude.get(y, 0)));
            }

            for (int x = 0; x < geoXTrack; x += intervalX) {
                coordinates.add(new Coordinate(arrayLongitude.get(0, x), arrayLatitude.get(0, x)));
            }
        }

        // close the polygon
        closePolygon(coordinates);

        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setCoordinates(coordinates);
        acquisitionInfo.setTimeAxisStartIndex(timeAxisStart);
        acquisitionInfo.setTimeAxisEndIndex(timeAxisEnd);

        return acquisitionInfo;
    }

    static void closePolygon(List<Coordinate> coordinates) {
        if (coordinates.size() > 1) {
            coordinates.add(coordinates.get(0));
        }
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
}
