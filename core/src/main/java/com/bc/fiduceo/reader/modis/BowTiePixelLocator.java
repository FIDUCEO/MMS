/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.modis;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.math.SphericalDistance;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.TiePointGeoCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.util.math.IndexValidator;
import org.esa.snap.core.util.math.Range;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

class BowTiePixelLocator implements PixelLocator {

    private static final int STRIPE_HEIGHT = 2;

    private final GeometryFactory geometryFactory;
    private Array longitudes;
    private Array latitudes;

    private List<GeoCoding> geoCodingList;
    private List<LineString> centerLinesList;
    private int sceneWidth;
    private int sceneHeight;
    private int listSize;


    BowTiePixelLocator(Array longitudes, Array latitudes, GeometryFactory geometryFactory) throws InvalidRangeException {
        this.geometryFactory = geometryFactory;
        this.longitudes = longitudes;
        this.latitudes = latitudes;

        init();
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        if (x < 0 || y < 0 || x > sceneWidth || y > sceneHeight) {
            return null;
        }
        int index = (int) Math.floor(y / STRIPE_HEIGHT);
        if (index == listSize) {
            index--;
        }
        final GeoCoding geoCoding = geoCodingList.get(index);
        if (geoCoding == null) {
            return null;
        }

        final double geoCodingRelativeY = y - 2 * index;
        final GeoPos geoPos = geoCoding.getGeoPos(new PixelPos(x, geoCodingRelativeY), null);

        if (point == null) {
            point = new Point2D.Double();
        }
        point.setLocation(geoPos.getLon(), geoPos.getLat());
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final SphericalDistance sphericalDistance = new SphericalDistance(lon, lat);
        int minIndex = 0;
        int currentIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (final LineString lineString : centerLinesList) {
            if (lineString != null) {
                final Point[] lineCoordinates = lineString.getCoordinates();
                final int centerIndex = lineCoordinates.length / 2;
                final Point centerPoint = lineCoordinates[centerIndex];

                final double currentDistance = sphericalDistance.distance(centerPoint.getLon(), centerPoint.getLat());
                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                    minIndex = currentIndex;
                }
            }
            ++currentIndex;
        }

        // check minIndex - 1 to minIndex + 1, if inside product
        final int[] subSearchIndices = new int[3];
        subSearchIndices[0] = minIndex - 1 >= 0 ? minIndex - 1 : -1;
        subSearchIndices[1] = minIndex;
        subSearchIndices[2] = minIndex + 1 < listSize ? minIndex + 1 : -1;

        final double[] subSearchDistances = new double[]{Double.NaN, Double.NaN, Double.NaN};

        for (int i = 0; i < subSearchIndices.length; i++) {
            if (subSearchIndices[i] > 0) {
                final LineString centerLine = centerLinesList.get(subSearchIndices[i]);
                if (centerLine != null) {
                    final Point[] lineCoordinates = centerLine.getCoordinates();
                    double lineDist = Double.MAX_VALUE;
                    for (final Point lineCoordinate : lineCoordinates) {
                        final double currentDistance = sphericalDistance.distance(lineCoordinate.getLon(), lineCoordinate.getLat());
                        if (currentDistance < lineDist) {
                            lineDist = currentDistance;
                        }
                    }

                    subSearchDistances[i] = lineDist;
                }
            }
        }

        int minOffset = Integer.MIN_VALUE;
        minDistance = Double.MAX_VALUE;
        for (int i = 0; i < subSearchDistances.length; i++) {
            final double currentDistance = subSearchDistances[i];
            if (Double.isNaN(currentDistance)) {
                continue;
            }
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                minOffset = i - 1;
            }
        }

        if (minOffset == Integer.MIN_VALUE) {
            return new Point2D[0];
        }

        minIndex = minIndex + minOffset;
        final GeoCoding geoCoding = geoCodingList.get(minIndex);
        final PixelPos pixelPos = geoCoding.getPixelPos(new GeoPos(lat, lon), null);
        final double subGeocodingY = pixelPos.getY();

        double y = subGeocodingY + STRIPE_HEIGHT * minIndex;
        final int index = (int) Math.floor(y / STRIPE_HEIGHT);
        if (index < (minIndex - 2) || index > (minIndex + 2)) {
            return new Point2D[0];
        }

        final Point2D.Double resultPoint = new Point2D.Double(pixelPos.getX(), y);
        return new Point2D[]{resultPoint};
    }

    void dispose() {
        latitudes = null;
        longitudes = null;
    }

    private void init() throws InvalidRangeException {
        boolean cross180 = false;
        geoCodingList = new ArrayList<>();
        centerLinesList = new ArrayList<>();

        final int[] shape = longitudes.getShape();
        sceneWidth = shape[1];
        sceneHeight = shape[0];
        final Product dummyProduct = new Product("DummyProduct", "type", sceneWidth, sceneHeight);

        final int gcRawSize = sceneWidth * STRIPE_HEIGHT;
        shape[0] = 2;   // one scan
        final int origin[] = {0, 0};
        for (int y = 0; y < sceneHeight; y += STRIPE_HEIGHT) {
            origin[0] = y;
            final float[] lons = new float[gcRawSize];
            final float[] lats = new float[gcRawSize];

            final Array lonSection = longitudes.section(origin, shape);
            final Array latSection = latitudes.section(origin, shape);
            final IndexIterator lonIterator = lonSection.getIndexIterator();
            final IndexIterator latIterator = latSection.getIndexIterator();
            int writeIndex = 0;
            while (lonIterator.hasNext() && latIterator.hasNext()) {
                lons[writeIndex] = lonIterator.getFloatNext();
                lats[writeIndex] = latIterator.getFloatNext();
                ++writeIndex;
            }

            final Range range = Range.computeRangeFloat(lats, IndexValidator.TRUE, null, ProgressMonitor.NULL);
            if (range.getMin() < -90) {
                geoCodingList.add(null);
                centerLinesList.add(null);
            } else {
                final TiePointGrid latTPG = new TiePointGrid("lat" + y, sceneWidth, STRIPE_HEIGHT, 0.5, 0.5, 1, 1, lats);
                final TiePointGrid lonTPG = new TiePointGrid("lon" + y, sceneWidth, STRIPE_HEIGHT, 0.5, 0.5, 1, 1, lons, true);
                dummyProduct.addTiePointGrid(latTPG);
                dummyProduct.addTiePointGrid(lonTPG);

                final TiePointGeoCoding geoCoding = new TiePointGeoCoding(latTPG, lonTPG, DefaultGeographicCRS.WGS84);
                cross180 = cross180 || geoCoding.isCrossingMeridianAt180();
                geoCodingList.add(geoCoding);

                final LineString centerLine = createCenterLine(geoCoding, sceneWidth);
                centerLinesList.add(centerLine);
            }
        }
        listSize = geoCodingList.size();
    }

    private LineString createCenterLine(TiePointGeoCoding geoCoding, final int sceneWidth) {
        final double numberOfSegments = 10.0;
        final double stepX = sceneWidth / numberOfSegments;

        final PixelPos pixelPos = new PixelPos();
        final GeoPos geoPos = new GeoPos();

        final List<Point> points = new ArrayList<>();

        pixelPos.y = 1.0;

        for (pixelPos.x = 0; pixelPos.x < sceneWidth + 0.5; pixelPos.x += stepX) {
            geoCoding.getGeoPos(pixelPos, geoPos);
            points.add(geometryFactory.createPoint(geoPos.getLon(), geoPos.getLat()));
        }

        return geometryFactory.createLineString(points);
    }
}
