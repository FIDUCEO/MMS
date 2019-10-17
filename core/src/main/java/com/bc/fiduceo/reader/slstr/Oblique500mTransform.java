package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

class Oblique500mTransform extends Abstract500mTransform {

    private final int rasterWidth;
    private final int rasterHeight;
    private final int rasterXOffset;

    Oblique500mTransform(int rasterWidth, int rasterHeight, int rasterXOffset) {
        this.rasterWidth = rasterWidth;
        this.rasterHeight = rasterHeight;
        this.rasterXOffset = rasterXOffset;
    }

    @Override
    public Dimension getRasterSize() {
        return new Dimension("raster", rasterWidth, rasterHeight);
    }

    @Override
    public int mapCoordinate_X(int coordinate) {
        return (coordinate - rasterXOffset) * 2;
    }

    @Override
    public int mapCoordinate_Y(int coordinate) {
        return coordinate * 2;
    }

    @Override
    public int inverseCoordinate_X(int coordinate) {
        // @todo 1 tb/tb add tests
        return (int) ((coordinate + rasterXOffset) * 0.5);
    }

    @Override
    public int inverseCoordinate_Y(int coordinate) {
        // @todo 1 tb/tb add tests
        return (int) (coordinate * 0.5);
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
