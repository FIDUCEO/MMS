package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.RasterPixelLocator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class OverlappingRasterPixelLocator implements PixelLocator {

    final RasterPixelLocator[] rasterLocators;

    // @todo 1 tb/tb we need to add the pixel boundaries for the segments in order to transform in both directions. 2022-11-18
    OverlappingRasterPixelLocator(float[][] lonVectors, float[][] latVectors, Rectangle2D.Float[] boundaries) {
        final int numElements = boundaries.length;
        rasterLocators = new RasterPixelLocator[numElements];

        for (int i = 0; i < numElements; i++) {
            rasterLocators[i] = new RasterPixelLocator(lonVectors[i], latVectors[i], boundaries[i]);
        }
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        throw new RuntimeException("not implemented");
    }
}
