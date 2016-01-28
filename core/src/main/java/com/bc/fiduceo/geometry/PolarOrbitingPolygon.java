
/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.geometry;


import java.util.ArrayList;
import java.util.List;

/**
 * Point-in-polygon test for polar orbiting satellites with maybe self-overlapping polygon.
 * Splits orbit into two halves, assumes that points start at one corner and that points are
 * more or less equally spaced (such that it is correct to split at 1/4 of points on first
 * side and to look for point nearest to this close to 3/4 of points). <p/>
 * <p>
 * The point-in-polygon test determines one equator crossing of the polygon and turns it
 * by 90 degrees to get a point that is for sure outside of the polygon. Then, the test
 * counts the number of crossings of polygon edges with the meridian between the sampling
 * point and the equator and the number of crossings between this equator point and the 90
 * degree point (using the shorter connection between them). If the number of crossings is
 * odd the point is inside.
 *
 * @author Martin Boettcher, Tom Block
 */
public class PolarOrbitingPolygon {

    // @todo 2 remove this class 2016-01-27
    public static class Point {

        final double lat;
        final double lon;

        Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        double getLat() {

            return lat;
        }

        double getLon() {
            return lon;
        }

    }

    private int id;
    private long time;
    private List<List<Point>> rings = new ArrayList<>();

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    List<List<Point>> getRings() {
        return rings;
    }

    public PolarOrbitingPolygon(int id, long time, Geometry geometry) {
        final com.bc.fiduceo.geometry.Point[] coordinates = geometry.getCoordinates();
        final int numPoints = coordinates.length;
        this.id = id;
        this.time = time;
        // select a point more or less in the middle of a long side, assuming that the short side has about 6 points
        final int middle1 = (numPoints - 12) / 4 + 3;   // TODO move splitting to boundary calculator
        // select a start point more or less in the middel of the other long side
        final int middle2 = findCorrespondingPoint(geometry, middle1, (numPoints - 12) * 3 / 4 + 9);
        if (numPoints > 11 && middle2 > middle1 + 3) {
            rings.add(collectFirstRing(geometry, middle1, middle2));
            rings.add(collectSecondRing(geometry, middle1, middle2));
        } else {
            rings.add(collectFirstRing(geometry, 0, numPoints - 2));
        }
    }

    public boolean isPointInPolygon(double sampleLat, double sampleLon) {
        for (List<Point> ring : rings) {
            if (isPointInRing(sampleLat, sampleLon, ring)) {
                return true;
            }
        }
        return false;
    }

    double shiftIfZero(double degree) {
        return degree == 0.0 ? 1e-8 : degree;
    }

    boolean isPointInRing(double sampleLat, double sampleLon, List<Point> ring) {
        final double equatorLat = 0.0;
        double firstEquatorCrossingLonPlus90 = Double.NaN;
        double transformedSampleLon = Double.NaN;
        boolean isInside = false;

        for (int i = 0; i < ring.size() - 1; ++i) {
            final Point point = ring.get(i);
            final Point nextPoint = ring.get(i + 1);

            final double lon1 = shiftIfZero(normalizeLongitude(point.getLon() - sampleLon));
            final double lat1 = shiftIfZero(point.getLat());
            final double lon2 = shiftIfZero(normalizeLongitude(nextPoint.getLon() - sampleLon));
            final double lat2 = shiftIfZero(nextPoint.getLat());

            if (isEdgeCrossingMeridian(lon1, lon2)) {
                final double crossingLat = getLatitudeAtMeridian(lat1, lon1, lat2, lon2);
                if (isBetween(crossingLat, equatorLat, sampleLat)) {
                    isInside = !isInside;
                }
            }
            if (isEdgeCrossingEquator(lat1, lat2)) {
                final double crossingLon = getLongitudeAtEquator(lat1, point.getLon(), lat2, nextPoint.getLon());
                if (Double.isNaN(firstEquatorCrossingLonPlus90)) {
                    firstEquatorCrossingLonPlus90 = normalizeLongitude(crossingLon + 90.0);
                    transformedSampleLon = normalizeLongitude(sampleLon - firstEquatorCrossingLonPlus90);
                }
                final double transformedCrossingLon = normalizeLongitude(crossingLon - firstEquatorCrossingLonPlus90);
                // TODO - why is isBetween() but not isEdgeCrossingMeridian() used here?
                // see class comment for an explanation (the question here is whether the equator crossing is between the sample projected to the equator and the new 90 degree point)
                if (isBetween(transformedCrossingLon, 0.0, transformedSampleLon)) {
                    isInside = !isInside;
                }
            }
        }
        return isInside;
    }

    // package access for testing only tb 2014-02-04
    static double getLongitudeAtEquator(double lat1, double lon1, double lat2, double lon2) {
        if (lat2 == lat1) {
            return lon1;
        }
        return lon1 + normalizeLongitude(lon2 - lon1) * (0.0 - lat1) / (lat2 - lat1);
    }

    // package access for testing only tb 2014-02-04
    static double getLatitudeAtMeridian(double lat1, double lon1, double lat2, double lon2) {
        if (lon2 == lon1) {
            return lat1;
        }
        return lat1 - (lat2 - lat1) * lon1 / normalizeLongitude(lon2 - lon1);
    }

    static boolean isEdgeCrossingMeridian(double lon1, double lon2) {
        return (lon1 <= 0.0 && lon2 > 0.0 && lon2 - lon1 < 180.0) || (lon1 >= 0.0 && lon2 < 0.0 && lon1 - lon2 < 180.0);
    }

    static boolean isBetween(double value, double from, double to) {
        return (value >= from && value < to) || (value <= from && value > to);
    }

    private int findCorrespondingPoint(Geometry geometry, int middle1, int middle2) {
        final com.bc.fiduceo.geometry.Point[] coordinates = geometry.getCoordinates();
        final com.bc.fiduceo.geometry.Point middle1Coordinate = coordinates[middle1];
        final SphericalDistance middle1DistanceCalculator = new SphericalDistance(middle1Coordinate.getLon(), middle1Coordinate.getLat());

        com.bc.fiduceo.geometry.Point middle2Coordinate = coordinates[middle2];
        double distance = middle1DistanceCalculator.distance(middle2Coordinate.getLon(), middle2Coordinate.getLat());

        /*
        while (middle2 + 2 < geometry.numPoints()) {
            point2 = geometry.getPoint(middle2 + 1);
            final double distance2 = middle1DistanceCalculator.distance(point2.getX(), point2.getY());
            if (distance2 >= distance) {
                break;
            }
            ++middle2;
            distance = distance2;
        }
        while (middle2 - 1 < middle1) {
            point2 = geometry.getPoint(middle2 - 1);
            final double distance2 = middle1DistanceCalculator.distance(point2.getX(), point2.getY());
            if (distance2 >= distance) {
                break;
            }
            --middle2;
            distance = distance2;
        }
        */
        // find nearest point on opposite product border
        final int numPoints = coordinates.length;
        for (int m = numPoints / 2; m + 1 < numPoints; ++m) {
            middle2Coordinate = coordinates[m + 1];
            final double distance2 = middle1DistanceCalculator.distance(middle2Coordinate.getLon(), middle2Coordinate.getLat());
            if (distance2 < distance) {
                middle2 = m;
                distance = distance2;
            }
        }
        return middle2;
    }

    private List<Point> collectFirstRing(Geometry geometry, int middle1, int middle2) {
        final List<Point> ring1 = new ArrayList<>(middle2 - middle1 + 2);
        final com.bc.fiduceo.geometry.Point[] coordinates = geometry.getCoordinates();
        for (int i = middle1; i <= middle2; ++i) {
            ring1.add(new Point(coordinates[i].getLat(), coordinates[i].getLon()));
        }
        ring1.add(ring1.get(0));
        return ring1;
    }

    private List<Point> collectSecondRing(Geometry geometry, int middle1, int middle2) {
        final com.bc.fiduceo.geometry.Point[] coordinates = geometry.getCoordinates();
        final int numPoints = coordinates.length;
        final List<Point> ring2 = new ArrayList<>(middle1 + 1 + numPoints - middle2);
        for (int i = 0; i <= middle1; ++i) {
            ring2.add(new Point(coordinates[i].getLat(), coordinates[i].getLon()));
        }
        for (int i = middle2; i < numPoints - 1; ++i) {
            ring2.add(new Point(coordinates[i].getLat(), coordinates[i].getLon()));
        }
        ring2.add(ring2.get(0));
        return ring2;
    }

    // package access for testing only tb 2014-02-03
    static boolean isEdgeCrossingEquator(double lat1, double lat2) {
        return (lat1 <= 0.0 && lat2 > 0.0) || (lat1 >= 0.0 && lat2 < 0.0);
    }

    static double normalizeLongitude(double lon) {
        return (lon + 180.0 + 720.0) % 360.0 - 180.0;
    }

    ///// TODO: stripped-down BEAM5 classes below, to be replaced with BEAM5 classes when BEAM5 is established /////

    private static final class SphericalDistance {

        private final double lon;
        private final double si;
        private final double co;

        /**
         * Creates a new instance of this class.
         *
         * @param lon The reference longitude of this distance calculator.
         * @param lat The reference latitude of this distance calculator.
         */
        private SphericalDistance(double lon, double lat) {
            this.lon = lon;
            this.si = Math.sin(Math.toRadians(lat));
            this.co = Math.cos(Math.toRadians(lat));
        }

        /**
         * Returns the spherical distance (in Radian) of a given (lon, lat) point to
         * the reference (lon, lat) point.
         *
         * @param lon The longitude.
         * @param lat The latitude.
         * @return the spherical distance (in Radian) of the given (lon, lat) point
         * to the reference (lon, lat) point.
         */
        private double distance(double lon, double lat) {
            final double phi = Math.toRadians(lat);
            return Math.acos(si * Math.sin(phi) + co * Math.cos(phi) * Math.cos(Math.toRadians(lon - this.lon)));
        }
    }

}
