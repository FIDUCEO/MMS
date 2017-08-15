/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.geometry;

import org.esa.snap.core.datamodel.Rotator;
import org.esa.snap.core.util.math.RsMathUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PaddingFactory {

    private final GeometryFactory geometryFactory;
    private final double halfDistInDegree;
    private Point2D.Double p1;
    private Point2D.Double p2;

    private PaddingFactory(GeometryFactory geometryFactory, final double sensorPixelWidthKm) {
        this.geometryFactory = geometryFactory;
        final double halfWidthKm = sensorPixelWidthKm / 2;
        final double halfDistInMeters = halfWidthKm * 1000;
        halfDistInDegree = Math.toDegrees(halfDistInMeters / RsMathUtils.MEAN_EARTH_RADIUS);
        p1 = new Point2D.Double();
        p2 = new Point2D.Double();
    }

    /**
     * Creates a bounding polygon around the given {@link com.bc.fiduceo.geometry.LineString} coordinates.
     * @param lineString the LineString to envelope
     * @param envelopeWithKm the full width. Padding will be applied using half width.
     * @param geometryFactory instance to create discrete Geometry instances.
     * @return a polygon geometry which encloses the input lineString
     */
    public static Polygon createLinePadding(LineString lineString, double envelopeWithKm, GeometryFactory geometryFactory) {
        final Point[] coordinates = lineString.getCoordinates();
        final PaddingFactory paddingFactory = new PaddingFactory(geometryFactory, envelopeWithKm);
        final int size = coordinates.length;
        final ArrayList<Point> polygonPoints = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Point p1;
            final Point p2;
            if (i == 0) {
                p1 = coordinates[i];
                p2 = coordinates[i + 1];
            } else {
                p1 = coordinates[i];
                p2 = coordinates[i - 1];
            }
            final double lon = p1.getLon();
            final double lat = p1.getLat();
            final LonLat ll_1 = new LonLat(lon, lat);
            final LonLat ll_2 = new LonLat(p2.getLon(), p2.getLat());
            double alpha = ll_1.bearingTo(ll_2);

            final FIL fil;
            if (i == 0) {
                fil = FIL.first;
            } else if (i == size - 1) {
                fil = FIL.last;
            } else {
                fil = FIL.inter;
            }

            final Point[] points = paddingFactory.createPoints(lon, lat, alpha, fil);
            polygonPoints.add(i, points[1]);
            polygonPoints.add(i + 1, points[0]);

        }

        return geometryFactory.createPolygon(polygonPoints);
    }

    private Point[] createPoints(double lon, double lat, double alpha, FIL fil) {

        double latPos;
        if (FIL.inter == fil) {
            latPos = 0;
        } else {
            latPos = -halfDistInDegree;
        }
        if (FIL.first == fil) {
            p1.setLocation(-halfDistInDegree, latPos);
            p2.setLocation(halfDistInDegree, latPos);
        } else {
            p1.setLocation(halfDistInDegree, latPos);
            p2.setLocation(-halfDistInDegree, latPos);
        }
        Rotator r = new Rotator(lon, lat, alpha);
        r.transformInversely(p1);
        r.transformInversely(p2);
        return new Point[]{
                geometryFactory.createPoint(p1.getX(), p1.getY()),
                geometryFactory.createPoint(p2.getX(), p2.getY())
        };
    }

    private enum FIL {first, inter, last}

    /**
     * Longitude/latitude spherical geodesy tools
     * (c) Chris Veness 2002-2016
     * MIT Licence
     * www.movable-type.co.uk/scripts/latlong.html
     * www.movable-type.co.uk/scripts/geodesy/docs/module-latlon-spherical.html
     */
    static class LonLat {

        public final double lon;
        public final double lat;

        /**
         * Creates a LonLon point on the earth's surface at the specified longitude / latitude.
         * <p>
         * Example:
         * <pre>
         * LonLat p1 = new LonLat(0.119, 52.205);
         * </pre>
         *
         * @param lon Longitude in degrees.
         * @param lat Latitude in degrees.
         */
        LonLat(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

        /**
         * Returns the (initial) bearing from ‘this’ point to destination point.
         * <p>
         * Example:
         * <pre>
         * LonLat p1 = new LonLat(0.119, 52.205);
         * LonLat p2 = new LonLat(2.351, 48.857);
         * double b1 = p1.bearingTo(p2); // 156.2°
         * </pre>
         *
         * @param to {@link LonLat point} ... Longitude/latitude of destination point.
         *
         * @return Initial bearing in degrees from north.
         */
        public double bearingTo(LonLat to) {
            final double phi_1 = Math.toRadians(lat);
            final double phi_2 = Math.toRadians(to.lat);
            final double deltaLambda = Math.toRadians(to.lon - this.lon);

            final double y = Math.sin(deltaLambda) * Math.cos(phi_2);
            final double x = Math.cos(phi_1) * Math.sin(phi_2) -
                             Math.sin(phi_1) * Math.cos(phi_2) * Math.cos(deltaLambda);
            final double theta = Math.atan2(y, x);
            return (Math.toDegrees(theta) + 360.0) % 360.0;
        }

        /**
         * Returns final bearing arriving at destination destination point from ‘this’ point; the final bearing
         * will differ from the initial bearing by varying degrees according to distance and latitude.
         * <p>
         * Example:
         * <pre>
         * LonLat p1 = new LonLat(0.119, 52.205);
         * LonLat p2 = new LonLat(2.351, 48.857);
         * double b1 = p1.finalBearingTo(p2); // 157.9°
         * </pre>
         *
         * @param to {@link LonLat point} ... Longitude/latitude of destination point.
         *
         * @return Final bearing in degrees from north.
         */
        public double finalBearingTo(LonLat to) {
            return (to.bearingTo(this) + 180) % 360;
        }
    }
}
