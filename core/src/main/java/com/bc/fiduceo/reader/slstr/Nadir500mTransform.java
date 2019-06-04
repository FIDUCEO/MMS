package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.*;

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
    public int mapCoordinate_X(int coordinate) {
        return 2 * coordinate;
    }

    @Override
    public int mapCoordinate_Y(int coordinate) {
        return 2 * coordinate;
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
