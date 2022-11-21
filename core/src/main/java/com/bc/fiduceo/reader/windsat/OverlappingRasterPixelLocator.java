package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.RasterPixelLocator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

class OverlappingRasterPixelLocator implements PixelLocator {

    private final float[] lons;
    private final float[] lats;

    OverlappingRasterPixelLocator(float[] longitudes, float[] latitudes) {
        lons = longitudes;
        lats = latitudes;

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

        for (int index : cutIndices) {
            System.out.println("i: " + index + " -------------");
            System.out.println(normLongitudes[index - 1] + " " + normLongitudes[index]);
        }
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D point) {
        final int x_pos = (int) Math.round(x - 0.5);
        final int y_pos = (int) Math.round(y - 0.5);

        if (x_pos < 0 || x_pos >= lons.length || y_pos < 0 || y_pos >= lats.length) {
            return null;
        }

        final float lon = lons[x_pos];
        final float lat = lats[y_pos];

        if (point == null) {
            point = new Point2D.Float();
        }

        point.setLocation(lon, lat);
        return point;
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        throw new RuntimeException("not implemented");
    }
}
