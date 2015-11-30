package com.bc.fiduceo.geometry;

import com.bc.fiduceo.geometry.jts.Jtsfactory;
import com.bc.fiduceo.geometry.s2.S2Factory;

public class GeometryFactory implements AbstractGeometryFactory {

    public enum Type {
        JTS,
        S2
    }

    private final AbstractGeometryFactory factoryImpl;

    public GeometryFactory(Type type) {
        if (type == Type.JTS) {
            factoryImpl = new Jtsfactory();
        } else if (type == Type.S2) {
            factoryImpl = new S2Factory();
        } else {
            throw new IllegalArgumentException("unknown geometry factory type");
        }
    }

    public Geometry parse(String wkt) {
        return factoryImpl.parse(wkt);
    }
}
