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
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.math.IndexValidator;
import org.esa.snap.core.util.math.Range;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BowTiePixelLocator implements PixelLocator {

    private final GeometryFactory geometryFactory;
    private Array longitudes;
    private Array latitudes;

    private List<GeoCoding> geoCodingList;
    private List<LineString> centerLinesList;
    private int sceneWidth;
    private int sceneHeight;
    private int listSize;
    private final int stripHeight;


    BowTiePixelLocator(Array longitudes, Array latitudes, GeometryFactory geometryFactory, int stripHeight) throws IOException {
        this.geometryFactory = geometryFactory;
        this.longitudes = longitudes;
        this.latitudes = latitudes;
        this.stripHeight = stripHeight;

        init();
    }

    BowTiePixelLocator(Array longitudes, Array latitudes, GeometryFactory geometryFactory) throws IOException {
        this(longitudes, latitudes, geometryFactory, 2);
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        if (x < 0 || y < 0 || x > sceneWidth || y > sceneHeight) {
            return null;
        }
        int index = (int) Math.floor(y / stripHeight);
        if (index == listSize) {
            index--;
        }
        final GeoCoding geoCoding = geoCodingList.get(index);
        if (geoCoding == null) {
            return null;
        }

        final double geoCodingRelativeY = y - stripHeight * index;
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
                if (currentDistance <= minDistance) {
                    minDistance = currentDistance;
                    minIndex = currentIndex;
                }
            }
            ++currentIndex;
        }

        // check minIndex - 1 to minIndex + 1, if inside product
        final int[] subSearchIndices = new int[5];
        subSearchIndices[0] = minIndex - 2 >= 0 ? minIndex - 2 : -1;
        subSearchIndices[1] = minIndex - 1 >= 0 ? minIndex - 1 : -1;
        subSearchIndices[2] = minIndex;
        subSearchIndices[3] = minIndex + 1 < listSize ? minIndex + 1 : -1;
        subSearchIndices[4] = minIndex + 2 < listSize ? minIndex + 2 : -1;

        final double[] subSearchDistances = new double[subSearchIndices.length];
        Arrays.fill(subSearchDistances, Double.NaN);

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

        minIndex = Integer.MIN_VALUE;
        minDistance = Double.MAX_VALUE;
        for (int i = 0; i < subSearchDistances.length; i++) {
            final double currentDistance = subSearchDistances[i];
            if (Double.isNaN(currentDistance)) {
                continue;
            }
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                minIndex = subSearchIndices[i];
            }
        }

        if (minIndex == Integer.MIN_VALUE) {
            return new Point2D[0];
        }

        final GeoCoding geoCoding = geoCodingList.get(minIndex);
        final PixelPos pixelPos = geoCoding.getPixelPos(new GeoPos(lat, lon), null);
        final double subGeocodingY = pixelPos.getY();

        double y = subGeocodingY + stripHeight * minIndex;
        final int index = (int) Math.floor(y / stripHeight);
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

    private void init() throws IOException {
        boolean cross180 = false;
        geoCodingList = new ArrayList<>();
        centerLinesList = new ArrayList<>();

        final int[] shape = longitudes.getShape();
        sceneWidth = shape[1];
        sceneHeight = shape[0];
        final Product dummyProduct = new Product("DummyProduct", "type", sceneWidth, sceneHeight);

        final int gcRawSize = sceneWidth * stripHeight;
        shape[0] = stripHeight;   // one scan
        final int[] origin = {0, 0};
        for (int y = 0; y < sceneHeight; y += stripHeight) {
            origin[0] = y;
            final float[] lons = new float[gcRawSize];
            final float[] lats = new float[gcRawSize];

            final Array lonSection = NetCDFUtils.section(longitudes, origin, shape);
            final Array latSection = NetCDFUtils.section(latitudes, origin, shape);
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
                final TiePointGrid latTPG = new TiePointGrid("lat" + y, sceneWidth, stripHeight, 0.5, 0.5, 1, 1, lats);
                final TiePointGrid lonTPG = new TiePointGrid("lon" + y, sceneWidth, stripHeight, 0.5, 0.5, 1, 1, lons, true);
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

        pixelPos.y = stripHeight / 2.0;

        for (pixelPos.x = 0; pixelPos.x < sceneWidth + 0.5; pixelPos.x += stepX) {
            geoCoding.getGeoPos(pixelPos, geoPos);
            points.add(geometryFactory.createPoint(geoPos.getLon(), geoPos.getLat()));
        }

        return geometryFactory.createLineString(points);
    }
}
