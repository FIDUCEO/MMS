package com.bc.fiduceo.geometry.s2;

import com.bc.fiduceo.geometry.AbstractGeometryFactory;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.geometry.s2.S2WKTReader;
import com.google.common.geometry.S2Polyline;

public class S2Factory implements AbstractGeometryFactory {

    private final S2WKTReader s2WKTReader;

    public S2Factory() {
        s2WKTReader = new S2WKTReader();
    }

    @Override
    public Geometry parse(String wkt) {
        final Object geometry = s2WKTReader.read(wkt);
        if (geometry instanceof com.google.common.geometry.S2Polygon) {
            return new S2Polygon(geometry);
        } else if(geometry instanceof com.google.common.geometry.S2Polyline) {
            return new S2LineString((S2Polyline) geometry);
        }

        throw  new RuntimeException("Unsupported geometry type");
    }
}
