package com.bc.fiduceo.matchup;

import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;

public class SampleCollector {

    private final PixelLocator pixelLocator;
    private final int width;
    private final int height;

    public SampleCollector(PixelLocator pixelLocator, int width, int height) {
        this.pixelLocator = pixelLocator;
        this.width = width;
        this.height = height;
    }

    public Sample[] getSamplesFor(Polygon polygon) {
        return new Sample[0];
    }
}
