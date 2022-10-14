package com.bc.fiduceo.reader.netcdf;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StandardLayerExtension implements LayerExtension {

    private static final NumberFormat CHANNEL_INDEX_FORMAT = new DecimalFormat("00");

    private final int offset;

    public StandardLayerExtension() {
        this(0);
    }

    public StandardLayerExtension(int offset) {
        this.offset = offset;
    }

    @Override
    public String getExtension(int index) {
        return "_ch" + CHANNEL_INDEX_FORMAT.format(index + offset + 1);
    }
}
