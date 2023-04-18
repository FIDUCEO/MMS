package com.bc.fiduceo.reader.insitu.tao;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class TaoReaderPlugin implements ReaderPlugin {

    private final static String[] SENSOR_KEYS = {"tao-sss"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new TaoReader();
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.INSITU;
    }
}
