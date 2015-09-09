package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

import java.util.ArrayList;
import java.util.List;

class BoundingPolygonCreator {

    private final int intervalX;
    private final int intervalY;
    private final GeometryFactory geometryFactory;
    private final Polygon westShiftedGlobe;
    private final Polygon eastShiftedGlobe;
    private final Polygon centralGlobe;

    BoundingPolygonCreator(int intervalX, int intervalY) {
        this.intervalX = intervalX;
        this.intervalY = intervalY;
        geometryFactory = new GeometryFactory();

        westShiftedGlobe = createWestShiftedGlobe();
        eastShiftedGlobe = createEastShiftedGlobe();
        centralGlobe = createCentralGlobe();
    }

    public SatelliteGeometry createPixelCodedBoundingPolygon(ArrayDouble.D2 arrayLatitude, ArrayDouble.D2 arrayLongitude, NodeType nodeType) {
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

        final Coordinate[] coordinatesArray = coordinates.toArray(new Coordinate[coordinates.size()]);
        normalizePolygon(coordinatesArray);
        final Polygon polygon = geometryFactory.createPolygon(coordinatesArray);

        // @todo 1 tb/tb move the code below to a different class, this one should only extract the bounding geometry and the axis indices ... 2015-09-09
//        final Coordinate[] normalizedCoordinates = polygon.getCoordinates();
//        final List<Coordinate> timeAxisPoints = new ArrayList<>();
//        for (int i = timeAxisStart; i <= timeAxisEnd; i++) {
//            timeAxisPoints.add(normalizedCoordinates[i]);
//        }


        final Geometry westShifted = (Geometry) westShiftedGlobe.intersection(polygon).clone();
        westShifted.apply(new LonShifter(360.0));
        System.out.println("west shift = " + westShifted);

        final Geometry eastShifted = (Geometry) eastShiftedGlobe.intersection(polygon).clone();
        eastShifted.apply(new LonShifter(-360.0));
        System.out.println("east shift = " + eastShifted);

        final Geometry central = centralGlobe.intersection(polygon);
        System.out.println("central = " + central);


        final SatelliteGeometry satelliteGeometry = new SatelliteGeometry(polygon, null);
        satelliteGeometry.setTimeAxisStartIndex(timeAxisStart);
        satelliteGeometry.setTimeAxisEndIndex(timeAxisEnd);
        return satelliteGeometry;
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

    static void closePolygon(List<Coordinate> coordinates) {
        if (coordinates.size() > 1) {
            coordinates.add(coordinates.get(0));
        }
    }

    public Geometry createIASIBoundingPolygon(ArrayFloat.D2 arrayLatitude, ArrayFloat.D2 arrayLongitude) {
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
        return geometryFactory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

    private Polygon createCentralGlobe() {
        final Coordinate[] unShiftedCoordinates = new Coordinate[5];
        unShiftedCoordinates[0] = new Coordinate(-180, 90);
        unShiftedCoordinates[1] = new Coordinate(-180, -90);
        unShiftedCoordinates[2] = new Coordinate(180, -90);
        unShiftedCoordinates[3] = new Coordinate(180, 90);
        unShiftedCoordinates[4] = new Coordinate(-180, 90);
        return geometryFactory.createPolygon(unShiftedCoordinates);
    }

    private Polygon createEastShiftedGlobe() {
        final Coordinate[] easternShiftedCoordinates = new Coordinate[5];
        easternShiftedCoordinates[0] = new Coordinate(180, 90);
        easternShiftedCoordinates[1] = new Coordinate(180, -90);
        easternShiftedCoordinates[2] = new Coordinate(540, -90);
        easternShiftedCoordinates[3] = new Coordinate(540, 90);
        easternShiftedCoordinates[4] = new Coordinate(180, 90);
        return geometryFactory.createPolygon(easternShiftedCoordinates);
    }

    private Polygon createWestShiftedGlobe() {
        final Coordinate[] westernShiftedCoordinates = new Coordinate[5];
        westernShiftedCoordinates[0] = new Coordinate(-540, 90);
        westernShiftedCoordinates[1] = new Coordinate(-540, -90);
        westernShiftedCoordinates[2] = new Coordinate(-180, -90);
        westernShiftedCoordinates[3] = new Coordinate(-180, 90);
        westernShiftedCoordinates[4] = new Coordinate(-540, 90);
        return geometryFactory.createPolygon(westernShiftedCoordinates);
    }
}
