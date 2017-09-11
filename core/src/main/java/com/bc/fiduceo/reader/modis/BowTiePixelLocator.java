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
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductNode;
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

    private int lastCenterLineIndex;
    private boolean cross180;
    private List<GeoCoding> geoCodingList;
    private List<LineString> centerLinesList;


    BowTiePixelLocator(Array longitudes, Array latitudes, GeometryFactory geometryFactory) throws InvalidRangeException {
        this.geometryFactory = geometryFactory;
        this.longitudes = longitudes;
        this.latitudes = latitudes;

        init();
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final int index = (int) Math.round(y / STRIPE_HEIGHT);
        final GeoCoding geoCoding = geoCodingList.get(index);

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
        throw new RuntimeException("not implemented");
    }

    void dispose() {
        latitudes = null;
        longitudes = null;
    }

    private void init() throws InvalidRangeException {
        lastCenterLineIndex = 0;
        cross180 = false;
        geoCodingList = new ArrayList<>();
        centerLinesList = new ArrayList<>();

        final int[] shape = longitudes.getShape();
        final int sceneWidth = shape[1];
        final int sceneHeight = shape[0];

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
                final ModisTiePointGrid latTPG = new ModisTiePointGrid("lat" + y, sceneWidth, STRIPE_HEIGHT, 0.5, 0.5, 1, 1, lats);
                final ModisTiePointGrid lonTPG = new ModisTiePointGrid("lon" + y, sceneWidth, STRIPE_HEIGHT, 0.5, 0.5, 1, 1, lons, true);

                final TiePointGeoCoding geoCoding = new TiePointGeoCoding(latTPG, lonTPG, DefaultGeographicCRS.WGS84);
                cross180 = cross180 || geoCoding.isCrossingMeridianAt180();
                geoCodingList.add(geoCoding);

                final LineString centerLine = createCenterLine(geoCoding, sceneWidth);
                centerLinesList.add(centerLine);
            }
        }
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
    
    private class ModisTiePointGrid extends TiePointGrid {

        private Product fakeProduct;

        ModisTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints) {
            super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints);
            createFakeProduct(gridWidth, gridHeight);
        }

        ModisTiePointGrid(String name, int gridWidth, int gridHeight, double offsetX, double offsetY, double subSamplingX, double subSamplingY, float[] tiePoints, boolean containsAngles) {
            super(name, gridWidth, gridHeight, offsetX, offsetY, subSamplingX, subSamplingY, tiePoints, containsAngles);
            createFakeProduct(gridWidth, gridHeight);
        }

        @Override
        public ProductNode getOwner() {
            return fakeProduct;
        }

        private void createFakeProduct(int width, int height) {
            fakeProduct = new Product("fake", "fake", width, height);
        }
    }
}
