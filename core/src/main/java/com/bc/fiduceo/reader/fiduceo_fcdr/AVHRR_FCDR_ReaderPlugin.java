package com.bc.fiduceo.reader.fiduceo_fcdr;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class AVHRR_FCDR_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = {"avhrr-n11-fcdr", "avhrr-n12-fcdr", "avhrr-n14-fcdr", "avhrr-n15-fcdr", "avhrr-n16-fcdr", "avhrr-n17-fcdr", "avhrr-n18-fcdr", "avhrr-n19-fcdr", "avhrr-ma-fcdr"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new AVHRR_FCDR_Reader(readerContext);
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
