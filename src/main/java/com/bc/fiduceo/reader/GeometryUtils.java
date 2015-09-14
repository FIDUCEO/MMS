package com.bc.fiduceo.reader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;

class GeometryUtils {

    private static final Polygon westShiftedGlobe;
    private static final Polygon eastShiftedGlobe;
    private static final Polygon centralGlobe;

    static {
        final GeometryFactory geometryFactory = new GeometryFactory();

        westShiftedGlobe = createWestShiftedGlobe(geometryFactory);
        eastShiftedGlobe = createEastShiftedGlobe(geometryFactory);
        centralGlobe = createCentralGlobe(geometryFactory);
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

    static Polygon[] mapToGlobe(Geometry geometry) {
        final ArrayList<Polygon> geometries = new ArrayList<>();
        final Polygon westShifted = (Polygon) westShiftedGlobe.intersection(geometry).clone();
        if (!westShifted.isEmpty()) {
            westShifted.apply(new LonShifter(360.0));
            geometries.add(westShifted);
        }

        final Polygon central = (Polygon)centralGlobe.intersection(geometry).clone();
        if (!central.isEmpty()) {
            geometries.add(central);
        }

        final Polygon eastShifted = (Polygon) eastShiftedGlobe.intersection(geometry).clone();
        if (!eastShifted.isEmpty()) {
            eastShifted.apply(new LonShifter(-360.0));
            geometries.add(eastShifted);
        }

        return geometries.toArray(new Polygon[geometries.size()]);
    }

    private static Polygon createCentralGlobe(GeometryFactory geometryFactory) {
        final Coordinate[] unShiftedCoordinates = new Coordinate[5];
        unShiftedCoordinates[0] = new Coordinate(-180, 90);
        unShiftedCoordinates[1] = new Coordinate(-180, -90);
        unShiftedCoordinates[2] = new Coordinate(180, -90);
        unShiftedCoordinates[3] = new Coordinate(180, 90);
        unShiftedCoordinates[4] = new Coordinate(-180, 90);
        return geometryFactory.createPolygon(unShiftedCoordinates);
    }

    private static Polygon createEastShiftedGlobe(GeometryFactory geometryFactory) {
        final Coordinate[] easternShiftedCoordinates = new Coordinate[5];
        easternShiftedCoordinates[0] = new Coordinate(180, 90);
        easternShiftedCoordinates[1] = new Coordinate(180, -90);
        easternShiftedCoordinates[2] = new Coordinate(540, -90);
        easternShiftedCoordinates[3] = new Coordinate(540, 90);
        easternShiftedCoordinates[4] = new Coordinate(180, 90);
        return geometryFactory.createPolygon(easternShiftedCoordinates);
    }

    private static Polygon createWestShiftedGlobe(GeometryFactory geometryFactory) {
        final Coordinate[] westernShiftedCoordinates = new Coordinate[5];
        westernShiftedCoordinates[0] = new Coordinate(-540, 90);
        westernShiftedCoordinates[1] = new Coordinate(-540, -90);
        westernShiftedCoordinates[2] = new Coordinate(-180, -90);
        westernShiftedCoordinates[3] = new Coordinate(-180, 90);
        westernShiftedCoordinates[4] = new Coordinate(-540, 90);
        return geometryFactory.createPolygon(westernShiftedCoordinates);
    }
}
