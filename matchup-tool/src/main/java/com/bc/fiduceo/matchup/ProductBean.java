package com.bc.fiduceo.matchup;

import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.Reader;

import java.util.List;

public class ProductBean {

    public final PixelLocator pixelLocator;
    /**
     * should be an unmodifiable List from Collections
     */
    public final List<Integer> splitLines;
    /**
     * should be an unmodifiable List from Collections
     */
    public final List<Polygon> productPolygones;

    /**
     * can get the valid bounding Geometries, TimeAxesGeometries, SplitLines, PixelLocator
     */
    public final Reader reader;

    public ProductBean(PixelLocator pixelLocator, List<Polygon> productPolygones, Reader reader, List<Integer> splitLines) {
        this.pixelLocator = pixelLocator;
        this.productPolygones = productPolygones;
        this.reader = reader;
        this.splitLines = splitLines;
    }
}
