package com.bc.fiduceo.reader.insitu.sirds_sst;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

class SirdsInsituReaderPlugin implements ReaderPlugin  {

    final String[] SUPPORTED_KEYS;

    SirdsInsituReaderPlugin(String supported_keys) {
        SUPPORTED_KEYS = new String[]{supported_keys};
    }

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SirdsInsituReader(SUPPORTED_KEYS[0]);
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
