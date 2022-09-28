package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.location.PixelLocator;

import java.awt.geom.Point2D;

class RasterPixelLocator implements PixelLocator {

    private final float[] lons;
    private final float[] lats;

    /**
     * Pixel locator for rectangular rasters, lon and lat axes supplied as vectors.
     * Ordering:
     * lons[0] is westernmost,
     * lats[0] is southernmost
     * @param lons the longitude axis
     * @param lats the latitude axis
     */
    RasterPixelLocator(float[] lons, float[] lats) {
        this.lons = lons;
        this.lats = lats;
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
