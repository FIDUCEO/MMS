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

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.*;
import com.google.common.collect.Lists;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.util.ArrayList;
import java.util.List;

public class BoundingPolygonCreator {

    private final int intervalX;
    private final int intervalY;
    private final GeometryFactory geometryFactory;

    public BoundingPolygonCreator(Interval interval, GeometryFactory geometryFactory) {
        if ((interval.getX() <= 0) || (interval.getY() <= 0)) {
            throw new RuntimeException("invalid interval");
        }
        this.intervalX = interval.getX();
        this.intervalY = interval.getY();

        this.geometryFactory = geometryFactory;
    }

    public Polygon createBoundingGeometry(Array longitudes, Array latitudes) {
        final List<Point> coordinates = extractBoundaryCoordinates(longitudes, latitudes);

        closePolygon(coordinates);

        return geometryFactory.createPolygon(coordinates);
    }


    public Polygon createBoundingGeometryClockwise(Array longitudes, Array latitudes) {
        final List<Point> coordinates = extractBoundaryCoordinates(longitudes, latitudes);

        closePolygon(coordinates);
        final List<Point> reverse = Lists.reverse(coordinates);

        return geometryFactory.createPolygon(reverse);
    }

    public Geometry createBoundingGeometrySplitted(Array longitudes, Array latitudes, int numSplits, boolean clockwise) {
        final List<Polygon> geometries = new ArrayList<>(numSplits);

        final int[] shape = longitudes.getShape();
        int height = shape[0];

        final int[] offsets = new int[]{0, 0};

        int yOffset = 0;
        int subsetHeight = getSubsetHeight(height, numSplits);
        for (int i = 0; i < numSplits; i++) {
            shape[0] = subsetHeight;
            offsets[0] = yOffset;

            Array longitudesSubset;
            Array latitudesSubset;
            try {
                longitudesSubset = longitudes.section(offsets, shape);
                latitudesSubset = latitudes.section(offsets, shape);
            } catch (InvalidRangeException e) {
                throw new RuntimeException(e.getMessage());
            }

            if (clockwise) {
                geometries.add(createBoundingGeometryClockwise(longitudesSubset, latitudesSubset));
            } else {
                geometries.add(createBoundingGeometry(longitudesSubset, latitudesSubset));
            }

            yOffset += subsetHeight - 1;
            if (yOffset + subsetHeight > height) {
                subsetHeight = height - yOffset;
            }
        }
        return geometryFactory.createMultiPolygon(geometries);
    }

    public LineString createTimeAxisGeometry(Array longitudes, Array latitudes) {
        final int[] shape = longitudes.getShape();
        final int xIndex = shape[1] / 2;
        final int maxY = shape[0] - 1;

        int maxYLoop = 0;
        final Index index = longitudes.getIndex();
        final List<Point> coordinates = new ArrayList<>();
        for (int y = 0; y <= maxY; y += intervalY) {
            index.set(y, xIndex);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));

            maxYLoop = y;
        }

        // ensure that we always have one point from the last scanline tb 2016-03-04
        if (maxYLoop < maxY) {
            index.set(maxY, xIndex);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));
        }

        return geometryFactory.createLineString(coordinates);
    }

    public Geometry createTimeAxisGeometrySplitted(Array longitudes, Array latitudes, int numSplits) {
        final Geometry[] geometries = new Geometry[numSplits];

        final int[] shape = longitudes.getShape();
        int height = shape[0];

        final int[] offsets = new int[]{0, 0};

        int yOffset = 0;
        int subsetHeight = getSubsetHeight(height, numSplits);
        for (int i = 0; i < numSplits; i++) {
            shape[0] = subsetHeight;
            offsets[0] = yOffset;

            Array longitudesSubset;
            Array latitudesSubset;
            try {
                longitudesSubset = longitudes.section(offsets, shape);
                latitudesSubset = latitudes.section(offsets, shape);
            } catch (InvalidRangeException e) {
                throw new RuntimeException(e.getMessage());
            }

            geometries[i] = createTimeAxisGeometry(longitudesSubset, latitudesSubset);

            yOffset += subsetHeight - 1;
            if (yOffset + subsetHeight > height) {
                subsetHeight = height - yOffset;
            }
        }
        return geometryFactory.createGeometryCollection(geometries);
    }

    public int getSubsetHeight(int height, int numSplits) {
        return height / numSplits + 1;
    }

    static void closePolygon(List<Point> coordinates) {
        if (coordinates.size() > 1) {
            coordinates.add(coordinates.get(0));
        }
    }

    public Interval[] extractValidIntervals(Array longitudes, double fillValue) {
        final int[] shape = longitudes.getShape();
        final int height = shape[0];
        final ArrayList<Interval> intervals = new ArrayList<>();

        final Index index = longitudes.getIndex();
        index.set(0, 0);

        Interval currentInterval = null;
        double lon = longitudes.getDouble(index);
        if (!isFillValue(lon, fillValue)) {
            currentInterval = new Interval();
            currentInterval.setX(0);
        }

        for (int i = 1; i < height; i++) {
            index.set(i, 0);
            lon = longitudes.getDouble(index);
            final boolean isFill = isFillValue(lon, fillValue);
            if (isFill && currentInterval != null) {
                // finishing an interval, we encountered fill value
                currentInterval.setY(i - 1);
                intervals.add(currentInterval);
                currentInterval = null;
            } else if (!isFill && currentInterval == null) {
                // we leave an invalid section and start a new interval
                currentInterval = new Interval();
                currentInterval.setX(i);
            }
        }
        if (currentInterval != null) {
            // if we`re still in a segment of valid data - close this
            currentInterval.setY(height -1);
            intervals.add(currentInterval);
        }

        return intervals.toArray(new Interval[]{});
    }

    // package access for testing only tb 2019-06-19
    static boolean isFillValue(double value, double fillValue) {
        return Math.abs(value - fillValue) < 1e-6;
    }

    private List<Point> extractBoundaryCoordinates(Array longitudes, Array latitudes) {
        final int[] shape = longitudes.getShape();
        int maxX = shape[1] - 1;
        int maxY = shape[0] - 1;

        final Index index = longitudes.getIndex();
        final List<Point> coordinates = new ArrayList<>();
        for (int y = 0; y < maxY; y += intervalY) {
            index.set(y, 0);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));
        }

        for (int x = 0; x < maxX; x += intervalX) {
            index.set(maxY, x);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));
        }

        for (int y = maxY; y > 0; y -= intervalY) {
            index.set(y, maxX);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));
        }

        for (int x = maxX; x > 0; x -= intervalX) {
            index.set(0, x);
            final double lon = longitudes.getDouble(index);
            final double lat = latitudes.getDouble(index);
            coordinates.add(geometryFactory.createPoint(lon, lat));
        }
        return coordinates;
    }
}
