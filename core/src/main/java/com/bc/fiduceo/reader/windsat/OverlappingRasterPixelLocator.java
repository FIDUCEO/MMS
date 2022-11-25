package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.RasterPixelLocator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static com.bc.fiduceo.location.RasterPixelLocator.LON_EAST_WEST;

class OverlappingRasterPixelLocator implements PixelLocator {

    private final float[] lons;
    private final float[] lats;
    private final RasterPixelLocator[] pixelLocators;
    final int[] xOffsets;

    OverlappingRasterPixelLocator(float[] longitudes, float[] latitudes) {
        lons = longitudes;
        lats = latitudes;

        // this assumes that we do not have a single pixel wide segment at the anti-meridian tb 2022-11-23
        final float halfCellHeight = Math.abs(latitudes[1] - latitudes[0]) * 0.5f;
        final float halfCellWidth = Math.abs(longitudes[1] - longitudes[0]) * 0.5f;
        final float startLat = lats[0] - halfCellHeight;
        final float boundaryHeight = latitudes[latitudes.length - 1] - latitudes[0] + 2 * halfCellHeight;

        // convert longitudes to -180 -> 180 range
        // and detect anti-meridian crosses at the same iteration
        final float[] normLongitudes = new float[longitudes.length];
        final ArrayList<Integer> cutIndices = new ArrayList<>();

        normLongitudes[0] = longitudes[0] > 180.f ? longitudes[0] - 360.f : longitudes[0];
        for (int i = 1; i < longitudes.length; i++) {
            normLongitudes[i] = longitudes[i] > 180.f ? longitudes[i] - 360.f : longitudes[i];
            final float delta = Math.abs(normLongitudes[i] - normLongitudes[i - 1]);
            if (delta > 300.f) {
                cutIndices.add(i);
            }
        }

        final int numLocators = cutIndices.size() + 1;
        pixelLocators = new RasterPixelLocator[numLocators];
        xOffsets = new int[numLocators];
        int previousIndex = 0;
        for (int locIdx = 0; locIdx < numLocators; locIdx++) {
            final int cutIdx;
            if (locIdx >= cutIndices.size()) {
                cutIdx = normLongitudes.length;
            } else {
                cutIdx = cutIndices.get(locIdx);
            }

            final float[] subLons = new float[cutIdx - previousIndex];
            System.arraycopy(normLongitudes, previousIndex, subLons, 0, cutIdx - previousIndex);

            float lonMin = Float.MAX_VALUE;
            float lonMax = -Float.MAX_VALUE;
            for (float subLon : subLons) {
                if (subLon > lonMax) {
                    lonMax = subLon;
                }
                if (subLon < lonMin) {
                    lonMin = subLon;
                }
            }

            final float startLon = lonMin - halfCellWidth;
            final float width = lonMax - lonMin + 2 * halfCellWidth;
            final Rectangle2D.Float boundary = new Rectangle2D.Float(startLon, startLat, width, boundaryHeight);
            pixelLocators[locIdx] = new RasterPixelLocator(subLons, lats, boundary, LON_EAST_WEST);
            xOffsets[locIdx] = previousIndex;

            previousIndex = cutIdx;
        }
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final int x_pos = (int) Math.round(x - 0.5);
        final int y_pos = (int) Math.round(y - 0.5);

        if (x_pos < 0 || x_pos >= lons.length || y_pos < 0 || y_pos >= lats.length) {
            return null;
        }

        final float lon = lons[x_pos] > 180.f ? lons[x_pos] - 360.f : lons[x_pos];
        final float lat = lats[y_pos];

        if (point == null) {
            point = new Point2D.Float();
        }

        point.setLocation(lon, lat);
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final ArrayList<Point2D> resultList = new ArrayList<>();

        for (int i = 0; i < pixelLocators.length; i++) {
            final RasterPixelLocator pixelLocator = pixelLocators[i];
            final int xOffset = xOffsets[i];

            final Point2D[] pixelLocations = pixelLocator.getPixelLocation(lon, lat);
            for (final Point2D location : pixelLocations) {
                location.setLocation(location.getX() + xOffset, location.getY());
                resultList.add(location);
            }
        }

        return resultList.toArray(new Point2D[0]);
    }
}
