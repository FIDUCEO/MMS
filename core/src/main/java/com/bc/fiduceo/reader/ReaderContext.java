package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.GeometryFactory;

public class ReaderContext {
    private GeometryFactory geometryFactory;

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }
}
