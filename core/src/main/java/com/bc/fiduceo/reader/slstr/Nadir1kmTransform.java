package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

class Nadir1kmTransform implements Transform {

    private final int rasterWidth;
    private final int rasterHeight;

    Nadir1kmTransform(int rasterWidth, int rasterHeight) {
        this.rasterWidth = rasterWidth / 2;
        this.rasterHeight = rasterHeight / 2;
    }

    @Override
    public Dimension getRasterSize() {
        return new Dimension("raster", rasterWidth, rasterHeight);
    }

    @Override
    public int mapCoordinate_X(int coordinate) {
        return coordinate;
    }

    @Override
    public int mapCoordinate_Y(int coordinate) {
        return coordinate;
    }

    @Override
    public int inverseCoordinate_X(int coordinate) {
        // @todo 1 tb/tb add tests
        return coordinate;
    }

    @Override
    public int inverseCoordinate_Y(int coordinate) {
        // @todo 1 tb/tb add tests
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
