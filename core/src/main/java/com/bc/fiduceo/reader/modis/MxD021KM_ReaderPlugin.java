package com.bc.fiduceo.reader.modis;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class MxD021KM_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = new String[]{"mod021km-te", "myd021km-aq"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new MxD021KM_Reader(readerContext);
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
