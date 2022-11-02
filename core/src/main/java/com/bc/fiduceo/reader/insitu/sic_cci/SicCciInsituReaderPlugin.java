package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SicCciInsituReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"sic-cci-dmisic0"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SicCciInsituReader();
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
