package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

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
    public int mapCoordinate(int coordinate) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getOffset() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Interval mapInterval(Interval interval) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array process(Array array, double noDataValue) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array processFlags(Array array, int noDataValue) throws InvalidRangeException {
        throw new RuntimeException("not implemented");
    }
}
