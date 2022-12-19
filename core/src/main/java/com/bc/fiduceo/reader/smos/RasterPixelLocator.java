package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.location.PixelLocator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class RasterPixelLocator implements PixelLocator {

    private final float[] lons;
    private final float[] lats;
    private final Rectangle2D.Float boundary;

    /**
     * Pixel locator for rectangular rasters, lon and lat axes supplied as vectors.
     * Ordering:
     * lons[0] is westernmost,
     * lats[0] is southernmost
     *
     * @param lons     the longitude axis
     * @param lats     the latitude axis
     * @param boundary the raster boundaries
     */
    RasterPixelLocator(float[] lons, float[] lats, Rectangle2D.Float boundary) {
        this.lons = lons;
        this.lats = lats;
        this.boundary = boundary;
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
        if (!boundary.contains(new Point2D.Float((float) lon, (float) lat))) {
            return new Point2D[0];
        }

        int x_break = getIndexLargerThan(lon, lons);
        x_break = adjustForClosest(x_break, lon, lons);

        int y_break = getIndexLargerThan(lat, lats);
        y_break = adjustForClosest(y_break, lat, lats);

        return new Point2D[]{new Point2D.Float(x_break + 0.5f, y_break + 0.5f)};
    }

    private static int adjustForClosest(int targetIndex, double location, float[] locations) {
        // check which lon is closer
        if (targetIndex > 0) {
            final double delta_r = Math.abs(locations[targetIndex] - location);
            final double delta_l = Math.abs(locations[targetIndex - 1] - location);
            if (delta_l < delta_r) {
                targetIndex -= 1;
            }
        } else if (targetIndex == -1) {
            // we ran beyond the last lon, but are within the boundary, so: last lon
            targetIndex = locations.length - 1;
        }
        return targetIndex;
    }

    private static int getIndexLargerThan(double location, float[] locations) {
        int x_break = -1;
        for (int i = 0; i < locations.length; i++) {
            if (location <= locations[i]) {
                x_break = i;
                break;
            }
        }
        return x_break;
    }
}
