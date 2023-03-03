package com.bc.fiduceo.reader.windsat;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class WindsatReaderPlugin implements ReaderPlugin {

    private static final String[] SUPPORTED_KEYS = {"windsat-coriolis"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new WindsatReader(readerContext);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
