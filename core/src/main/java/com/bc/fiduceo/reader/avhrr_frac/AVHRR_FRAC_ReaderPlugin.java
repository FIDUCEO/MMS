package com.bc.fiduceo.reader.avhrr_frac;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class AVHRR_FRAC_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = {"avhrr-frac-ma", "avhrr-frac-mb", "avhrr-frac-mc"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new AVHRR_FRAC_Reader(readerContext);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
