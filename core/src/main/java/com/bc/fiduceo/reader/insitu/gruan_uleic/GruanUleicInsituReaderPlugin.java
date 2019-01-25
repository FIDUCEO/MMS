package com.bc.fiduceo.reader.insitu.gruan_uleic;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class GruanUleicInsituReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"gruan-uleic"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new GruanUleicInsituReader();
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
