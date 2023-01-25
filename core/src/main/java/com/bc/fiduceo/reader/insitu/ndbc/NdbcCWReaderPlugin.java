package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.CONSTANT_WIND;

public class NdbcCWReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"ndbc-cw"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new NdbcInsituReader(CONSTANT_WIND);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.INSITU;
    }
}
