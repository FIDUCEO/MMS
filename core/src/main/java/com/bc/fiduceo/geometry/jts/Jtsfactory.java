package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.AbstractGeometryFactory;
import com.bc.fiduceo.geometry.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Jtsfactory implements AbstractGeometryFactory {

    private final WKTReader wktReader;

    public Jtsfactory() {
        wktReader = new WKTReader();
    }

    @Override
    public Geometry parse(String wkt) {
        final com.vividsolutions.jts.geom.Geometry geometry;
        try {
            geometry = wktReader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            return new JTSPolygon((com.vividsolutions.jts.geom.Polygon) geometry);
        } else if (geometry instanceof com.vividsolutions.jts.geom.LineString) {
            return new JTSLineString((com.vividsolutions.jts.geom.LineString) geometry);
        }

        throw new RuntimeException("Unsupported geometry type");
    }
}
