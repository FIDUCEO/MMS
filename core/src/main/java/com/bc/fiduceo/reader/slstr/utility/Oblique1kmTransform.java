package com.bc.fiduceo.reader.slstr.utility;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

class Oblique1kmTransform implements Transform {

    private final int rasterWidth;
    private final int rasterHeight;
    private final int rasterXOffset;

    Oblique1kmTransform(int rasterWidth, int rasterHeight, int rasterXOffset) {
        this.rasterWidth = rasterWidth / 2 - 600;
        this.rasterHeight = rasterHeight / 2;
        this.rasterXOffset = rasterXOffset;
    }

    @Override
    public Dimension getRasterSize() {
        return new Dimension("raster", rasterWidth, rasterHeight);
    }

    @Override
    public double mapCoordinate_X(double coordinate) {
        return coordinate - rasterXOffset;
    }

    @Override
    public double mapCoordinate_Y(double coordinate) {
        return coordinate;
    }

    @Override
    public double inverseCoordinate_X(double coordinate) {
        return coordinate + rasterXOffset;
    }

    @Override
    public double inverseCoordinate_Y(double coordinate) {
        return coordinate;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public Interval mapInterval(Interval interval) {
        return interval;
    }

    @Override
    public Array process(Array array, double noDataValue) {
        return array;
    }

    @Override
    public Array processFlags(Array array, int noDataValue) {
        return array;
    }
}
