package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;

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
}
