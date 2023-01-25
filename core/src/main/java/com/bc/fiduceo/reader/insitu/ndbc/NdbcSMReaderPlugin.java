package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

import static com.bc.fiduceo.reader.insitu.ndbc.MeasurementType.STANDARD_METEOROLOGICAL;

public class NdbcSMReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"ndbc-sm"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new NdbcInsituReader(STANDARD_METEOROLOGICAL);
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
