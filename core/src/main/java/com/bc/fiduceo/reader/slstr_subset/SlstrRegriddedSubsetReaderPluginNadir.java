package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class SlstrRegriddedSubsetReaderPluginNadir implements ReaderPlugin {

    @Override
    public String[] getSupportedSensorKeys() {
        return new String[]{"slstr-s3a-uor-n", "slstr-s3b-uor-n"};
    }

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new SlstrRegriddedSubsetReader(readerContext, true);
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}