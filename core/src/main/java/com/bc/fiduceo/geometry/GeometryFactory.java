package com.bc.fiduceo.geometry;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryFactory {

    public enum Type{
        JTS,
        S2
    }
    private WKTReader wktReader;

    public GeometryFactory(Type type) {
        wktReader = new WKTReader();
    }

    public Geometry parse(String wkt) {
        final com.vividsolutions.jts.geom.Geometry geometry;
        try {
            geometry = wktReader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
        return new JTSPolygon((com.vividsolutions.jts.geom.Polygon) geometry);
    }
}
