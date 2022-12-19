package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class DMISIC0SicInsituReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"DMISIC0-sic-cci"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SicCciInsituReader(".*DMISIC0.*.text");
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
