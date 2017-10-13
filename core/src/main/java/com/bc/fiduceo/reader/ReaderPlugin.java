package com.bc.fiduceo.reader;

import com.bc.fiduceo.geometry.GeometryFactory;

public interface ReaderPlugin {

    /**
     * Creates a new instance of a reader associated to this plugin. The reader is not opened.
     *
     * @param geometryFactory the geometry factory to be used
     * @return the reader object
     */
    Reader createReader(GeometryFactory geometryFactory);

    /**
     * Returns and array of Sensor-Keys supported by this reader.
     *
     * @return the sensor keys
     */
    String[] getSupportedSensorKeys();

    /**
     * Returns the reader data type, i.e. polar-orbiting or in-situ.
     *
     * @return the data type
     */
    DataType getDataType();
}
