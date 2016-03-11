package com.bc.fiduceo.reader;

public interface ReaderPlugin {

    Reader createReader();

    String[] getSupportedSensorKeys();
}
