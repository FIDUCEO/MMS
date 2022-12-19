package com.bc.fiduceo.reader.netcdf;

public interface LayerExtension {

    String getExtension(int index);

    int getIndex(String extension);
}
