package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.netcdf.LayerExtension;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ModisL1EmissiveExtension implements LayerExtension {

    private static final NumberFormat CHANNEL_INDEX_FORMAT = new DecimalFormat("00");
    private static final int CHANNEL_OFFSET = 20;

    @Override
    public String getExtension(int index) {
        final int chIndex = index + CHANNEL_OFFSET;

        if (chIndex < 26) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex);
        } else if (chIndex <= 35) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex + 1);
        }

        throw new IllegalArgumentException("unsupported channel index");
    }

    @Override
    public int getIndex(String extension) {
        throw new RuntimeException("not implemented");
    }
}
