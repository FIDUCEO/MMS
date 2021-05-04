package com.bc.fiduceo.post.plugin.era5;

import java.awt.*;

class InterpolationContext {

    private final BilinearInterpolator[][] interpolators;
    private final int width;
    private final int height;
    private Rectangle era5Region;

    InterpolationContext(int x, int y) {
        width = x;
        height = y;
        interpolators = new BilinearInterpolator[y][x];
    }

    BilinearInterpolator get(int x, int y) {
        checkBoundaries(x, y);
        return interpolators[y][x];
    }

    public void set(int x, int y, BilinearInterpolator interpolator) {
        checkBoundaries(x, y);

        interpolators[y][x] = interpolator;
    }

    Rectangle getEra5Region() {
        return era5Region;
    }

    void setEra5Region(Rectangle era5Region) {
        this.era5Region = era5Region;
    }

    private void checkBoundaries(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Access interpolator out of raster: " + x + ", " + y);
        }
    }
}
