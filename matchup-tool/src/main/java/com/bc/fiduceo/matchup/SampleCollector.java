package com.bc.fiduceo.matchup;

import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;

import java.awt.geom.Point2D;

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
        if (polygon.isEmpty()) {
            return null;
        }
        final Point[] coordinates = polygon.getCoordinates();
        for (Point coordinate : coordinates) {
            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(coordinate.getLon(), coordinate.getLat());
        }

        return new Sample[0];
    }
}
