package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.netcdf.LayerExtension;

import java.text.DecimalFormat;
import java.text.NumberFormat;

class ModisL1ReflectiveExtension implements LayerExtension {

    private static final NumberFormat CHANNEL_INDEX_FORMAT = new DecimalFormat("00");
    private static final int CHANNEL_OFFSET = 8;

    @Override
    public String getExtension(int index) {
        final int chIndex = index + CHANNEL_OFFSET;
        if (chIndex <= 12) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex);
        } else if (chIndex == 13) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex) + "L";
        } else if (chIndex == 14) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex - 1) + "H";
        } else if (chIndex == 15) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex - 1) + "L";
        } else if (chIndex == 16) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex - 2) + "H";
        } else if (chIndex <= 21) {
            return "_ch" + CHANNEL_INDEX_FORMAT.format(chIndex - 2);
        } else if (chIndex == 22) {
            return "_ch26";
        }

        throw new IllegalStateException("unsupported index");
    }
}
