package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;

class Nadir500mTransform extends Abstract500mTransform {

    private final int rasterWidth;
    private final int rasterHeight;

    Nadir500mTransform(int rasterWidth, int rasterHeight) {
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
    }

    @Override
    public Dimension getRasterSize() {
        return new Dimension("raster", rasterWidth, rasterHeight);
    }

    @Override
    public double mapCoordinate_X(double coordinate) {
        return 2.0 * coordinate;
    }

    @Override
    public double mapCoordinate_Y(double coordinate) {
        return 2.0 * coordinate;
    }

    @Override
    public double inverseCoordinate_X(double coordinate) {
        return coordinate * 0.5;
    }

    @Override
    public double inverseCoordinate_Y(double coordinate) {
        return coordinate * 0.5;
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public Interval mapInterval(Interval interval) {
        final int width = interval.getX() * 2;
        final int height = interval.getY() * 2;
        return new Interval(width, height);
    }
}
