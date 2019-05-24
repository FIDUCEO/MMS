package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;

class Oblique500mTransform implements Transform {

    @Override
    public Dimension getRasterSize() {
        throw new RuntimeException("not implemented");
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
}
