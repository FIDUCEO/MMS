package com.bc.fiduceo.reader.insitu.ndbc;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class NdbcSMReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"ndbc-sm-ob", "ndbc-sm-cb", "ndbc-sm-lb", "ndbc-sm-os", "ndbc-sm-cs", "ndbc-sm-ls"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new NdbcSMReader();
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
