package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class NdbcCWReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"ndbc-cw-ob", "ndbc-cw-cb", "ndbc-cw-lb", "ndbc-cw-os", "ndbc-cw-cs", "ndbc-cw-ls"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new NdbcCWReader();
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
