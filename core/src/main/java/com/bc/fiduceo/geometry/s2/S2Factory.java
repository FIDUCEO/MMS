package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.AbstractGeometryFactory;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.geometry.s2.S2WKTReader;

public class S2Factory implements AbstractGeometryFactory {

    private final S2WKTReader s2WKTReader;

    public S2Factory() {
        s2WKTReader = new S2WKTReader();
    }

    @Override
    public Geometry parse(String wkt) {
        final Object geometry = s2WKTReader.read(wkt);
        return new S2Polygon(geometry);
    }
}
