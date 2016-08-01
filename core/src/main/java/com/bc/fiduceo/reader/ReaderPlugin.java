package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.GeometryFactory;

public interface ReaderPlugin {

    Reader createReader(GeometryFactory geometryFactory);

    String[] getSupportedSensorKeys();
}
